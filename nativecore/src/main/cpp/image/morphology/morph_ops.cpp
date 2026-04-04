#include "morph_ops.h"
#include "erosion.h"
#include "dilation.h"
#include "../pixel_ops/pixel_ops.h"

namespace nc {

void morphOpen(const Image8& src, Image8& dst, const StructuringElement& se, int iters) {
    Image8 tmp;
    erode(src, tmp, se, iters);
    dilate(tmp, dst, se, iters);
}

void morphClose(const Image8& src, Image8& dst, const StructuringElement& se, int iters) {
    Image8 tmp;
    dilate(src, tmp, se, iters);
    erode(tmp, dst, se, iters);
}

void morphGradient(const Image8& src, Image8& dst, const StructuringElement& se) {
    Image8 dilated, eroded;
    dilate(src, dilated, se);
    erode (src, eroded,  se);
    absDiff(dilated, eroded, dst);
}

void morphTopHat(const Image8& src, Image8& dst, const StructuringElement& se) {
    Image8 opened;
    morphOpen(src, opened, se);
    absDiff(src, opened, dst);
}

Image8 morphOpen(const Image8& src, const StructuringElement& se, int iters) {
    Image8 dst; morphOpen(src, dst, se, iters); return dst;
}

Image8 morphClose(const Image8& src, const StructuringElement& se, int iters) {
    Image8 dst; morphClose(src, dst, se, iters); return dst;
}

Image8 morphGradient(const Image8& src, const StructuringElement& se) {
    Image8 dst; morphGradient(src, dst, se); return dst;
}

} // namespace nc
