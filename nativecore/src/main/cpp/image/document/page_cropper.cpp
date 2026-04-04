#include "page_cropper.h"
#include "../geometry/homography.h"
#include "../geometry/warp_perspective.h"
#include "../geometry/intersection.h"
#include <algorithm>

namespace nc {

Image8 cropDocument(const Image8& src, const Quad& corners) {
    if (src.empty()) return {};

    int outW, outH;
    documentOutputSize(corners, outW, outH);
    if (outW <= 0 || outH <= 0) return {};

    // src points: TL, TR, BR, BL
    Point2f srcPts[4] = {corners.tl, corners.tr, corners.br, corners.bl};
    // dst points: (0,0), (w,0), (w,h), (0,h)
    Point2f dstPts[4] = {
        {0.0f,           0.0f},
        {(float)outW,    0.0f},
        {(float)outW,    (float)outH},
        {0.0f,           (float)outH}
    };

    Mat3f H = computeHomography(srcPts, dstPts);
    return warpPerspective(src, H, outW, outH);
}

} // namespace nc
