#include "contour_tracer.h"
#include <cmath>
#include <algorithm>

namespace nc {

namespace {

// 8-connected neighbors, starting from right, going clockwise
static const int dx8[8] = { 1, 1, 0,-1,-1,-1, 0, 1};
static const int dy8[8] = { 0, 1, 1, 1, 0,-1,-1,-1};

// Check if pixel is foreground (non-zero)
static inline bool isFg(const Image8& img, int x, int y) {
    if (x < 0 || x >= img.width || y < 0 || y >= img.height) return false;
    return img.rowPtr(y)[x] != 0;
}

// Trace one contour using Moore boundary tracing
static Contour traceBorder(const Image8& src,
                            std::vector<uint8_t>& visited,
                            int startX, int startY) {
    Contour c;
    const int w = src.width;
    // Find entry direction: look for a background pixel
    int dir = 0;
    for (; dir < 8; ++dir)
        if (!isFg(src, startX + dx8[dir], startY + dy8[dir])) break;

    int cx = startX, cy = startY;
    int backDir = (dir + 4) % 8; // direction back to last background
    do {
        if (visited[cy * w + cx] == 0) {
            visited[cy * w + cx] = 1;
            c.points.push_back({cx, cy});
        }
        // Turn right from backDir
        int newDir = (backDir + 1) % 8;
        int nx, ny;
        for (int i = 0; i < 8; ++i) {
            int d = (newDir + i) % 8;
            nx = cx + dx8[d];
            ny = cy + dy8[d];
            if (isFg(src, nx, ny)) {
                backDir = (d + 4) % 8;
                cx = nx; cy = ny;
                break;
            }
        }
    } while (cx != startX || cy != startY);

    return c;
}

} // anonymous

std::vector<Contour> findContours(const Image8& binary) {
    if (binary.empty() || binary.channels != 1) return {};
    const int w = binary.width, h = binary.height;
    std::vector<uint8_t> visited(static_cast<size_t>(w * h), 0);
    std::vector<Contour> contours;

    for (int y = 1; y < h - 1; ++y) {
        const uint8_t* row  = binary.rowPtr(y);
        const uint8_t* prev = binary.rowPtr(y - 1);
        for (int x = 1; x < w - 1; ++x) {
            // Outer boundary: foreground pixel with background above
            if (row[x] != 0 && prev[x] == 0 && !visited[y * w + x]) {
                Contour c = traceBorder(binary, visited, x, y);
                if (!c.points.empty())
                    contours.push_back(std::move(c));
            }
        }
    }
    return contours;
}

float arcLength(const std::vector<Point2f>& pts, bool closed) {
    float len = 0.0f;
    for (size_t i = 0; i + 1 < pts.size(); ++i) {
        float dx = pts[i+1].x - pts[i].x;
        float dy = pts[i+1].y - pts[i].y;
        len += std::sqrt(dx*dx + dy*dy);
    }
    if (closed && pts.size() > 1) {
        float dx = pts.back().x - pts.front().x;
        float dy = pts.back().y - pts.front().y;
        len += std::sqrt(dx*dx + dy*dy);
    }
    return len;
}

float arcLength(const Contour& c, bool closed) {
    std::vector<Point2f> pts;
    pts.reserve(c.points.size());
    for (auto& p : c.points) pts.push_back({static_cast<float>(p.x), static_cast<float>(p.y)});
    return arcLength(pts, closed);
}

float contourArea(const std::vector<Point2f>& pts) {
    if (pts.size() < 3) return 0.0f;
    float area = 0.0f;
    const int n = static_cast<int>(pts.size());
    for (int i = 0; i < n; ++i) {
        const auto& p1 = pts[i];
        const auto& p2 = pts[(i + 1) % n];
        area += p1.x * p2.y - p2.x * p1.y;
    }
    return std::abs(area) * 0.5f;
}

float contourArea(const Contour& c) {
    std::vector<Point2f> pts;
    pts.reserve(c.points.size());
    for (auto& p : c.points) pts.push_back({static_cast<float>(p.x), static_cast<float>(p.y)});
    return contourArea(pts);
}

} // namespace nc
