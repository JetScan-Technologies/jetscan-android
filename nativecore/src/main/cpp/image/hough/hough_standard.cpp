#include "hough_standard.h"
#include <cmath>
#include <vector>
#include <algorithm>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

namespace nc {

std::vector<HoughLine> houghLines(const Image8& edges,
                                   float rho, float theta, int threshold) {
    if (edges.empty() || edges.channels != 1) return {};
    const int w = edges.width, h = edges.height;
    const int numAngles = static_cast<int>(M_PI / theta + 0.5f);
    const int maxRho    = static_cast<int>(std::sqrt((double)w*w + h*h) + 0.5);
    const int numRho    = 2 * maxRho + 1;

    // Precompute sin/cos LUT
    std::vector<float> sinLut(numAngles), cosLut(numAngles);
    for (int i = 0; i < numAngles; ++i) {
        float ang = i * theta;
        sinLut[i] = std::sin(ang);
        cosLut[i] = std::cos(ang);
    }

    // Accumulator
    std::vector<int> acc(static_cast<size_t>(numAngles) * numRho, 0);

    // Vote
    for (int y = 0; y < h; ++y) {
        const uint8_t* row = edges.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            if (row[x] == 0) continue;
            for (int ai = 0; ai < numAngles; ++ai) {
                float r = x * cosLut[ai] + y * sinLut[ai];
                int ri = static_cast<int>(r / rho + maxRho + 0.5f);
                if (ri >= 0 && ri < numRho)
                    acc[ai * numRho + ri]++;
            }
        }
    }

    // Extract peaks above threshold (simple local-max suppression)
    std::vector<HoughLine> lines;
    for (int ai = 0; ai < numAngles; ++ai) {
        for (int ri = 0; ri < numRho; ++ri) {
            int v = acc[ai * numRho + ri];
            if (v < threshold) continue;
            // Local maximum check (3x3)
            bool isMax = true;
            for (int da = -1; da <= 1 && isMax; ++da) {
                for (int dr = -1; dr <= 1 && isMax; ++dr) {
                    if (da == 0 && dr == 0) continue;
                    int na = ai + da, nr = ri + dr;
                    if (na >= 0 && na < numAngles && nr >= 0 && nr < numRho)
                        if (acc[na * numRho + nr] > v) isMax = false;
                }
            }
            if (isMax) {
                HoughLine hl;
                hl.rho   = (ri - maxRho) * rho;
                hl.theta = ai * theta;
                lines.push_back(hl);
            }
        }
    }
    return lines;
}

bool houghLineToSlope(const HoughLine& hl, float& slope, float& intercept) {
    const float sinT = std::sin(hl.theta);
    const float cosT = std::cos(hl.theta);
    if (std::abs(sinT) < 1e-4f) return false; // nearly vertical
    slope     = -cosT / sinT;
    intercept = hl.rho / sinT;
    return true;
}

} // namespace nc
