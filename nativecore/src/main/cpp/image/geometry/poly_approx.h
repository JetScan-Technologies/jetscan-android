#pragma once
#include "../core/image.h"
#include <vector>

namespace nc {

// Ramer-Douglas-Peucker polygon simplification.
// epsilon: max distance from simplified polyline to original points
// Replaces Imgproc.approxPolyDP
std::vector<Point2f> approxPolyDP(const std::vector<Point2f>& contour,
                                   float epsilon, bool closed = true);

// Convenience: convert Contour points to Point2f
std::vector<Point2f> contourToPoints(const std::vector<Point2i>& pts);

// Convexity test
bool isContourConvex(const std::vector<Point2f>& pts);

// Max cosine of angle at each corner (for quad quality check)
float maxCornerCosine(const std::vector<Point2f>& quad);

} // namespace nc
