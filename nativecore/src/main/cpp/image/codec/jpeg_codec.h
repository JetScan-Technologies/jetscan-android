#pragma once
#include "../core/image.h"
#include <vector>
#include <string>

namespace nc {

// JPEG encode/decode via stb_image (libjpeg-turbo not available as NDK system lib,
// so we use stb for portability; quality 0-100)
std::vector<uint8_t> encodeJpeg(const Image8& img, int quality = 90);
Image8               decodeJpeg(const uint8_t* data, size_t size);
Image8               decodeJpeg(const std::vector<uint8_t>& data);
bool                 saveJpeg(const Image8& img, const std::string& path, int quality = 90);

} // namespace nc
