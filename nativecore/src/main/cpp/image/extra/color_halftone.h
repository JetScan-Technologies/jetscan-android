#pragma once
#include "../core/image.h"

namespace nc {

// Port of ImageFilters.colorHalftone():
// grayscale → adaptiveThreshMean(blockSize=11, C=2)
Image8 colorHalftoneEffect(const Image8& src);

} // namespace nc
