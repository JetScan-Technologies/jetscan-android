#pragma once
#include "../core/image.h"

namespace nc {

void normalizeMinMax(const Image8& src, Image8& dst,
                     uint8_t outMin = 0, uint8_t outMax = 255);

void normalizeMinMaxF(const Image32& src, Image32& dst,
                      float outMin = 0.0f, float outMax = 1.0f);

void normalizeChannel(Image32& channel, float outMin = 0.0f, float outMax = 1.0f);

} // namespace nc
