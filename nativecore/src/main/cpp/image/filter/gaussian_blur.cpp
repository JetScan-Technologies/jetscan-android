#include "gaussian_blur.h"
#include "../core/simd_utils.h"
#include <vector>
#include <cmath>
#include <algorithm>
#include <android/log.h>

#define TAG "nc::gaussian_blur"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

namespace {

static std::vector<int> makeGaussKernel(int ksize, float sigma) {
    if (sigma <= 0.0f) sigma = 0.3f * ((ksize - 1) * 0.5f - 1.0f) + 0.8f;
    const int half = ksize / 2;
    std::vector<float> k(ksize);
    float sum = 0.0f;
    for (int i = 0; i < ksize; ++i) {
        float x = i - half;
        k[i] = std::exp(-x * x / (2.0f * sigma * sigma));
        sum += k[i];
    }
    // Fixed-point kernel normalized to sum=256
    std::vector<int> ki(ksize);
    int isum = 0;
    for (int i = 0; i < ksize - 1; ++i) {
        ki[i] = static_cast<int>(std::round(k[i] / sum * 256.0f));
        isum += ki[i];
    }
    ki[ksize - 1] = 256 - isum;
    return ki;
}

static void hPass(const Image8& src, Image8& tmp, const std::vector<int>& ki) {
    const int ksize = static_cast<int>(ki.size());
    const int half  = ksize / 2;
    const int w = src.width, h = src.height, ch = src.channels;
    tmp = Image8(w, h, ch);
    for (int y = 0; y < h; ++y) {
        const uint8_t* row = src.rowPtr(y);
        uint8_t*       out = tmp.rowPtr(y);
        // Left border (x < half): use clamp
        for (int x = 0; x < std::min(half, w); ++x) {
            for (int c = 0; c < ch; ++c) {
                int acc = 0;
                for (int k = 0; k < ksize; ++k) {
                    int sx = std::clamp(x + k - half, 0, w - 1);
                    acc += ki[k] * row[sx * ch + c];
                }
                out[x * ch + c] = static_cast<uint8_t>(acc >> 8);
            }
        }
        // Interior (no clamp needed): x in [half, w-half)
        for (int x = half; x < w - half; ++x) {
            for (int c = 0; c < ch; ++c) {
                int acc = 0;
                const uint8_t* base = row + (x - half) * ch + c;
                for (int k = 0; k < ksize; ++k) {
                    acc += ki[k] * base[k * ch];
                }
                out[x * ch + c] = static_cast<uint8_t>(acc >> 8);
            }
        }
        // Right border (x >= w-half): use clamp
        for (int x = std::max(half, w - half); x < w; ++x) {
            for (int c = 0; c < ch; ++c) {
                int acc = 0;
                for (int k = 0; k < ksize; ++k) {
                    int sx = std::clamp(x + k - half, 0, w - 1);
                    acc += ki[k] * row[sx * ch + c];
                }
                out[x * ch + c] = static_cast<uint8_t>(acc >> 8);
            }
        }
    }
}

static void vPass(const Image8& tmp, Image8& dst, const std::vector<int>& ki) {
    const int ksize = static_cast<int>(ki.size());
    const int half  = ksize / 2;
    const int w = tmp.width, h = tmp.height, ch = tmp.channels;
    dst = Image8(w, h, ch);
    const int stride = w * ch;
    // Top border (y < half): use clamp
    for (int y = 0; y < std::min(half, h); ++y) {
        uint8_t* out = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            for (int c = 0; c < ch; ++c) {
                int acc = 0;
                for (int k = 0; k < ksize; ++k) {
                    int sy = std::clamp(y + k - half, 0, h - 1);
                    acc += ki[k] * tmp.rowPtr(sy)[x * ch + c];
                }
                out[x * ch + c] = static_cast<uint8_t>(acc >> 8);
            }
        }
    }
    // Interior (no clamp needed): y in [half, h-half)
    for (int y = half; y < h - half; ++y) {
        uint8_t* out = dst.rowPtr(y);
        const uint8_t* baseRow = tmp.rowPtr(y - half);
        for (int x = 0; x < w; ++x) {
            for (int c = 0; c < ch; ++c) {
                int acc = 0;
                const uint8_t* base = baseRow + x * ch + c;
                for (int k = 0; k < ksize; ++k) {
                    acc += ki[k] * base[k * stride];
                }
                out[x * ch + c] = static_cast<uint8_t>(acc >> 8);
            }
        }
    }
    // Bottom border (y >= h-half): use clamp
    for (int y = std::max(half, h - half); y < h; ++y) {
        uint8_t* out = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            for (int c = 0; c < ch; ++c) {
                int acc = 0;
                for (int k = 0; k < ksize; ++k) {
                    int sy = std::clamp(y + k - half, 0, h - 1);
                    acc += ki[k] * tmp.rowPtr(sy)[x * ch + c];
                }
                out[x * ch + c] = static_cast<uint8_t>(acc >> 8);
            }
        }
    }
}

} // anonymous

void gaussianBlur(const Image8& src, Image8& dst, int ksize, float sigma) {
    if (src.empty()) return;
    if (ksize < 3) ksize = 3;
    if (ksize % 2 == 0) ksize++;
    auto ki = makeGaussKernel(ksize, sigma);
    Image8 tmp;
    hPass(src, tmp, ki);
    vPass(tmp, dst, ki);
}

Image8 gaussianBlur(const Image8& src, int ksize, float sigma) {
    Image8 dst;
    gaussianBlur(src, dst, ksize, sigma);
    return dst;
}

} // namespace nc
