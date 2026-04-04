#pragma once
#include "../core/image.h"

namespace nc {

// Mirror of the Kotlin ImageFilter enum
enum class ImageFilter {
    ORIGINAL,
    VIBRANT,
    NO_SHADOW,
    AUTO,
    COLOR_BUMP,
    GRAYSCALE,
    B_W
};

// Apply a filter effect to src and return the result
Image8 applyEffect(const Image8& src, ImageFilter filter);

// Color adjustment (separate from ImageFilter enum)
// brightness: [-255, 255], contrast: [0,10], saturation: [0,5]
Image8 colorAdjust(const Image8& src, float brightness, float contrast, float saturation);

} // namespace nc
