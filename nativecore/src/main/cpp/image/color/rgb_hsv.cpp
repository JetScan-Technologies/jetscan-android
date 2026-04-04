#include "rgb_hsv.h"
#include <cmath>
#include <algorithm>
#include <android/log.h>

#define TAG "nc::rgb_hsv"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

namespace {

static void pixelRgbToHsv(float r, float g, float b,
                           float& H, float& S, float& V) {
    float maxC = std::max({r, g, b});
    float minC = std::min({r, g, b});
    float delta = maxC - minC;
    V = maxC;
    S = (maxC > 1e-5f) ? (delta / maxC) : 0.0f;
    if (delta < 1e-5f) { H = 0.0f; return; }
    if      (maxC == r) H = 60.0f * std::fmod((g - b) / delta, 6.0f);
    else if (maxC == g) H = 60.0f * ((b - r) / delta + 2.0f);
    else                H = 60.0f * ((r - g) / delta + 4.0f);
    if (H < 0.0f) H += 360.0f;
}

static void pixelHsvToRgb(float H, float S, float V,
                           float& r, float& g, float& b) {
    if (S <= 1e-5f) { r = g = b = V; return; }
    float hh = H / 60.0f;
    int   i  = static_cast<int>(hh);
    float ff = hh - i;
    float p  = V * (1.0f - S);
    float q  = V * (1.0f - S * ff);
    float t  = V * (1.0f - S * (1.0f - ff));
    switch (i % 6) {
        case 0: r = V; g = t; b = p; break;
        case 1: r = q; g = V; b = p; break;
        case 2: r = p; g = V; b = t; break;
        case 3: r = p; g = q; b = V; break;
        case 4: r = t; g = p; b = V; break;
        default: r = V; g = p; b = q; break;
    }
}

} // anonymous

Image32 rgbToHsv(const Image8& src) {
    if (src.empty() || (src.channels != 3 && src.channels != 4)) return {};
    Image32 out(src.width, src.height, 3);
    const int w = src.width, h = src.height, ch = src.channels;
    for (int y = 0; y < h; ++y) {
        const uint8_t* s = src.rowPtr(y);
        float*         d = out.rowPtr(y);
        for (int x = 0; x < w; ++x, s += ch, d += 3) {
            pixelRgbToHsv(s[0] / 255.0f, s[1] / 255.0f, s[2] / 255.0f,
                           d[0], d[1], d[2]);
        }
    }
    return out;
}

Image8 hsvToRgb(const Image32& hsv) {
    if (hsv.empty() || hsv.channels != 3) return {};
    Image8 out(hsv.width, hsv.height, 3);
    const int w = hsv.width, h = hsv.height;
    for (int y = 0; y < h; ++y) {
        const float* s = hsv.rowPtr(y);
        uint8_t*     d = out.rowPtr(y);
        for (int x = 0; x < w; ++x, s += 3, d += 3) {
            float r, g, b;
            pixelHsvToRgb(s[0], s[1], s[2], r, g, b);
            d[0] = static_cast<uint8_t>(std::clamp(r * 255.0f, 0.0f, 255.0f));
            d[1] = static_cast<uint8_t>(std::clamp(g * 255.0f, 0.0f, 255.0f));
            d[2] = static_cast<uint8_t>(std::clamp(b * 255.0f, 0.0f, 255.0f));
        }
    }
    return out;
}

Image8 extractHsvChannel(const Image8& src, int channel) {
    if (channel < 0 || channel > 2) return {};
    Image32 hsv = rgbToHsv(src);
    if (hsv.empty()) return {};
    Image8 out(src.width, src.height, 1);
    const float scale = (channel == 0) ? (255.0f / 360.0f) : 255.0f;
    const int w = src.width, h = src.height;
    for (int y = 0; y < h; ++y) {
        const float* s = hsv.rowPtr(y);
        uint8_t*     d = out.rowPtr(y);
        for (int x = 0; x < w; ++x, s += 3, ++d) {
            *d = static_cast<uint8_t>(std::clamp(s[channel] * scale, 0.0f, 255.0f));
        }
    }
    return out;
}

Image8 scaleSaturation(const Image8& src, float factor) {
    Image32 hsv = rgbToHsv(src);
    if (hsv.empty()) return {};
    const int w = src.width, h = src.height;
    for (int y = 0; y < h; ++y) {
        float* row = hsv.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            float* s = row + x * 3;
            s[1] = std::clamp(s[1] * factor, 0.0f, 1.0f);
        }
    }
    return hsvToRgb(hsv);
}

void splitHsv(const Image32& hsv, Image32& H, Image32& S, Image32& V) {
    if (hsv.empty() || hsv.channels != 3) return;
    const int w = hsv.width, h = hsv.height;
    H = Image32(w, h, 1);
    S = Image32(w, h, 1);
    V = Image32(w, h, 1);
    for (int y = 0; y < h; ++y) {
        const float* src = hsv.rowPtr(y);
        float*       hRow = H.rowPtr(y);
        float*       sRow = S.rowPtr(y);
        float*       vRow = V.rowPtr(y);
        for (int x = 0; x < w; ++x, src += 3) {
            hRow[x] = src[0];
            sRow[x] = src[1];
            vRow[x] = src[2];
        }
    }
}

void mergeHsv(const Image32& H, const Image32& S, const Image32& V, Image32& dst) {
    if (H.empty() || S.empty() || V.empty()) return;
    const int w = H.width, h = H.height;
    dst = Image32(w, h, 3);
    for (int y = 0; y < h; ++y) {
        float* d = dst.rowPtr(y);
        const float* hr = H.rowPtr(y);
        const float* sr = S.rowPtr(y);
        const float* vr = V.rowPtr(y);
        for (int x = 0; x < w; ++x, d += 3) {
            d[0] = hr[x]; d[1] = sr[x]; d[2] = vr[x];
        }
    }
}

} // namespace nc
