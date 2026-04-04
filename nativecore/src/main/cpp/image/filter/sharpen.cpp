#include "sharpen.h"
#include <algorithm>

namespace nc {

namespace {

static void apply3x3(const Image8& src, Image8& dst, const int kernel[9]) {
    if (src.empty()) return;
    const int w = src.width, h = src.height, ch = src.channels;
    dst = Image8(w, h, ch);
    for (int y = 0; y < h; ++y) {
        uint8_t* drow = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            for (int c = 0; c < ch; ++c) {
                int acc = 0;
                for (int ky = -1; ky <= 1; ++ky) {
                    int sy = std::clamp(y + ky, 0, h - 1);
                    const uint8_t* srow = src.rowPtr(sy);
                    for (int kx = -1; kx <= 1; ++kx) {
                        int sx = std::clamp(x + kx, 0, w - 1);
                        acc += kernel[(ky + 1) * 3 + (kx + 1)] * srow[sx * ch + c];
                    }
                }
                drow[x * ch + c] = static_cast<uint8_t>(std::clamp(acc, 0, 255));
            }
        }
    }
}

} // anonymous

void sharpenCross(const Image8& src, Image8& dst) {
    static const int k[9] = {0,-1,0, -1,5,-1, 0,-1,0};
    apply3x3(src, dst, k);
}

Image8 sharpenCross(const Image8& src) {
    Image8 dst; sharpenCross(src, dst); return dst;
}

void sharpenFull(const Image8& src, Image8& dst) {
    static const int k[9] = {-1,-1,-1, -1,9,-1, -1,-1,-1};
    apply3x3(src, dst, k);
}

Image8 sharpenFull(const Image8& src) {
    Image8 dst; sharpenFull(src, dst); return dst;
}

} // namespace nc
