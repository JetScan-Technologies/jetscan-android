#pragma once
#include "../core/image.h"
#include "../geometry/quad_utils.h"
#include <vector>
#include <optional>

namespace nc {

// ImageCropCoords mirrors the Kotlin data class
struct ImageCropCoords {
    Point2f topLeft, topRight, bottomLeft, bottomRight;
};

// Path A: detectCorners() — per-channel Canny + contour approach
// Port of DocumentDetection.detectCorners() used in OpenCvManagerImpl.detectDocument()
std::optional<ImageCropCoords> detectCornersA(const Image8& src);

// Path B: detectDocument() — LAB + HoughLinesP approach
// Port of DocumentDetection.detectDocument() / detectSingleDocument()
// Returns up to 50 Hough line segments
std::vector<LineSegment> detectDocumentLines(const Image8& src);

// detectSingleDocument: LineBundler → findBestQuad
std::optional<ImageCropCoords> detectSingleDocument(const Image8& src);

// getLines: detect + groupSimilarLines (for live camera overlay)
std::vector<Line> getLines(const Image8& src);

// Helper: convert Quad → ImageCropCoords
ImageCropCoords quadToCoords(const Quad& q);

} // namespace nc
