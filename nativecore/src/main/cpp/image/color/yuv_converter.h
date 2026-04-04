#pragma once
#include "../core/image.h"

namespace nc {

Image8 nv21ToRgba(const uint8_t* nv21, int width, int height);

Image8 yuv420ToRgba(const uint8_t* yPlane,  int yStride,
                    const uint8_t* uvPlane, int uvStride,
                    int width, int height);

} // namespace nc
