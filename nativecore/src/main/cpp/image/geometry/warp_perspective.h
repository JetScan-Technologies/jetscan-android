#pragma once
#include "../core/image.h"
#include "homography.h"

namespace nc {

// Perspective warp using inverse mapping + bilinear interpolation.
// H maps src → dst, so we compute H_inv to map dst → src.
void warpPerspective(const Image8& src, Image8& dst,
                     const Mat3f& H, int outW, int outH);
Image8 warpPerspective(const Image8& src, const Mat3f& H, int outW, int outH);

} // namespace nc
