#include "image.h"
#include <cmath>

namespace nc {

float LineSegment::length() const {
    float dx = x2 - x1, dy = y2 - y1;
    return std::sqrt(dx * dx + dy * dy);
}

float Quad::area() const {
    // Shoelace formula for quadrilateral
    auto cross = [](Point2f a, Point2f b, Point2f c) -> float {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    };
    float a1 = std::abs(cross(tl, tr, br));
    float a2 = std::abs(cross(tl, br, bl));
    return (a1 + a2) * 0.5f;
}

} // namespace nc
