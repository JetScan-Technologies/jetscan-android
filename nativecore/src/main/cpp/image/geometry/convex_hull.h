#pragma once
#include "../core/image.h"
#include <vector>

namespace nc {

// Graham scan convex hull
std::vector<Point2f> convexHull(const std::vector<Point2f>& pts);
std::vector<int>     convexHullIndices(const std::vector<Point2i>& pts);

} // namespace nc
