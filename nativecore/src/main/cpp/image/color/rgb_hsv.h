#pragma once
#include "../core/image.h"

namespace nc {

// RGB (3 or 4 ch uint8) <-> HSV (3 ch float: H in [0,360], S in [0,1], V in [0,1])
Image32 rgbToHsv(const Image8& src);
Image8  hsvToRgb(const Image32& hsv);

Image8 extractHsvChannel(const Image8& src, int channel);
Image8 scaleSaturation(const Image8& src, float factor);

void splitHsv(const Image32& hsv, Image32& H, Image32& S, Image32& V);
void mergeHsv(const Image32& H, const Image32& S, const Image32& V, Image32& dst);

} // namespace nc
