#pragma once
#include "../core/image.h"

namespace nc {

// Apply Jet colormap LUT to grayscale image.
// Port of ImageFilters.colorize(COLORMAP_JET)
Image8 applyJetColormap(const Image8& gray);

} // namespace nc
