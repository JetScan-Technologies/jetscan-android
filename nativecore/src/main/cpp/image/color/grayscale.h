#pragma once
#include "../core/image.h"

namespace nc {

Image8 toGrayscale(const Image8& src);
void toGrayscale(const Image8& src, Image8& dst);

} // namespace nc
