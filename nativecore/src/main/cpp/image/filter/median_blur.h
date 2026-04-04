#pragma once
#include "../core/image.h"

namespace nc {

// Histogram-based median blur. ksize must be odd >= 3.
void medianBlur(const Image8& src, Image8& dst, int ksize);
Image8 medianBlur(const Image8& src, int ksize);

} // namespace nc
