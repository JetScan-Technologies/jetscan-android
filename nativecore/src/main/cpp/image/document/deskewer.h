#pragma once
#include "../core/image.h"

namespace nc {

// Auto-deskew using dominant Hough line angle.
// Detects edges → HoughLines → histogram of angles → dominant angle → rotate
Image8 deskew(const Image8& src);

} // namespace nc
