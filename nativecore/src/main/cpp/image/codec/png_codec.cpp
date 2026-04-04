#include "png_codec.h"
#include <android/log.h>

#define TAG "nc::png"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// stb symbols compiled in jpeg_codec.cpp
extern "C" {
    unsigned char* stbi_load_from_memory(const unsigned char*, int, int*, int*, int*, int);
    void stbi_image_free(void*);
    const char* stbi_failure_reason(void);
    int stbi_write_png_to_func(void (*)(void*, void*, int), void*, int, int, int, const void*, int);
    int stbi_write_png(const char*, int, int, int, const void*, int);
}

namespace nc {

static void writeCb(void* ctx, void* data, int size) {
    auto* buf = static_cast<std::vector<uint8_t>*>(ctx);
    const auto* bytes = static_cast<const uint8_t*>(data);
    buf->insert(buf->end(), bytes, bytes + size);
}

std::vector<uint8_t> encodePng(const Image8& img) {
    if (img.empty()) return {};
    std::vector<uint8_t> out;
    int ok = stbi_write_png_to_func(writeCb, &out, img.width, img.height,
                                    img.channels, img.data.data(),
                                    img.width * img.channels);
    if (!ok) LOGE("stbi_write_png_to_func failed");
    return out;
}

Image8 decodePng(const uint8_t* data, size_t size) {
    int w = 0, h = 0, ch = 0;
    uint8_t* px = stbi_load_from_memory(data, static_cast<int>(size), &w, &h, &ch, 4);
    if (!px) { LOGE("decode failed: %s", stbi_failure_reason()); return {}; }
    Image8 img(px, w, h, 4);
    stbi_image_free(px);
    return img;
}

Image8 decodePng(const std::vector<uint8_t>& data) {
    return decodePng(data.data(), data.size());
}

bool savePng(const Image8& img, const std::string& path) {
    if (img.empty()) return false;
    int ok = stbi_write_png(path.c_str(), img.width, img.height,
                            img.channels, img.data.data(),
                            img.width * img.channels);
    if (!ok) LOGE("failed to write %s", path.c_str());
    return ok != 0;
}

} // namespace nc
