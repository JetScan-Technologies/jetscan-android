#include "warp_affine.h"
#include <cmath>
#include <algorithm>

namespace nc {

Affine2x3 rotationMatrix2D(Point2f center, float angleDeg, float scale) {
    float rad = angleDeg * M_PI / 180.0f;
    float cosA = std::cos(rad) * scale;
    float sinA = std::sin(rad) * scale;
    Affine2x3 M;
    M.m[0] = cosA;  M.m[1] = sinA; M.m[2] = (1-cosA)*center.x - sinA*center.y;
    M.m[3] = -sinA; M.m[4] = cosA; M.m[5] = sinA*center.x + (1-cosA)*center.y;
    return M;
}

// Invert 2x3 affine matrix
static Affine2x3 invertAffine(const Affine2x3& M) {
    const float a=M.m[0], b=M.m[1], tx=M.m[2];
    const float c=M.m[3], d=M.m[4], ty=M.m[5];
    float det = a*d - b*c;
    Affine2x3 inv;
    if (std::abs(det) < 1e-8f) return inv;
    float id = 1.0f / det;
    inv.m[0] = d*id;  inv.m[1] = -b*id;
    inv.m[3] = -c*id; inv.m[4] = a*id;
    inv.m[2] = -(inv.m[0]*tx + inv.m[1]*ty);
    inv.m[5] = -(inv.m[3]*tx + inv.m[4]*ty);
    return inv;
}

void warpAffine(const Image8& src, Image8& dst, const Affine2x3& M, int outW, int outH) {
    if (src.empty()) return;
    dst = Image8(outW, outH, src.channels);
    Affine2x3 Mi = invertAffine(M);
    const int ch = src.channels;
    for (int y = 0; y < outH; ++y) {
        uint8_t* drow = dst.rowPtr(y);
        for (int x = 0; x < outW; ++x) {
            float sx = Mi.m[0]*x + Mi.m[1]*y + Mi.m[2];
            float sy = Mi.m[3]*x + Mi.m[4]*y + Mi.m[5];
            int ix0 = static_cast<int>(sx), iy0 = static_cast<int>(sy);
            if (ix0 < 0 || iy0 < 0 || ix0 >= src.width || iy0 >= src.height) {
                for (int c = 0; c < ch; ++c) drow[x*ch+c] = 0;
                continue;
            }
            int ix1 = std::min(ix0+1, src.width-1);
            int iy1 = std::min(iy0+1, src.height-1);
            float dx = sx - ix0, dy = sy - iy0;
            for (int c = 0; c < ch; ++c) {
                float v00 = src.rowPtr(iy0)[ix0*ch+c];
                float v10 = src.rowPtr(iy0)[ix1*ch+c];
                float v01 = src.rowPtr(iy1)[ix0*ch+c];
                float v11 = src.rowPtr(iy1)[ix1*ch+c];
                float v = v00*(1-dx)*(1-dy) + v10*dx*(1-dy)
                        + v01*(1-dx)*dy    + v11*dx*dy;
                drow[x*ch+c] = static_cast<uint8_t>(v < 0 ? 0 : v > 255 ? 255 : v);
            }
        }
    }
}

Image8 warpAffine(const Image8& src, const Affine2x3& M, int outW, int outH) {
    Image8 dst; warpAffine(src, dst, M, outW, outH); return dst;
}

} // namespace nc
