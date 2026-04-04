#pragma once
#include "../core/image.h"

namespace nc {

// Edge-preserving bilateral filter.
// d: diameter of pixel neighborhood, sigmaColor/sigmaSpace: filter sigmas
void bilateralFilter(const Image8& src, Image8& dst,
                     int d, float sigmaColor, float sigmaSpace);
Image8 bilateralFilter(const Image8& src, int d, float sigmaColor, float sigmaSpace);

} // namespace nc
