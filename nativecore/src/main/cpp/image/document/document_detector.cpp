#include "document_detector.h"
#include "../color/grayscale.h"
#include "../color/rgb_lab.h"
#include "../channel_ops/channel_ops.h"
#include "../filter/median_blur.h"
#include "../filter/gaussian_blur.h"
#include "../morphology/morph_ops.h"
#include "../morphology/erosion.h"
#include "../morphology/dilation.h"
#include "../morphology/structuring_element.h"
#include "../pixel_ops/pixel_ops.h"
#include "../threshold/global_threshold.h"
#include "../edge/canny.h"
#include "../edge/contour_tracer.h"
#include "../hough/hough_probabilistic.h"
#include "../hough/hough_standard.h"
#include "../hough/line_bundler.h"
#include "../geometry/poly_approx.h"
#include "../geometry/intersection.h"
#include "../geometry/quad_utils.h"
#include "corner_refiner.h"
#include <cmath>
#include <algorithm>
#include <android/log.h>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define TAG "nc::document_detector"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

namespace nc {

ImageCropCoords quadToCoords(const Quad& q) {
    return {q.tl, q.tr, q.bl, q.br};
}

// ── Path A: detectCorners() ────────────────────────────────────────────────
std::optional<ImageCropCoords> detectCornersA(const Image8& src) {
    if (src.empty()) return std::nullopt;
    const int w = src.width, h = src.height;
    const int minArea = (w/5) * (h/5);

    // medianBlur(9)
    Image8 blurred = medianBlur(src, 9);

    std::optional<Quad> bestQuad;
    float bestArea = 0.0f;

    // Per-channel processing
    for (int ch = 0; ch < 3; ++ch) {
        Image8 channel(w, h, 1);
        extractChannel(blurred, channel, ch);

        // Canny mode: t = 10, 20, 30, 40, 50, 60
        for (int t = 10; t <= 60; t += 10) {
            Image8 edges = canny(channel, t, t * 2.0);
            // dilate 2 iterations with default rect SE
            Image8 dilated = dilate(edges, makeRect(3), 2);

            auto contours = findContours(dilated);
            for (auto& cnt : contours) {
                auto pts = contourToPoints(cnt.points);
                float peri = arcLength(pts, true);
                auto approx = approxPolyDP(pts, peri * 0.03f, true);
                if (approx.size() != 4) continue;
                float area = contourArea(approx);
                if (area < minArea) continue;
                if (!isContourConvex(approx)) continue;
                if (maxCornerCosine(approx) > 0.5f) continue;
                if (area > bestArea) {
                    bestArea = area;
                    bestQuad = sortCorners(approx);
                }
            }
        }

        // Threshold mode: l = 1, 2
        for (int l = 1; l <= 2; ++l) {
            float thresh = 200.0f - 175.0f / (l + 2.0f);
            Image8 threshImg = threshold(channel, thresh, 255.0);
            auto contours = findContours(threshImg);
            for (auto& cnt : contours) {
                auto pts = contourToPoints(cnt.points);
                float peri = arcLength(pts, true);
                auto approx = approxPolyDP(pts, peri * 0.03f, true);
                if (approx.size() != 4) continue;
                float area = contourArea(approx);
                if (area < minArea) continue;
                if (!isContourConvex(approx)) continue;
                if (maxCornerCosine(approx) > 0.5f) continue;
                if (area > bestArea) {
                    bestArea = area;
                    bestQuad = sortCorners(approx);
                }
            }
        }
    }

    if (bestQuad) return quadToCoords(*bestQuad);
    return std::nullopt;
}

// ── Path B: detectDocumentLines() ─────────────────────────────────────────
std::vector<LineSegment> detectDocumentLines(const Image8& src) {
    if (src.empty()) return {};
    const int w = src.width;

    // cvtColor(RGB→Lab)
    Image32 lab = rgbToLab(src);
    Image32 L, a, b;
    splitLab(lab, L, a, b);

    // morphClose(L, ellipse_7)
    // (L is float; we need uint8 — scale L [0,100] to [0,255])
    Image8 L8(L.width, L.height, 1);
    for (int y = 0; y < L.height; ++y) {
        const float* lr = L.rowPtr(y);
        uint8_t* dr = L8.rowPtr(y);
        for (int x = 0; x < L.width; ++x)
            dr[x] = static_cast<uint8_t>(std::clamp(lr[x] * 2.55f, 0.0f, 255.0f));
    }

    auto se7 = makeEllipse(7);
    auto se3 = makeEllipse(3);
    Image8 Lc = morphClose(L8, se7);

    // morphErode(a, ellipse_3)
    Image8 a8(a.width, a.height, 1);
    for (int y = 0; y < a.height; ++y) {
        const float* ar = a.rowPtr(y);
        uint8_t* dr = a8.rowPtr(y);
        for (int x = 0; x < a.width; ++x)
            dr[x] = static_cast<uint8_t>(std::clamp((ar[x] + 128.0f), 0.0f, 255.0f));
    }
    Image8 ae = erode(a8, se3);

    // merge [L, a, b] (use Lc and ae) as uint8 3-channel
    Image8 merged(L.width, L.height, 3);
    for (int y = 0; y < L.height; ++y) {
        uint8_t* dr = merged.rowPtr(y);
        const uint8_t* lr = Lc.rowPtr(y);
        const uint8_t* ar = ae.rowPtr(y);
        // b channel (keep unchanged)
        const float* br = b.rowPtr(y);
        for (int x = 0; x < L.width; ++x) {
            dr[x*3+0] = lr[x];
            dr[x*3+1] = ar[x];
            dr[x*3+2] = static_cast<uint8_t>(std::clamp((br[x] + 128.0f), 0.0f, 255.0f));
        }
    }

    // morphGradient(merged, ellipse_3)
    Image8 gradient = morphGradient(merged, se3);

    // extractChannel(gradient, 0) = red channel (L gradient)
    Image8 redChannel(gradient.width, gradient.height, 1);
    extractChannel(gradient, redChannel, 0);

    // morphDilate(redChannel, ellipse_3)
    Image8 dilated = dilate(redChannel, se3);

    // Canny(50, 150)
    Image8 edges = canny(dilated, 50.0, 150.0);

    // HoughLinesP(rho=1, theta=PI/180, thresh=65, minLen=50, maxGap=60)
    auto segs = houghLinesP(edges, 1.0f, M_PI/180.0f, 65, 50.0f, 60.0f);

    // filter: length >= width * 0.6, x/y in bounds
    const float minLen = w * 0.6f;
    const int imgW = src.width, imgH = src.height;
    std::vector<LineSegment> result;
    for (auto& seg : segs) {
        if (seg.length() < minLen) continue;
        // Check at least one endpoint in bounds
        bool ok = (seg.x1 >= 0 && seg.x1 < imgW && seg.y1 >= 0 && seg.y1 < imgH) ||
                  (seg.x2 >= 0 && seg.x2 < imgW && seg.y2 >= 0 && seg.y2 < imgH);
        if (ok) result.push_back(seg);
    }
    // Return top 50
    if (result.size() > 50) result.resize(50);
    return result;
}

// ── detectSingleDocument ─────────────────────────────────────────────────
std::optional<ImageCropCoords> detectSingleDocument(const Image8& src) {
    auto segs = detectDocumentLines(src);
    if (segs.empty()) return std::nullopt;

    // Convert segments to Lines
    std::vector<Line> lines;
    for (auto& seg : segs) lines.push_back(segmentToLine(seg));

    // Bundle lines
    auto bundled = bundleLines(lines, 10.0f, 30.0f);

    // Try to find best quad
    auto quad = findBestQuad(bundled, src.width, src.height);
    if (quad) return quadToCoords(*quad);

    // Fallback: if exactly 4 bundled lines, intersect them
    if (bundled.size() == 4) {
        auto q = findBestQuad(bundled, src.width, src.height);
        if (q) return quadToCoords(*q);
    }
    return std::nullopt;
}

// ── getLines ─────────────────────────────────────────────────────────────
std::vector<Line> getLines(const Image8& src) {
    if (src.empty()) return {};

    // bitwiseNot
    Image8 inv;
    bitwiseNot(src, inv);

    // saturationChannel (HSV S channel) — simplified: use grayscale
    Image8 gray = toGrayscale(inv);

    // GaussianBlur(7)
    Image8 blurred = gaussianBlur(gray, 7, 0.0f);

    // Canny(100, 200)
    Image8 edges = canny(blurred, 100.0, 200.0);

    // HoughLines(rho=1, theta=PI/90, threshold=65)
    auto hlines = houghLines(edges, 1.0f, M_PI/90.0f, 65);

    // Convert to slope-intercept Lines
    std::vector<Line> lines;
    for (auto& hl : hlines) {
        float slope, intercept;
        if (houghLineToSlope(hl, slope, intercept))
            lines.push_back({slope, intercept});
    }

    // groupSimilarLines(slopeDiff<30, interceptDiff<30, limit=30)
    return groupSimilarLines(lines, 30.0f, 30.0f, 30);
}

} // namespace nc
