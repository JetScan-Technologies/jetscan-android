#include "warp_perspective.h"
#include <algorithm>
#include <cmath>

namespace nc {

namespace {

// Bilinear sample from src at (fx, fy), writes ch channels into dst_px
static void bilinearSample(const Image8& src, float fx, float fy, uint8_t* dst_px) {
    const int x0 = static_cast<int>(fx);
    const int y0 = static_cast<int>(fy);
    const int x1 = std::min(x0 + 1, src.width  - 1);
    const int y1 = std::min(y0 + 1, src.height - 1);
    const float dx = fx - x0, dy = fy - y0;
    const int ch = src.channels;
    for (int c = 0; c < ch; ++c) {
        float v00 = src.rowPtr(y0)[x0 * ch + c];
        float v10 = src.rowPtr(y0)[x1 * ch + c];
        float v01 = src.rowPtr(y1)[x0 * ch + c];
        float v11 = src.rowPtr(y1)[x1 * ch + c];
        float v = v00*(1-dx)*(1-dy) + v10*dx*(1-dy)
                + v01*(1-dx)*dy    + v11*dx*dy;
        dst_px[c] = static_cast<uint8_t>(v < 0 ? 0 : v > 255 ? 255 : v);
    }
}

} // anonymous

void warpPerspective(const Image8& src, Image8& dst,
                     const Mat3f& H, int outW, int outH) {
    if (src.empty()) return;
    dst = Image8(outW, outH, src.channels);
    Mat3f Hinv = invert3x3(H);
    for (int y = 0; y < outH; ++y) {
        uint8_t* drow = dst.rowPtr(y);
        for (int x = 0; x < outW; ++x) {
            Point2f sp = applyHomography(Hinv, {static_cast<float>(x), static_cast<float>(y)});
            int sx = static_cast<int>(sp.x);
            int sy = static_cast<int>(sp.y);
            if (sx < 0 || sy < 0 || sx >= src.width || sy >= src.height) {
                // Out of bounds: black
                for (int c = 0; c < src.channels; ++c) drow[x * src.channels + c] = 0;
            } else {
                bilinearSample(src, sp.x, sp.y, drow + x * src.channels);
            }
        }
    }
}

Image8 warpPerspective(const Image8& src, const Mat3f& H, int outW, int outH) {
    Image8 dst; warpPerspective(src, dst, H, outW, outH); return dst;
}

} // namespace nc
