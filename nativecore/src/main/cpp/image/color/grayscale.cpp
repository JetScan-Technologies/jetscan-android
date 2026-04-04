#include "grayscale.h"
#include "../core/simd_utils.h"
#include <android/log.h>

#define TAG "nc::grayscale"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

// BT.601 integer coefficients: 77*R + 150*G + 29*B (sum=256) >> 8
static constexpr int kR = 77;
static constexpr int kG = 150;
static constexpr int kB = 29;

void toGrayscale(const Image8& src, Image8& dst) {
    if (src.empty()) return;
    if (dst.width != src.width || dst.height != src.height || dst.channels != 1) {
        dst = Image8(src.width, src.height, 1);
    }
    const int ch = src.channels;
    if (ch != 3 && ch != 4) {
        LOGE("toGrayscale: expected 3 or 4 channels, got %d", ch);
        return;
    }
    const int w = src.width;
    const int h = src.height;

#if NC_HAS_NEON
    for (int y = 0; y < h; ++y) {
        const uint8_t* s = src.rowPtr(y);
        uint8_t*       d = dst.rowPtr(y);
        int x = 0;
        for (; x <= w - 8; x += 8, s += 8 * ch, d += 8) {
            uint8x8_t r8, g8, b8;
            if (ch == 3) {
                uint8x8x3_t rgb = vld3_u8(s);
                r8 = rgb.val[0]; g8 = rgb.val[1]; b8 = rgb.val[2];
            } else {
                uint8x8x4_t rgba = vld4_u8(s);
                r8 = rgba.val[0]; g8 = rgba.val[1]; b8 = rgba.val[2];
            }
            uint16x8_t acc = vmull_u8(r8, vdup_n_u8(kR));
            acc = vmlal_u8(acc, g8, vdup_n_u8(kG));
            acc = vmlal_u8(acc, b8, vdup_n_u8(kB));
            uint8x8_t gray = vshrn_n_u16(acc, 8);
            vst1_u8(d, gray);
        }
        for (; x < w; ++x, s += ch, ++d) {
            *d = static_cast<uint8_t>((kR * s[0] + kG * s[1] + kB * s[2]) >> 8);
        }
    }
#else
    for (int y = 0; y < h; ++y) {
        const uint8_t* s = src.rowPtr(y);
        uint8_t*       d = dst.rowPtr(y);
        for (int x = 0; x < w; ++x, s += ch, ++d) {
            *d = static_cast<uint8_t>((kR * s[0] + kG * s[1] + kB * s[2]) >> 8);
        }
    }
#endif
}

Image8 toGrayscale(const Image8& src) {
    Image8 dst;
    toGrayscale(src, dst);
    return dst;
}

} // namespace nc
