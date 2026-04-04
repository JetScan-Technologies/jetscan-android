#include "jpeg_codec.h"
#include <android/log.h>

#define TAG "nc::jpeg"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"

#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "stb_image_write.h"

namespace nc {

static void stbiWriteCallback(void* ctx, void* data, int size) {
    auto* buf = static_cast<std::vector<uint8_t>*>(ctx);
    const auto* bytes = static_cast<const uint8_t*>(data);
    buf->insert(buf->end(), bytes, bytes + size);
}

std::vector<uint8_t> encodeJpeg(const Image8& img, int quality) {
    if (img.empty()) return {};
    std::vector<uint8_t> out;
    int ok = stbi_write_jpg_to_func(stbiWriteCallback, &out,
                                    img.width, img.height, img.channels,
                                    img.data.data(), quality);
    if (!ok) { LOGE("stbi_write_jpg failed"); return {}; }
    return out;
}

Image8 decodeJpeg(const uint8_t* data, size_t size) {
    int w = 0, h = 0, ch = 0;
    uint8_t* px = stbi_load_from_memory(data, static_cast<int>(size), &w, &h, &ch, 4);
    if (!px) { LOGE("decode failed: %s", stbi_failure_reason()); return {}; }
    Image8 img(px, w, h, 4);
    stbi_image_free(px);
    return img;
}

Image8 decodeJpeg(const std::vector<uint8_t>& data) {
    return decodeJpeg(data.data(), data.size());
}

bool saveJpeg(const Image8& img, const std::string& path, int quality) {
    if (img.empty()) return false;
    int ok = stbi_write_jpg(path.c_str(), img.width, img.height,
                            img.channels, img.data.data(), quality);
    if (!ok) LOGE("failed to write %s", path.c_str());
    return ok != 0;
}

} // namespace nc
