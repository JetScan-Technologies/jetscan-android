#include "image_writer.h"
#include <algorithm>
#include <android/log.h>
#include <cctype>

#define TAG "nc::writer"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {
    int stbi_write_jpg(const char*, int, int, int, const void*, int);
    int stbi_write_png(const char*, int, int, int, const void*, int);
    int stbi_write_bmp(const char*, int, int, int, const void*);
    int stbi_write_tga(const char*, int, int, int, const void*);
}

namespace nc {

namespace {

std::string getExtLower(const std::string& path) {
    const size_t dot = path.rfind('.');
    if (dot == std::string::npos) return "";
    std::string ext = path.substr(dot);
    std::transform(ext.begin(), ext.end(), ext.begin(),
                   [](unsigned char c) { return std::tolower(c); });
    return ext;
}

} // namespace

bool writeImage(const Image8& img, const std::string& path, int jpegQuality) {
    if (img.empty()) return false;
    const std::string ext = getExtLower(path);
    const int stride_bytes = img.width * img.channels;
    int ok = 0;
    if (ext == ".jpg" || ext == ".jpeg")
        ok = stbi_write_jpg(path.c_str(), img.width, img.height, img.channels, img.data.data(), jpegQuality);
    else if (ext == ".bmp")
        ok = stbi_write_bmp(path.c_str(), img.width, img.height, img.channels, img.data.data());
    else if (ext == ".tga")
        ok = stbi_write_tga(path.c_str(), img.width, img.height, img.channels, img.data.data());
    else // .png or unknown
        ok = stbi_write_png(path.c_str(), img.width, img.height, img.channels, img.data.data(), stride_bytes);
    if (!ok) LOGE("failed to write '%s'", path.c_str());
    return ok != 0;
}

} // namespace nc
