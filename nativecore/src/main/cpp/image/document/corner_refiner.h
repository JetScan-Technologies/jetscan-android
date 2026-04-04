#pragma once
#include "../core/image.h"
#include "../geometry/quad_utils.h"
#include <vector>
#include <optional>

namespace nc {

// Find best quadrilateral from a set of lines.
// Port of OpenCvManagerImpl.findBestQuadrilaterals():
//   - filter: only keep lines that intersect another at [70°, 110°]
//   - enumerate 4-line combos, compute 4 intersections
//   - validate: all angles in [60°, 120°], all points in bounds
//   - return quad with largest area
std::optional<Quad> findBestQuad(const std::vector<Line>& lines, int imgW, int imgH);

// Check if a 4-corner quad has all corners at valid angles [minDeg, maxDeg]
bool isValidQuad(const Quad& q, float minDeg = 60.0f, float maxDeg = 120.0f);

} // namespace nc
