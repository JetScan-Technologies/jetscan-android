#pragma once
#include "../core/image.h"

namespace nc {

// Adaptive threshold using local Gaussian-weighted mean.
// blockSize: odd (e.g. 11, 15). C: constant subtracted from mean.
// Output: binary image (0 or 255). src must be 1-channel.
void adaptiveThreshGaussian(const Image8& src, Image8& dst, int blockSize, double C);

// Adaptive threshold using plain box mean.
void adaptiveThreshMean(const Image8& src, Image8& dst, int blockSize, double C);

// Returning variants
Image8 adaptiveThreshGaussian(const Image8& src, int blockSize, double C);
Image8 adaptiveThreshMean    (const Image8& src, int blockSize, double C);

} // namespace nc
