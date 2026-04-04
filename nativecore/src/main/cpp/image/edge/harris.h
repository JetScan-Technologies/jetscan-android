#pragma once
#include "../core/image.h"

namespace nc {

// Harris corner detection.
// Returns corner response map (Image32) where peaks are corners.
// src: 1-channel grayscale.
// blockSize: neighborhood size (2), apertureSize: Sobel kernel size (3), k: 0.04
void harrisCorners(const Image8& src, Image32& dst,
                   int blockSize = 2, int apertureSize = 3, float k = 0.04f);
Image32 harrisCorners(const Image8& src, int blockSize = 2, float k = 0.04f);

} // namespace nc
