#include "hough_probabilistic.h"
#include "hough_standard.h"
#include <cmath>
#include <algorithm>
#include <vector>

namespace nc {

// Simple HoughLinesP implementation:
// 1. Run standard Hough to get lines
// 2. For each Hough line, scan the edge image along it to find segments
//    satisfying minLen and maxGap constraints.
std::vector<LineSegment> houghLinesP(const Image8& edges,
                                      float rho, float theta,
                                      int threshold, float minLineLen, float maxLineGap) {
    if (edges.empty() || edges.channels != 1) return {};

    const int w = edges.width, h = edges.height;

    // Get candidate lines from standard Hough
    auto hLines = houghLines(edges, rho, theta, threshold);

    std::vector<LineSegment> result;

    for (const auto& hl : hLines) {
        const float cosT = std::cos(hl.theta);
        const float sinT = std::sin(hl.theta);

        // Sample along the line within image bounds
        // Parametric point: (rho*cosT - t*sinT, rho*sinT + t*cosT) for t in [-maxT, maxT]
        const float maxT = static_cast<float>(std::sqrt((double)w*w + h*h));

        float segX1 = 0, segY1 = 0, segX2 = 0, segY2 = 0;
        float gapLen = 0;
        float segLen = 0;
        bool inSeg = false;

        const int nSteps = static_cast<int>(2.0f * maxT);
        for (int step = 0; step <= nSteps; ++step) {
            float t  = -maxT + step;
            float px = hl.rho * cosT - t * sinT;
            float py = hl.rho * sinT + t * cosT;
            int ix = static_cast<int>(px + 0.5f);
            int iy = static_cast<int>(py + 0.5f);
            bool onEdge = (ix >= 0 && ix < w && iy >= 0 && iy < h &&
                           edges.rowPtr(iy)[ix] != 0);
            if (onEdge) {
                if (!inSeg) {
                    segX1 = px; segY1 = py;
                    inSeg = true;
                    segLen = 0;
                    gapLen = 0;
                } else {
                    gapLen = 0;
                }
                segX2 = px; segY2 = py;
                segLen += 1.0f;
            } else if (inSeg) {
                gapLen += 1.0f;
                if (gapLen > maxLineGap) {
                    if (segLen >= minLineLen) {
                        result.push_back({segX1, segY1, segX2, segY2});
                    }
                    inSeg = false;
                    segLen = 0;
                    gapLen = 0;
                }
            }
        }
        // End of scan
        if (inSeg && segLen >= minLineLen) {
            result.push_back({segX1, segY1, segX2, segY2});
        }
    }

    return result;
}

} // namespace nc
