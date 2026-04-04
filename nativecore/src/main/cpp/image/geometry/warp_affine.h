#pragma once
#include "../core/image.h"

// 2x3 affine matrix (row-major): [a,b,tx; c,d,ty]
struct Affine2x3 {
    float m[6] = {1,0,0, 0,1,0}; // identity
};

namespace nc {

// Build rotation matrix 2x3: rotate around center by angleDeg, scale
Affine2x3 rotationMatrix2D(Point2f center, float angleDeg, float scale = 1.0f);

// Affine warp using inverse mapping + bilinear interpolation
void warpAffine(const Image8& src, Image8& dst,
                const Affine2x3& M, int outW, int outH);
Image8 warpAffine(const Image8& src, const Affine2x3& M, int outW, int outH);

} // namespace nc
