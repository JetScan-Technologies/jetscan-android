#pragma once
#include "../core/image.h"

namespace nc {

// Fixed binary threshold: dst(x,y) = (src(x,y) > thresh) ? maxVal : 0
// src must be 1-channel.
void threshold(const Image8& src, Image8& dst, double thresh, double maxVal = 255.0);
Image8 threshold(const Image8& src, double thresh, double maxVal = 255.0);

} // namespace nc
