#include "color_halftone.h"
#include "../color/grayscale.h"
#include "../threshold/adaptive_threshold.h"

namespace nc {

Image8 colorHalftoneEffect(const Image8& src) {
    Image8 gray = toGrayscale(src);
    return adaptiveThreshMean(gray, 11, 2.0);
}

} // namespace nc
