#pragma once
#include "../core/image.h"
#include <optional>

namespace nc {

// Line-line intersection in 2D (slope-intercept form)
// Returns empty optional if lines are parallel
std::optional<Point2f> lineIntersect(const Line& l1, const Line& l2);

// Angle between two lines in degrees [0, 90]
float angleBetweenLines(const Line& l1, const Line& l2);

// Check if two lines are parallel (angle diff < tol degrees)
bool isParallel(const Line& l1, const Line& l2, float tolDeg = 5.0f);

// Euclidean distance between two points
float pointDistance(const Point2f& a, const Point2f& b);

// Check if point is within image bounds
bool pointInBounds(const Point2f& p, int width, int height);

} // namespace nc
