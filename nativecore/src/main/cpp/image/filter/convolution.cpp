#include "convolution.h"
#include <algorithm>
#include <android/log.h>

#define TAG "nc::convolution"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

void filter2D(const Image8& src, Image8& dst, const float* kernel, int ksize) {
    if (src.empty()) return;
    if (!kernel) {
        LOGE("filter2D: null kernel pointer");
        return;
    }
    if (ksize < 1) {
        LOGE("filter2D: invalid ksize %d", ksize);
        return;
    }
    const int half = ksize / 2;
    const int w = src.width, h = src.height, ch = src.channels;
    dst = Image8(w, h, ch);
    for (int y = 0; y < h; ++y) {
        uint8_t* drow = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            for (int c = 0; c < ch; ++c) {
                float acc = 0.0f;
                for (int ky = 0; ky < ksize; ++ky) {
                    int sy = std::clamp(y + ky - half, 0, h - 1);
                    const uint8_t* srow = src.rowPtr(sy);
                    for (int kx = 0; kx < ksize; ++kx) {
                        int sx = std::clamp(x + kx - half, 0, w - 1);
                        acc += kernel[ky * ksize + kx] * srow[sx * ch + c];
                    }
                }
                drow[x * ch + c] = static_cast<uint8_t>(
                    std::clamp(static_cast<int>(acc), 0, 255));
            }
        }
    }
}

Image8 filter2D(const Image8& src, const float* kernel, int ksize) {
    Image8 dst;
    filter2D(src, dst, kernel, ksize);
    return dst;
}

} // namespace nc
