#include "deskewer.h"
#include "../color/grayscale.h"
#include "../edge/canny.h"
#include "../hough/hough_standard.h"
#include "../geometry/warp_affine.h"
#include <cmath>
#include <algorithm>
#include <vector>

namespace nc {

Image8 deskew(const Image8& src) {
    if (src.empty()) return src.clone();

    Image8 gray = toGrayscale(src);
    Image8 edges = canny(gray, 50.0, 150.0);
    auto hlines = houghLines(edges, 1.0f, M_PI/180.0f, 30);
    if (hlines.empty()) return src.clone();

    // Histogram of angles in [-45, 45] degrees (map rho/theta → skew)
    std::vector<float> angles;
    for (const auto& hl : hlines) {
        float deg = hl.theta * 180.0f / M_PI;
        // Normalize to [-90, 90]
        if (deg > 90.0f) deg -= 180.0f;
        if (std::abs(deg) < 45.0f) angles.push_back(deg);
    }
    if (angles.empty()) return src.clone();

    // Median angle as dominant skew
    std::sort(angles.begin(), angles.end());
    float dominantAngle = angles[angles.size() / 2];

    if (std::abs(dominantAngle) < 0.5f) return src.clone();

    // Rotate by -dominantAngle around center
    Point2f center = {src.width / 2.0f, src.height / 2.0f};
    Affine2x3 M = rotationMatrix2D(center, -dominantAngle, 1.0f);
    return warpAffine(src, M, src.width, src.height);
}

} // namespace nc
