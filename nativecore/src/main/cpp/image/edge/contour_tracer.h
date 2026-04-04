#pragma once
#include "../core/image.h"
#include <vector>

namespace nc {

struct Contour {
    std::vector<Point2i> points;
};

// Suzuki-85 border-following contour tracing.
// src: binary image (0/255), 1-channel.
// Returns all contours with CHAIN_APPROX_SIMPLE compression.
std::vector<Contour> findContours(const Image8& binary);

// Contour perimeter (arc length)
float arcLength(const Contour& c, bool closed = true);
float arcLength(const std::vector<Point2f>& pts, bool closed = true);

// Signed area via shoelace formula
float contourArea(const Contour& c);
float contourArea(const std::vector<Point2f>& pts);

} // namespace nc
