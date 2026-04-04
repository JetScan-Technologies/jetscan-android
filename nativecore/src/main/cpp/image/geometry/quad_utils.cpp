#include "quad_utils.h"
#include "intersection.h"
#include <algorithm>
#include <cmath>

namespace nc {

Quad sortCorners(const std::array<Point2f, 4>& pts) {
    // Sort by x to find left/right pairs
    auto sorted = pts;
    std::sort(sorted.begin(), sorted.end(),
              [](const Point2f& a, const Point2f& b) { return a.x < b.x; });
    // Left two
    Point2f left1 = sorted[0], left2 = sorted[1];
    // Right two
    Point2f right1 = sorted[2], right2 = sorted[3];
    // Sort each pair by y
    Point2f tl = (left1.y  <= left2.y)  ? left1  : left2;
    Point2f bl = (left1.y  <= left2.y)  ? left2  : left1;
    Point2f tr = (right1.y <= right2.y) ? right1 : right2;
    Point2f br = (right1.y <= right2.y) ? right2 : right1;
    return {tl, tr, br, bl};
}

Quad sortCorners(const std::vector<Point2f>& pts) {
    if (pts.size() < 4) return {};
    std::array<Point2f, 4> arr = {pts[0], pts[1], pts[2], pts[3]};
    return sortCorners(arr);
}

void documentOutputSize(const Quad& q, int& outW, int& outH) {
    float wTop  = pointDistance(q.tl, q.tr);
    float wBot  = pointDistance(q.bl, q.br);
    float hLeft = pointDistance(q.tl, q.bl);
    float hRight= pointDistance(q.tr, q.br);
    outW = static_cast<int>(std::max(wTop, wBot));
    outH = static_cast<int>(std::max(hLeft, hRight));
}

} // namespace nc
