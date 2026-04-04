#pragma once
#include "../core/image.h"

namespace nc {

void convertToFloat(const Image8& src, Image32& dst);
void convertToUint8(const Image32& src, Image8& dst);
void convertToUint8Scaled(const Image32& src, Image8& dst, float scale = 255.0f);

} // namespace nc
