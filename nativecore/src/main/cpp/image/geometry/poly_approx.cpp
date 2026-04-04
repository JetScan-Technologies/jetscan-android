#include "poly_approx.h"
#include <cmath>
#include <algorithm>

namespace nc {

namespace {

static float pointToSegmentDist(const Point2f& p, const Point2f& a, const Point2f& b) {
    float dx = b.x - a.x, dy = b.y - a.y;
    float len2 = dx*dx + dy*dy;
    if (len2 < 1e-8f) {
        float ex = p.x - a.x, ey = p.y - a.y;
        return std::sqrt(ex*ex + ey*ey);
    }
    float t = ((p.x - a.x)*dx + (p.y - a.y)*dy) / len2;
    t = std::clamp(t, 0.0f, 1.0f);
    float projX = a.x + t*dx, projY = a.y + t*dy;
    float ex = p.x - projX, ey = p.y - projY;
    return std::sqrt(ex*ex + ey*ey);
}

static void rdp(const std::vector<Point2f>& pts, int start, int end,
                float epsilon, std::vector<bool>& keep) {
    if (start >= end - 1) return;
    float maxDist = 0;
    int maxIdx    = start;
    for (int i = start + 1; i < end; ++i) {
        float d = pointToSegmentDist(pts[i], pts[start], pts[end]);
        if (d > maxDist) { maxDist = d; maxIdx = i; }
    }
    if (maxDist > epsilon) {
        keep[maxIdx] = true;
        rdp(pts, start, maxIdx, epsilon, keep);
        rdp(pts, maxIdx, end, epsilon, keep);
    }
}

} // anonymous

std::vector<Point2f> approxPolyDP(const std::vector<Point2f>& contour,
                                   float epsilon, bool closed) {
    if (contour.size() < 2) return contour;
    const int n = static_cast<int>(contour.size());
    std::vector<bool> keep(n, false);
    keep[0] = true;
    keep[n-1] = true;
    rdp(contour, 0, n - 1, epsilon, keep);
    if (closed) {
        // Also run from last to first
        rdp(contour, 0, n - 1, epsilon, keep);
    }
    std::vector<Point2f> result;
    for (int i = 0; i < n; ++i)
        if (keep[i]) result.push_back(contour[i]);
    return result;
}

std::vector<Point2f> contourToPoints(const std::vector<Point2i>& pts) {
    std::vector<Point2f> out;
    out.reserve(pts.size());
    for (auto& p : pts) out.push_back({static_cast<float>(p.x), static_cast<float>(p.y)});
    return out;
}

bool isContourConvex(const std::vector<Point2f>& pts) {
    if (pts.size() < 3) return false;
    const int n = static_cast<int>(pts.size());
    int sign = 0;
    for (int i = 0; i < n; ++i) {
        const auto& a = pts[i];
        const auto& b = pts[(i + 1) % n];
        const auto& c = pts[(i + 2) % n];
        float cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);
        int s = (cross > 0) ? 1 : (cross < 0) ? -1 : 0;
        if (s != 0) {
            if (sign == 0) sign = s;
            else if (sign != s) return false;
        }
    }
    return true;
}

float maxCornerCosine(const std::vector<Point2f>& quad) {
    const int n = static_cast<int>(quad.size());
    float maxCos = 0.0f;
    for (int i = 0; i < n; ++i) {
        const auto& a = quad[(i - 1 + n) % n];
        const auto& b = quad[i];
        const auto& c = quad[(i + 1) % n];
        float bax = a.x - b.x, bay = a.y - b.y;
        float bcx = c.x - b.x, bcy = c.y - b.y;
        float dot  = bax*bcx + bay*bcy;
        float la   = std::sqrt(bax*bax + bay*bay);
        float lc   = std::sqrt(bcx*bcx + bcy*bcy);
        if (la < 1e-6f || lc < 1e-6f) continue;
        float cosA = std::abs(dot / (la * lc));
        if (cosA > maxCos) maxCos = cosA;
    }
    return maxCos;
}

} // namespace nc
