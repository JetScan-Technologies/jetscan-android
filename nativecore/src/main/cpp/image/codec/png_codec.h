#pragma once
#include "../core/image.h"
#include <vector>
#include <string>

namespace nc {

std::vector<uint8_t> encodePng(const Image8& img);
Image8               decodePng(const uint8_t* data, size_t size);
Image8               decodePng(const std::vector<uint8_t>& data);
bool                 savePng(const Image8& img, const std::string& path);

} // namespace nc
