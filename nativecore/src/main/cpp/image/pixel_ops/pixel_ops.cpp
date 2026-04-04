#include "pixel_ops.h"
#include "../core/simd_utils.h"
#include <algorithm>
#include <cmath>
#include <android/log.h>

#define TAG "nc::pixel_ops"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

static inline uint8_t sat8(double v) {
    return static_cast<uint8_t>(std::clamp(v, 0.0, 255.0));
}

static inline bool dimensionsMatch(const Image8& a, const Image8& b) {
    return a.width == b.width && a.height == b.height && a.channels == b.channels;
}

void bitwiseNot(const Image8& src, Image8& dst) {
    if (src.empty()) return;
    if (dst.width != src.width || dst.height != src.height || dst.channels != src.channels)
        dst = Image8(src.width, src.height, src.channels);
    const int n = src.width * src.height * src.channels;
    const uint8_t* s = src.data.data();
    uint8_t*       d = dst.data.data();
    int i = 0;
#if NC_HAS_NEON
    for (; i <= n - 16; i += 16, s += 16, d += 16)
        vst1q_u8(d, vmvnq_u8(vld1q_u8(s)));
#endif
    for (; i < n; ++i) d[i] = ~s[i];
}

void bitwiseAnd(const Image8& a, const Image8& b, Image8& dst) {
    if (a.empty() || b.empty() || !dimensionsMatch(a, b)) {
        LOGE("bitwiseAnd: empty or dimension mismatch");
        return;
    }
    const int n = a.width * a.height * a.channels;
    if (dst.width != a.width || dst.height != a.height || dst.channels != a.channels)
        dst = Image8(a.width, a.height, a.channels);
    const uint8_t* sa = a.data.data();
    const uint8_t* sb = b.data.data();
    uint8_t*       d  = dst.data.data();
    int i = 0;
#if NC_HAS_NEON
    for (; i <= n - 16; i += 16, sa += 16, sb += 16, d += 16)
        vst1q_u8(d, vandq_u8(vld1q_u8(sa), vld1q_u8(sb)));
#endif
    for (; i < n; ++i) d[i] = sa[i] & sb[i];
}

void bitwiseOr(const Image8& a, const Image8& b, Image8& dst) {
    if (a.empty() || b.empty() || !dimensionsMatch(a, b)) {
        LOGE("bitwiseOr: empty or dimension mismatch");
        return;
    }
    const int n = a.width * a.height * a.channels;
    if (dst.width != a.width || dst.height != a.height || dst.channels != a.channels)
        dst = Image8(a.width, a.height, a.channels);
    const uint8_t* sa = a.data.data();
    const uint8_t* sb = b.data.data();
    uint8_t*       d  = dst.data.data();
    int i = 0;
#if NC_HAS_NEON
    for (; i <= n - 16; i += 16, sa += 16, sb += 16, d += 16)
        vst1q_u8(d, vorrq_u8(vld1q_u8(sa), vld1q_u8(sb)));
#endif
    for (; i < n; ++i) d[i] = sa[i] | sb[i];
}

void bitwiseXor(const Image8& a, const Image8& b, Image8& dst) {
    if (a.empty() || b.empty() || !dimensionsMatch(a, b)) {
        LOGE("bitwiseXor: empty or dimension mismatch");
        return;
    }
    const int n = a.width * a.height * a.channels;
    if (dst.width != a.width || dst.height != a.height || dst.channels != a.channels)
        dst = Image8(a.width, a.height, a.channels);
    const uint8_t* sa = a.data.data();
    const uint8_t* sb = b.data.data();
    uint8_t*       d  = dst.data.data();
    int i = 0;
#if NC_HAS_NEON
    for (; i <= n - 16; i += 16, sa += 16, sb += 16, d += 16)
        vst1q_u8(d, veorq_u8(vld1q_u8(sa), vld1q_u8(sb)));
#endif
    for (; i < n; ++i) d[i] = sa[i] ^ sb[i];
}

void absDiff(const Image8& a, const Image8& b, Image8& dst) {
    if (a.empty() || b.empty() || !dimensionsMatch(a, b)) {
        LOGE("absDiff: empty or dimension mismatch");
        return;
    }
    const int n = a.width * a.height * a.channels;
    if (dst.width != a.width || dst.height != a.height || dst.channels != a.channels)
        dst = Image8(a.width, a.height, a.channels);
    const uint8_t* sa = a.data.data();
    const uint8_t* sb = b.data.data();
    uint8_t*       d  = dst.data.data();
    int i = 0;
#if NC_HAS_NEON
    for (; i <= n - 16; i += 16, sa += 16, sb += 16, d += 16)
        vst1q_u8(d, vabdq_u8(vld1q_u8(sa), vld1q_u8(sb)));
#endif
    for (; i < n; ++i)
        d[i] = static_cast<uint8_t>(std::abs(static_cast<int>(sa[i]) - sb[i]));
}

void linearTransform(const Image8& src, Image8& dst, double alpha, double beta) {
    if (src.empty()) return;
    if (dst.width != src.width || dst.height != src.height || dst.channels != src.channels)
        dst = Image8(src.width, src.height, src.channels);
    const int n = src.width * src.height * src.channels;
    const uint8_t* s = src.data.data();
    uint8_t*       d = dst.data.data();
    int i = 0;
#if NC_HAS_NEON
    const float fa = static_cast<float>(alpha);
    const float fb = static_cast<float>(beta);
    const float32x4_t va = vdupq_n_f32(fa);
    const float32x4_t vb = vdupq_n_f32(fb);
    for (; i <= n - 8; i += 8) {
        // Load 8 uint8 pixels
        uint8x8_t src8 = vld1_u8(s + i);
        // Widen to 16-bit
        uint16x8_t src16 = vmovl_u8(src8);
        // Process low 4 pixels
        uint32x4_t lo32 = vmovl_u16(vget_low_u16(src16));
        float32x4_t flo = vcvtq_f32_u32(lo32);
        flo = vmlaq_f32(vb, flo, va); // alpha * src + beta
        // Process high 4 pixels
        uint32x4_t hi32 = vmovl_u16(vget_high_u16(src16));
        float32x4_t fhi = vcvtq_f32_u32(hi32);
        fhi = vmlaq_f32(vb, fhi, va);
        // Clamp to [0, 255] and narrow back
        int32x4_t ilo = vcvtq_s32_f32(flo);
        int32x4_t ihi = vcvtq_s32_f32(fhi);
        int16x4_t lo16 = vqmovn_s32(ilo);
        int16x4_t hi16 = vqmovn_s32(ihi);
        int16x8_t combined = vcombine_s16(lo16, hi16);
        uint8x8_t result = vqmovun_s16(combined);
        vst1_u8(d + i, result);
    }
#endif
    for (; i < n; ++i)
        d[i] = sat8(alpha * s[i] + beta);
}

void addWeighted(const Image8& a, double w1, const Image8& b, double w2,
                 double gamma, Image8& dst) {
    if (a.empty() || b.empty() || !dimensionsMatch(a, b)) {
        LOGE("addWeighted: empty or dimension mismatch");
        return;
    }
    const int n = a.width * a.height * a.channels;
    if (dst.width != a.width || dst.height != a.height || dst.channels != a.channels)
        dst = Image8(a.width, a.height, a.channels);
    const uint8_t* sa = a.data.data();
    const uint8_t* sb = b.data.data();
    uint8_t*       d  = dst.data.data();
    for (int i = 0; i < n; ++i)
        d[i] = sat8(w1 * sa[i] + w2 * sb[i] + gamma);
}

void multiplyScalar(const Image32& src, Image32& dst, double scalar) {
    if (src.empty()) return;
    if (dst.width != src.width || dst.height != src.height || dst.channels != src.channels)
        dst = Image32(src.width, src.height, src.channels);
    const int n = src.width * src.height * src.channels;
    const float f = static_cast<float>(scalar);
    const float* s = src.data.data();
    float*       d = dst.data.data();
    for (int i = 0; i < n; ++i) d[i] = s[i] * f;
}

void minClamp(const Image32& src, Image32& dst, float maxVal) {
    if (src.empty()) return;
    if (dst.width != src.width || dst.height != src.height || dst.channels != src.channels)
        dst = Image32(src.width, src.height, src.channels);
    const int n = src.width * src.height * src.channels;
    const float* s = src.data.data();
    float*       d = dst.data.data();
    for (int i = 0; i < n; ++i)
        d[i] = std::min(s[i], maxVal);
}

void divide(const Image8& a, const Image8& b, Image8& dst, double scale) {
    if (a.empty() || b.empty() || !dimensionsMatch(a, b)) {
        LOGE("divide: empty or dimension mismatch");
        return;
    }
    const int n = a.width * a.height * a.channels;
    if (dst.width != a.width || dst.height != a.height || dst.channels != a.channels)
        dst = Image8(a.width, a.height, a.channels);
    const uint8_t* sa = a.data.data();
    const uint8_t* sb = b.data.data();
    uint8_t*       d  = dst.data.data();
    for (int i = 0; i < n; ++i) {
        double denom = static_cast<double>(sb[i]);
        d[i] = (denom > 0.0) ? sat8(scale * sa[i] / denom) : 0;
    }
}

Image8 bitwiseNot(const Image8& src) {
    Image8 dst;
    bitwiseNot(src, dst);
    return dst;
}

Image8 absDiff(const Image8& a, const Image8& b) {
    Image8 dst;
    absDiff(a, b, dst);
    return dst;
}

} // namespace nc
