#pragma once
#include "../core/image.h"

namespace nc {

// Generic 2D convolution. kernel is ksize x ksize row-major float array.
void filter2D(const Image8& src, Image8& dst, const float* kernel, int ksize);
Image8 filter2D(const Image8& src, const float* kernel, int ksize);

} // namespace nc
