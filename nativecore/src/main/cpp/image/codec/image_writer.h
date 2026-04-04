#pragma once
#include "../core/image.h"
#include <string>

namespace nc {

// Write image auto-detecting format from path extension
// Supported: .jpg/.jpeg, .png, .bmp, .tga
bool writeImage(const Image8& img, const std::string& path, int jpegQuality = 90);

} // namespace nc
