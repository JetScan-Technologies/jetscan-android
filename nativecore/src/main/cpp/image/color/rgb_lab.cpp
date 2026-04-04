#include "rgb_lab.h"
#include <cmath>
#include <algorithm>
#include <android/log.h>

#define TAG "nc::rgb_lab"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

namespace {

static inline float srgbLinear(float c) {
    return (c <= 0.04045f) ? c / 12.92f : std::pow((c + 0.055f) / 1.055f, 2.4f);
}

static inline float linearSrgb(float c) {
    return (c <= 0.0031308f) ? c * 12.92f : 1.055f * std::pow(c, 1.0f / 2.4f) - 0.055f;
}

static inline float labF(float t) {
    const float d = 6.0f / 29.0f;
    return (t > d * d * d) ? std::cbrt(t)
                           : t / (3.0f * d * d) + 4.0f / 29.0f;
}

static inline float labFInv(float t) {
    const float d = 6.0f / 29.0f;
    return (t > d) ? t * t * t : 3.0f * d * d * (t - 4.0f / 29.0f);
}

// D65 reference white
static constexpr float Xn = 0.95047f;
static constexpr float Yn = 1.00000f;
static constexpr float Zn = 1.08883f;

static void rgbToLabPixel(uint8_t r8, uint8_t g8, uint8_t b8,
                           float& L, float& a, float& bOut) {
    float rLin = srgbLinear(r8 / 255.0f);
    float gLin = srgbLinear(g8 / 255.0f);
    float bLin = srgbLinear(b8 / 255.0f);
    float X = 0.4124564f * rLin + 0.3575761f * gLin + 0.1804375f * bLin;
    float Y = 0.2126729f * rLin + 0.7151522f * gLin + 0.0721750f * bLin;
    float Z = 0.0193339f * rLin + 0.1191920f * gLin + 0.9503041f * bLin;
    float fx = labF(X / Xn);
    float fy = labF(Y / Yn);
    float fz = labF(Z / Zn);
    L    = 116.0f * fy - 16.0f;
    a    = 500.0f * (fx - fy);
    bOut = 200.0f * (fy - fz);
}

static void labToRgbPixel(float L, float a, float bIn,
                           uint8_t& r8, uint8_t& g8, uint8_t& b8) {
    float fy = (L + 16.0f) / 116.0f;
    float fx = a / 500.0f + fy;
    float fz = fy - bIn / 200.0f;
    float X = Xn * labFInv(fx);
    float Y = Yn * labFInv(fy);
    float Z = Zn * labFInv(fz);
    float rLin =  3.2404542f * X - 1.5371385f * Y - 0.4985314f * Z;
    float gLin = -0.9692660f * X + 1.8760108f * Y + 0.0415560f * Z;
    float bLin =  0.0556434f * X - 0.2040259f * Y + 1.0572252f * Z;
    r8 = static_cast<uint8_t>(std::clamp(linearSrgb(rLin) * 255.0f, 0.0f, 255.0f));
    g8 = static_cast<uint8_t>(std::clamp(linearSrgb(gLin) * 255.0f, 0.0f, 255.0f));
    b8 = static_cast<uint8_t>(std::clamp(linearSrgb(bLin) * 255.0f, 0.0f, 255.0f));
}

} // anonymous

Image32 rgbToLab(const Image8& src) {
    if (src.empty() || (src.channels != 3 && src.channels != 4)) return {};
    Image32 out(src.width, src.height, 3);
    const int w = src.width, h = src.height, ch = src.channels;
    for (int y = 0; y < h; ++y) {
        const uint8_t* s = src.rowPtr(y);
        float*         d = out.rowPtr(y);
        for (int x = 0; x < w; ++x, s += ch, d += 3) {
            rgbToLabPixel(s[0], s[1], s[2], d[0], d[1], d[2]);
        }
    }
    return out;
}

Image8 labToRgb(const Image32& lab) {
    if (lab.empty() || lab.channels != 3) return {};
    Image8 out(lab.width, lab.height, 3);
    const int w = lab.width, h = lab.height;
    for (int y = 0; y < h; ++y) {
        const float* s = lab.rowPtr(y);
        uint8_t*     d = out.rowPtr(y);
        for (int x = 0; x < w; ++x, s += 3, d += 3) {
            labToRgbPixel(s[0], s[1], s[2], d[0], d[1], d[2]);
        }
    }
    return out;
}

void splitLab(const Image32& lab, Image32& L, Image32& a, Image32& b) {
    if (lab.empty() || lab.channels != 3) return;
    const int w = lab.width, h = lab.height;
    L = Image32(w, h, 1);
    a = Image32(w, h, 1);
    b = Image32(w, h, 1);
    for (int y = 0; y < h; ++y) {
        const float* src = lab.rowPtr(y);
        float* Lr = L.rowPtr(y);
        float* ar = a.rowPtr(y);
        float* br = b.rowPtr(y);
        for (int x = 0; x < w; ++x, src += 3) {
            Lr[x] = src[0];
            ar[x] = src[1];
            br[x] = src[2];
        }
    }
}

void mergeLab(const Image32& L, const Image32& a, const Image32& b, Image32& dst) {
    if (L.empty() || a.empty() || b.empty()) return;
    const int w = L.width, h = L.height;
    dst = Image32(w, h, 3);
    for (int y = 0; y < h; ++y) {
        float* d = dst.rowPtr(y);
        const float* Lr = L.rowPtr(y);
        const float* ar = a.rowPtr(y);
        const float* br = b.rowPtr(y);
        for (int x = 0; x < w; ++x, d += 3) {
            d[0] = Lr[x]; d[1] = ar[x]; d[2] = br[x];
        }
    }
}

} // namespace nc
