#pragma once
#include "../core/image.h"
#include <vector>

namespace nc {

// Convert LineSegment to slope-intercept Line
Line segmentToLine(const LineSegment& seg);

// Group similar lines: collapse lines where |slopeDiff| < slopeTol AND |interceptDiff| < interceptTol
// Matches groupSimilarLines() from OpenCvManagerImpl
std::vector<Line> groupSimilarLines(const std::vector<Line>& lines,
                                     float slopeTol     = 30.0f,
                                     float interceptTol = 30.0f,
                                     int   limit        = 30);

// Bundle close/parallel LineSegments (merge nearby lines)
// Matches LineBundler(minDist=10, minAngle=30)
std::vector<Line> bundleLines(const std::vector<Line>& lines,
                               float minDist  = 10.0f,
                               float minAngle = 30.0f);

} // namespace nc
