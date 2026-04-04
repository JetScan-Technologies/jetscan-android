#pragma once
#include "../core/image.h"
#include <vector>

namespace nc {

// Probabilistic Hough line transform (HoughLinesP).
// rho=1, theta=PI/180, threshold=65, minLen=50, maxGap=60
std::vector<LineSegment> houghLinesP(const Image8& edges,
                                      float rho        = 1.0f,
                                      float theta      = M_PI / 180.0f,
                                      int   threshold  = 65,
                                      float minLineLen = 50.0f,
                                      float maxLineGap = 60.0f);

} // namespace nc
