#include "harris.h"
#include "sobel.h"
#include "../filter/gaussian_blur.h"
#include <cmath>
#include <algorithm>

namespace nc {

void harrisCorners(const Image8& src, Image32& dst,
                   int blockSize, int /*apertureSize*/, float k) {
    if (src.empty()) return;
    const int w = src.width, h = src.height;

    // Compute Sobel gradients
    Image32 gx, gy;
    sobel(src, gx, gy);

    // Structure tensor components: Ix², IxIy, Iy²
    Image32 Ixx(w, h, 1), Ixy(w, h, 1), Iyy(w, h, 1);
    for (int y = 0; y < h; ++y) {
        const float* gxr = gx.rowPtr(y);
        const float* gyr = gy.rowPtr(y);
        float* xxr = Ixx.rowPtr(y);
        float* xyr = Ixy.rowPtr(y);
        float* yyr = Iyy.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            xxr[x] = gxr[x] * gxr[x];
            xyr[x] = gxr[x] * gyr[x];
            yyr[x] = gyr[x] * gyr[x];
        }
    }

    // Smooth structure tensor with Gaussian
    // We reinterpret Image32 as Image8 for Gaussian (hack: can't directly, use float pass)
    // Instead, manually box-sum over blockSize window
    const int half = blockSize / 2;
    dst = Image32(w, h, 1);
    for (int y = 0; y < h; ++y) {
        float* drow = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            float sxx = 0, sxy = 0, syy = 0;
            for (int by = -half; by <= half; ++by) {
                int sy2 = std::clamp(y + by, 0, h - 1);
                for (int bx = -half; bx <= half; ++bx) {
                    int sx2 = std::clamp(x + bx, 0, w - 1);
                    sxx += Ixx.rowPtr(sy2)[sx2];
                    sxy += Ixy.rowPtr(sy2)[sx2];
                    syy += Iyy.rowPtr(sy2)[sx2];
                }
            }
            float det   = sxx * syy - sxy * sxy;
            float trace = sxx + syy;
            drow[x] = det - k * trace * trace;
        }
    }
}

Image32 harrisCorners(const Image8& src, int blockSize, float k) {
    Image32 dst; harrisCorners(src, dst, blockSize, 3, k); return dst;
}

} // namespace nc
