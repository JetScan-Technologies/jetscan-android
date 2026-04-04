#pragma once
#include "../core/image.h"
#include <vector>
#include <string>

namespace nc {

// WebP encode/decode via libwebp
std::vector<uint8_t> encodeWebP(const Image8& img, int quality = 85);
Image8               decodeWebP(const uint8_t* data, size_t size);
Image8               decodeWebP(const std::vector<uint8_t>& data);

} // namespace nc
