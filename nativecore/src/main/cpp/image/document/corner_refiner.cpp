#include "corner_refiner.h"
#include "../geometry/intersection.h"
#include <cmath>
#include <algorithm>

namespace nc {

namespace {

static float cornerAngleDeg(const Point2f& a, const Point2f& b, const Point2f& c) {
    float bax = a.x - b.x, bay = a.y - b.y;
    float bcx = c.x - b.x, bcy = c.y - b.y;
    float dot = bax*bcx + bay*bcy;
    float la  = std::sqrt(bax*bax + bay*bay);
    float lc  = std::sqrt(bcx*bcx + bcy*bcy);
    if (la < 1e-4f || lc < 1e-4f) return 0.0f;
    float cosA = std::clamp(dot / (la * lc), -1.0f, 1.0f);
    return std::acos(cosA) * 180.0f / M_PI;
}

// Area of quad via shoelace
static float quadArea(const Quad& q) {
    const Point2f pts[4] = {q.tl, q.tr, q.br, q.bl};
    float area = 0.0f;
    for (int i = 0; i < 4; ++i) {
        const auto& p1 = pts[i];
        const auto& p2 = pts[(i+1)%4];
        area += p1.x * p2.y - p2.x * p1.y;
    }
    return std::abs(area) * 0.5f;
}

} // anonymous

bool isValidQuad(const Quad& q, float minDeg, float maxDeg) {
    const Point2f pts[4] = {q.tl, q.tr, q.br, q.bl};
    for (int i = 0; i < 4; ++i) {
        float ang = cornerAngleDeg(pts[(i-1+4)%4], pts[i], pts[(i+1)%4]);
        if (ang < minDeg || ang > maxDeg) return false;
    }
    return true;
}

std::optional<Quad> findBestQuad(const std::vector<Line>& lines, int imgW, int imgH) {
    const int n = static_cast<int>(lines.size());
    if (n < 4) return std::nullopt;

    // Filter: keep lines that intersect at least one other at [70°, 110°]
    std::vector<bool> keep(n, false);
    for (int i = 0; i < n; ++i) {
        for (int j = 0; j < n; ++j) {
            if (i == j) continue;
            float ang = angleBetweenLines(lines[i], lines[j]);
            if (ang >= 70.0f && ang <= 110.0f) { keep[i] = true; break; }
        }
    }
    std::vector<int> valid;
    for (int i = 0; i < n; ++i) if (keep[i]) valid.push_back(i);
    if (static_cast<int>(valid.size()) < 4) {
        // Fallback: use all lines
        for (int i = 0; i < n; ++i) valid.push_back(i);
    }

    Quad best;
    float bestArea = 0.0f;
    bool found = false;

    // Enumerate 4-line combinations
    const int m = static_cast<int>(valid.size());
    for (int a = 0; a < m-3; ++a)
    for (int b = a+1; b < m-2; ++b)
    for (int c = b+1; c < m-1; ++c)
    for (int d = c+1; d < m; ++d) {
        const Line& l0 = lines[valid[a]];
        const Line& l1 = lines[valid[b]];
        const Line& l2 = lines[valid[c]];
        const Line& l3 = lines[valid[d]];
        // Compute 4 pairwise intersections (l0∩l2, l0∩l3, l1∩l2, l1∩l3)
        // (like pairs of "opposite" line groups)
        auto i02 = lineIntersect(l0, l2);
        auto i03 = lineIntersect(l0, l3);
        auto i12 = lineIntersect(l1, l2);
        auto i13 = lineIntersect(l1, l3);
        if (!i02 || !i03 || !i12 || !i13) continue;
        // Check all 4 points in bounds
        if (!pointInBounds(*i02, imgW, imgH)) continue;
        if (!pointInBounds(*i03, imgW, imgH)) continue;
        if (!pointInBounds(*i12, imgW, imgH)) continue;
        if (!pointInBounds(*i13, imgW, imgH)) continue;
        // Sort as quad
        std::vector<Point2f> pts = {*i02, *i03, *i12, *i13};
        Quad q = sortCorners(pts);
        if (!isValidQuad(q)) continue;
        float area = quadArea(q);
        if (area > bestArea) { bestArea = area; best = q; found = true; }
    }

    if (found) return best;
    return std::nullopt;
}

} // namespace nc
