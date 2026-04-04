#pragma once
#include "../core/image.h"
#include <vector>

namespace nc {

struct HoughLine {
    float rho;   // distance from origin
    float theta; // angle in radians
};

// Standard Hough line transform (polar accumulator).
// src: binary edge image (1-channel).
// rho=1.0, theta=PI/90 (or PI/180), threshold=65
std::vector<HoughLine> houghLines(const Image8& edges,
                                   float rho       = 1.0f,
                                   float theta     = M_PI / 90.0f,
                                   int   threshold = 65);

// Convert polar (rho, theta) to slope-intercept Line
// Returns valid flag: false if line is nearly vertical (undefined slope)
bool houghLineToSlope(const HoughLine& hl, float& slope, float& intercept);

} // namespace nc
