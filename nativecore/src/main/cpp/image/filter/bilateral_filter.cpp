#include "bilateral_filter.h"
#include <cmath>
#include <algorithm>
#include <vector>

namespace nc {

namespace {

struct RangeLUT {
    float lut[256] = {};
    explicit RangeLUT(float sigmaColor) {
        const float inv2sc2 = -1.0f / (2.0f * sigmaColor * sigmaColor);
        for (int i = 0; i < 256; ++i)
            lut[i] = std::exp(i * i * inv2sc2);
    }
};

} // anonymous

void bilateralFilter(const Image8& src, Image8& dst,
                     int d, float sigmaColor, float sigmaSpace) {
    if (src.empty()) return;
    if (d < 1) d = 1;
    const int w = src.width, h = src.height, ch = src.channels;
    const int half = d / 2;
    dst = Image8(w, h, ch);

    RangeLUT rangeLut(sigmaColor);
    const float inv2ss2 = -1.0f / (2.0f * sigmaSpace * sigmaSpace);

    const int kd = d;
    std::vector<float> spaceW(static_cast<size_t>(kd * kd));
    for (int ky = 0; ky < kd; ++ky) {
        for (int kx = 0; kx < kd; ++kx) {
            float dy = ky - half, dx = kx - half;
            spaceW[ky * kd + kx] = std::exp((dx*dx + dy*dy) * inv2ss2);
        }
    }

    for (int y = 0; y < h; ++y) {
        uint8_t* drow = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            const uint8_t* center = src.rowPtr(y) + x * ch;
            float acc[4] = {0.0f, 0.0f, 0.0f, 0.0f};
            float wsum = 0.0f;

            for (int ky = 0; ky < kd; ++ky) {
                int sy = std::clamp(y + ky - half, 0, h - 1);
                const uint8_t* srow = src.rowPtr(sy);
                for (int kx = 0; kx < kd; ++kx) {
                    int sx = std::clamp(x + kx - half, 0, w - 1);
                    const uint8_t* neighbor = srow + sx * ch;
                    int cdiff = std::abs(static_cast<int>(neighbor[0]) - center[0]);
                    float weight = spaceW[ky * kd + kx] * rangeLut.lut[cdiff];
                    for (int c = 0; c < ch; ++c) acc[c] += weight * neighbor[c];
                    wsum += weight;
                }
            }

            if (wsum > 0.0f) {
                for (int c = 0; c < ch; ++c) {
                    drow[x * ch + c] = static_cast<uint8_t>(
                        std::clamp(acc[c] / wsum, 0.0f, 255.0f));
                }
            } else {
                for (int c = 0; c < ch; ++c)
                    drow[x * ch + c] = center[c];
            }
        }
    }
}

Image8 bilateralFilter(const Image8& src, int d, float sigmaColor, float sigmaSpace) {
    Image8 dst;
    bilateralFilter(src, dst, d, sigmaColor, sigmaSpace);
    return dst;
}

} // namespace nc
