#pragma once
#include "../core/image.h"
#include "structuring_element.h"

namespace nc {

void dilate(const Image8& src, Image8& dst, const StructuringElement& se, int iterations = 1);
Image8 dilate(const Image8& src, const StructuringElement& se, int iterations = 1);

void dilateRect(const Image8& src, Image8& dst, int ksize, int iterations = 1);

} // namespace nc
