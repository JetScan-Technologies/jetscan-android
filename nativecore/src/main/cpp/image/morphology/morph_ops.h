#pragma once
#include "../core/image.h"
#include "structuring_element.h"

namespace nc {

// Opening  = erode → dilate
void morphOpen(const Image8& src, Image8& dst, const StructuringElement& se, int iters = 1);

// Closing  = dilate → erode
void morphClose(const Image8& src, Image8& dst, const StructuringElement& se, int iters = 1);

// Gradient = dilate - erode (edge highlight)
void morphGradient(const Image8& src, Image8& dst, const StructuringElement& se);

// Top-hat  = src - open(src)
void morphTopHat(const Image8& src, Image8& dst, const StructuringElement& se);

// Returning variants
Image8 morphOpen    (const Image8& src, const StructuringElement& se, int iters = 1);
Image8 morphClose   (const Image8& src, const StructuringElement& se, int iters = 1);
Image8 morphGradient(const Image8& src, const StructuringElement& se);

} // namespace nc
