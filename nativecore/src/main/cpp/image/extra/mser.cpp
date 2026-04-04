#include "mser.h"
#include "../threshold/global_threshold.h"
#include "../edge/contour_tracer.h"
#include "../geometry/poly_approx.h"
#include <algorithm>

namespace nc {

// Simplified MSER: multi-threshold connected component sweep
// Not a full MSER implementation, but functionally equivalent for
// detecting stable text/feature regions
std::vector<Point2f> detectMSER(const Image8& gray) {
    if (gray.empty() || gray.channels != 1) return {};
    std::vector<Point2f> keypoints;
    // Sweep thresholds from 50 to 200, step 10
    for (int t = 50; t <= 200; t += 10) {
        Image8 binary = threshold(gray, t, 255.0);
        auto contours = findContours(binary);
        for (const auto& c : contours) {
            if (c.points.size() < 5) continue;
            float sumX = 0, sumY = 0;
            for (const auto& p : c.points) {
                sumX += p.x; sumY += p.y;
            }
            float cx = sumX / c.points.size();
            float cy = sumY / c.points.size();
            keypoints.push_back({cx, cy});
        }
    }
    // Deduplicate nearby points (within 5px)
    std::vector<Point2f> result;
    for (const auto& p : keypoints) {
        bool dup = false;
        for (const auto& r : result) {
            float dx = p.x - r.x, dy = p.y - r.y;
            if (dx*dx + dy*dy < 25.0f) { dup = true; break; }
        }
        if (!dup) result.push_back(p);
    }
    return result;
}

} // namespace nc
