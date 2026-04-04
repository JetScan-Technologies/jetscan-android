#include "line_bundler.h"
#include <cmath>
#include <algorithm>

namespace nc {

Line segmentToLine(const LineSegment& seg) {
    float dx = seg.x2 - seg.x1;
    float dy = seg.y2 - seg.y1;
    Line l;
    if (std::abs(dx) < 1e-4f) {
        // Vertical line — represent with large slope
        l.slope      = 1e6f;
        l.yIntercept = seg.x1; // store x as intercept for vertical
    } else {
        l.slope      = dy / dx;
        l.yIntercept = seg.y1 - l.slope * seg.x1;
    }
    return l;
}

std::vector<Line> groupSimilarLines(const std::vector<Line>& lines,
                                     float slopeTol, float interceptTol, int limit) {
    std::vector<Line> result;
    std::vector<bool> used(lines.size(), false);
    for (size_t i = 0; i < lines.size() && static_cast<int>(result.size()) < limit; ++i) {
        if (used[i]) continue;
        // Find all lines similar to lines[i]
        float sumSlope = lines[i].slope, sumIntercept = lines[i].yIntercept;
        int count = 1;
        for (size_t j = i + 1; j < lines.size(); ++j) {
            if (used[j]) continue;
            if (std::abs(lines[j].slope - lines[i].slope) < slopeTol &&
                std::abs(lines[j].yIntercept - lines[i].yIntercept) < interceptTol) {
                sumSlope += lines[j].slope;
                sumIntercept += lines[j].yIntercept;
                count++;
                used[j] = true;
            }
        }
        used[i] = true;
        result.push_back({sumSlope / count, sumIntercept / count});
    }
    return result;
}

std::vector<Line> bundleLines(const std::vector<Line>& lines, float minDist, float minAngle) {
    // Simple: group lines that are nearly parallel (angle diff < minAngle)
    // and close (intercept diff < minDist * some_factor)
    std::vector<Line> result;
    std::vector<bool> used(lines.size(), false);
    for (size_t i = 0; i < lines.size(); ++i) {
        if (used[i]) continue;
        float sumSlope = lines[i].slope, sumInt = lines[i].yIntercept;
        int count = 1;
        float ang1 = std::atan(lines[i].slope) * 180.0f / M_PI;
        for (size_t j = i + 1; j < lines.size(); ++j) {
            if (used[j]) continue;
            float ang2 = std::atan(lines[j].slope) * 180.0f / M_PI;
            float angDiff = std::abs(ang1 - ang2);
            float intDiff = std::abs(lines[i].yIntercept - lines[j].yIntercept);
            if (angDiff < minAngle && intDiff < minDist * 10.0f) {
                sumSlope += lines[j].slope;
                sumInt   += lines[j].yIntercept;
                count++;
                used[j] = true;
            }
        }
        used[i] = true;
        result.push_back({sumSlope / count, sumInt / count});
    }
    return result;
}

} // namespace nc
