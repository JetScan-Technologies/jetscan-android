#include "intersection.h"
#include <cmath>
#include <algorithm>

namespace nc {

std::optional<Point2f> lineIntersect(const Line& l1, const Line& l2) {
    // y = m1*x + b1, y = m2*x + b2
    // x = (b2 - b1) / (m1 - m2)
    float dm = l1.slope - l2.slope;
    if (std::abs(dm) < 1e-6f) return std::nullopt; // parallel
    float x = (l2.yIntercept - l1.yIntercept) / dm;
    float y = l1.slope * x + l1.yIntercept;
    return Point2f{x, y};
}

float angleBetweenLines(const Line& l1, const Line& l2) {
    float ang1 = std::atan(l1.slope) * 180.0f / M_PI;
    float ang2 = std::atan(l2.slope) * 180.0f / M_PI;
    float diff = std::abs(ang1 - ang2);
    if (diff > 90.0f) diff = 180.0f - diff;
    return diff;
}

bool isParallel(const Line& l1, const Line& l2, float tolDeg) {
    return angleBetweenLines(l1, l2) < tolDeg;
}

float pointDistance(const Point2f& a, const Point2f& b) {
    float dx = a.x - b.x, dy = a.y - b.y;
    return std::sqrt(dx*dx + dy*dy);
}

bool pointInBounds(const Point2f& p, int width, int height) {
    return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
}

} // namespace nc
