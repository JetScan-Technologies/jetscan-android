#pragma once
#include "../core/image.h"

namespace nc {

// Sobel gradient: 3×3 kernels.
// src: 1-channel grayscale.
// gx, gy: signed gradient (stored as int16 in float32 images)
void sobel(const Image8& src, Image32& gx, Image32& gy);

// Gradient magnitude: sqrt(gx^2 + gy^2), clamped to uint8
void sobelMagnitude(const Image8& src, Image8& mag);
Image8 sobelMagnitude(const Image8& src);

} // namespace nc
