#include "sobel.h"
#include <cmath>
#include <algorithm>
#include <android/log.h>

#define TAG "nc::sobel"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

static inline int pixelAt(const Image8& src, int x, int y) {
    int sx = std::clamp(x, 0, src.width  - 1);
    int sy = std::clamp(y, 0, src.height - 1);
    return src.rowPtr(sy)[sx];
}

void sobel(const Image8& src, Image32& gx, Image32& gy) {
    if (src.empty() || src.channels != 1) {
        LOGE("sobel: expected non-empty 1-channel image");
        return;
    }
    const int w = src.width, h = src.height;
    gx = Image32(w, h, 1);
    gy = Image32(w, h, 1);
    for (int y = 0; y < h; ++y) {
        float* gxRow = gx.rowPtr(y);
        float* gyRow = gy.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            int tl = pixelAt(src, x-1, y-1), t = pixelAt(src, x, y-1), tr = pixelAt(src, x+1, y-1);
            int ml = pixelAt(src, x-1, y  ),                            mr = pixelAt(src, x+1, y  );
            int bl = pixelAt(src, x-1, y+1), b = pixelAt(src, x, y+1), br = pixelAt(src, x+1, y+1);
            gxRow[x] = static_cast<float>(-tl + tr - 2*ml + 2*mr - bl + br);
            gyRow[x] = static_cast<float>(-tl - 2*t - tr + bl + 2*b + br);
        }
    }
}

void sobelMagnitude(const Image8& src, Image8& mag) {
    Image32 gx, gy;
    sobel(src, gx, gy);
    if (gx.empty()) return;
    const int w = src.width, h = src.height;
    mag = Image8(w, h, 1);
    for (int y = 0; y < h; ++y) {
        const float* gxRow = gx.rowPtr(y);
        const float* gyRow = gy.rowPtr(y);
        uint8_t*     mRow  = mag.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            float m = std::sqrt(gxRow[x]*gxRow[x] + gyRow[x]*gyRow[x]);
            mRow[x] = static_cast<uint8_t>(std::clamp(m, 0.0f, 255.0f));
        }
    }
}

Image8 sobelMagnitude(const Image8& src) {
    Image8 mag; sobelMagnitude(src, mag); return mag;
}

} // namespace nc
