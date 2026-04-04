#pragma once
#include "../core/image.h"

namespace nc {

// RGB (3 or 4 ch uint8) <-> CIE-L*a*b* (D65 illuminant)
// L in [0,100], a in [-128,127], b in [-128,127]
Image32 rgbToLab(const Image8& src);
Image8 labToRgb(const Image32& lab);

void splitLab(const Image32& lab, Image32& L, Image32& a, Image32& b);
void mergeLab(const Image32& L, const Image32& a, const Image32& b, Image32& dst);

} // namespace nc
