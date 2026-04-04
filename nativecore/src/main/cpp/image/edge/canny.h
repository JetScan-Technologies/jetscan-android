#pragma once
#include "../core/image.h"

namespace nc {

// Full Canny edge detector.
// src: 1-channel grayscale.
// lowThresh, highThresh: double threshold values (e.g. 50/150 or 100/200).
// Returns binary edge image (0 or 255).
void canny(const Image8& src, Image8& dst, double lowThresh, double highThresh);
Image8 canny(const Image8& src, double lowThresh, double highThresh);

} // namespace nc
