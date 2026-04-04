#pragma once
#include "../core/image.h"
#include <vector>

namespace nc {

// Simplified MSER region detection (grayscale input).
// Returns keypoint centers of maximally stable extremal regions.
std::vector<Point2f> detectMSER(const Image8& gray);

} // namespace nc
