#include "convex_hull.h"
#include <algorithm>
#include <cmath>

namespace nc {

static float cross2D(const Point2f& O, const Point2f& A, const Point2f& B) {
    return (A.x - O.x) * (B.y - O.y) - (A.y - O.y) * (B.x - O.x);
}

std::vector<Point2f> convexHull(const std::vector<Point2f>& pts) {
    int n = static_cast<int>(pts.size());
    if (n < 3) return pts;
    // Andrew's monotone chain algorithm
    std::vector<Point2f> sorted = pts;
    std::sort(sorted.begin(), sorted.end(),
              [](const Point2f& a, const Point2f& b) {
                  return a.x < b.x || (a.x == b.x && a.y < b.y);
              });
    std::vector<Point2f> hull;
    // Lower hull
    for (auto& p : sorted) {
        while (hull.size() >= 2 && cross2D(hull[hull.size()-2], hull.back(), p) <= 0)
            hull.pop_back();
        hull.push_back(p);
    }
    // Upper hull
    const int lower_size = static_cast<int>(hull.size());
    for (int i = n - 2; i >= 0; --i) {
        while (static_cast<int>(hull.size()) > lower_size &&
               cross2D(hull[hull.size()-2], hull.back(), sorted[i]) <= 0)
            hull.pop_back();
        hull.push_back(sorted[i]);
    }
    hull.pop_back(); // remove duplicated start point
    return hull;
}

std::vector<int> convexHullIndices(const std::vector<Point2i>& pts) {
    int n = static_cast<int>(pts.size());
    std::vector<int> idx(n);
    for (int i = 0; i < n; ++i) idx[i] = i;
    std::sort(idx.begin(), idx.end(), [&](int a, int b) {
        return pts[a].x < pts[b].x || (pts[a].x == pts[b].x && pts[a].y < pts[b].y);
    });
    auto cross = [&](int o, int a, int b) {
        return (long long)(pts[a].x - pts[o].x) * (pts[b].y - pts[o].y)
             - (long long)(pts[a].y - pts[o].y) * (pts[b].x - pts[o].x);
    };
    std::vector<int> hull;
    for (int i = 0; i < n; ++i) {
        while (hull.size() >= 2 && cross(hull[hull.size()-2], hull.back(), idx[i]) <= 0)
            hull.pop_back();
        hull.push_back(idx[i]);
    }
    int lower = static_cast<int>(hull.size());
    for (int i = n - 2; i >= 0; --i) {
        while (static_cast<int>(hull.size()) > lower &&
               cross(hull[hull.size()-2], hull.back(), idx[i]) <= 0)
            hull.pop_back();
        hull.push_back(idx[i]);
    }
    hull.pop_back();
    return hull;
}

} // namespace nc
