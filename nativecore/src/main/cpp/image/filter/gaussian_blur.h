#pragma once
#include "../core/image.h"

namespace nc {

// Separable Gaussian blur. ksize must be odd >= 3. sigma 0 = auto from ksize.
void gaussianBlur(const Image8& src, Image8& dst, int ksize, float sigma = 0.0f);
Image8 gaussianBlur(const Image8& src, int ksize, float sigma = 0.0f);

} // namespace nc
