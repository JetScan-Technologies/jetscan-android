#include "dilation.h"
#include <algorithm>

namespace nc {

static void dilateOnce(const Image8& src, Image8& dst, const StructuringElement& se) {
    const int w = src.width, h = src.height, ch = src.channels;
    const int half = se.ksize / 2;
    dst = Image8(w, h, ch);
    for (int y = 0; y < h; ++y) {
        uint8_t* drow = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            for (int c = 0; c < ch; ++c) {
                uint8_t maxV = 0;
                for (int ky = 0; ky < se.ksize; ++ky) {
                    for (int kx = 0; kx < se.ksize; ++kx) {
                        if (!se.get(ky, kx)) continue;
                        int sy = std::clamp(y + ky - half, 0, h - 1);
                        int sx = std::clamp(x + kx - half, 0, w - 1);
                        uint8_t v = src.rowPtr(sy)[sx * ch + c];
                        if (v > maxV) maxV = v;
                    }
                }
                drow[x * ch + c] = maxV;
            }
        }
    }
}

void dilate(const Image8& src, Image8& dst, const StructuringElement& se, int iterations) {
    if (src.empty()) return;
    Image8 cur = src.clone();
    for (int i = 0; i < iterations; ++i) {
        Image8 tmp;
        dilateOnce(cur, tmp, se);
        cur = std::move(tmp);
    }
    dst = std::move(cur);
}

Image8 dilate(const Image8& src, const StructuringElement& se, int iterations) {
    Image8 dst;
    dilate(src, dst, se, iterations);
    return dst;
}

void dilateRect(const Image8& src, Image8& dst, int ksize, int iterations) {
    dilate(src, dst, makeRect(ksize), iterations);
}

} // namespace nc
