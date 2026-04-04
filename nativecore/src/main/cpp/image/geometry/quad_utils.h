#pragma once
#include "../core/image.h"
#include <array>
#include <vector>

namespace nc {

// Order 4 points as TL, TR, BR, BL
// Implements getOrderedPoints() from DocumentDetection.kt:
//   - two leftmost = left side, sorted by y → TL (top), BL (bottom)
//   - two rightmost = right side, sorted by y → TR (top), BR (bottom)
Quad sortCorners(const std::array<Point2f, 4>& pts);
Quad sortCorners(const std::vector<Point2f>& pts); // first 4 used

// Output size of perspective-corrected document:
// width  = max(dist(TL,TR), dist(BL,BR))
// height = max(dist(TL,BL), dist(TR,BR))
void documentOutputSize(const Quad& q, int& outW, int& outH);

} // namespace nc
