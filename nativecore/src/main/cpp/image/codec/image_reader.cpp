#include "image_reader.h"
#include <android/log.h>

#define TAG "nc::reader"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {
    unsigned char* stbi_load(const char*, int*, int*, int*, int);
    unsigned char* stbi_load_from_memory(const unsigned char*, int, int*, int*, int*, int);
    void stbi_image_free(void*);
    const char* stbi_failure_reason(void);
}

namespace nc {

Image8 readImage(const std::string& path) {
    int w = 0, h = 0, ch = 0;
    uint8_t* px = stbi_load(path.c_str(), &w, &h, &ch, 4);
    if (!px) { LOGE("failed to load '%s': %s", path.c_str(), stbi_failure_reason()); return {}; }
    Image8 img(px, w, h, 4);
    stbi_image_free(px);
    return img;
}

Image8 readImageFromMemory(const uint8_t* data, size_t size) {
    int w = 0, h = 0, ch = 0;
    uint8_t* px = stbi_load_from_memory(data, static_cast<int>(size), &w, &h, &ch, 4);
    if (!px) { LOGE("decode failed: %s", stbi_failure_reason()); return {}; }
    Image8 img(px, w, h, 4);
    stbi_image_free(px);
    return img;
}

Image8 readImageFromMemory(const std::vector<uint8_t>& data) {
    return readImageFromMemory(data.data(), data.size());
}

} // namespace nc
