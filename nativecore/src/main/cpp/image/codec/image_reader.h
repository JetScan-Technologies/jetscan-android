#pragma once
#include "../core/image.h"
#include <string>
#include <vector>

namespace nc {

// Auto-detect format from file extension / magic bytes (JPEG, PNG, BMP, TGA, HDR)
Image8 readImage(const std::string& path);
Image8 readImageFromMemory(const uint8_t* data, size_t size);
Image8 readImageFromMemory(const std::vector<uint8_t>& data);

} // namespace nc
