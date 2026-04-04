#include "webp_codec.h"
#include <android/log.h>

#define TAG "nc::webp"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#if __has_include("webp/encode.h")
#  include "webp/encode.h"
#  include "webp/decode.h"
#  define NC_HAS_WEBP 1
#elif __has_include("src/webp/encode.h")
#  include "src/webp/encode.h"
#  include "src/webp/decode.h"
#  define NC_HAS_WEBP 1
#else
#  define NC_HAS_WEBP 0
#endif

namespace nc {

std::vector<uint8_t> encodeWebP(const Image8& img, int quality) {
    if (img.empty()) return {};
#if NC_HAS_WEBP
    uint8_t* output = nullptr;
    size_t   size   = 0;
    // WebP expects stride in bytes; for uint8_t images, stride (elements) == stride (bytes)
    const int stride_bytes = img.width * img.channels;
    if (img.channels == 4)
        size = WebPEncodeRGBA(img.data.data(), img.width, img.height, stride_bytes, quality, &output);
    else if (img.channels == 3)
        size = WebPEncodeRGB(img.data.data(), img.width, img.height, stride_bytes, quality, &output);
    else {
        LOGE("unsupported channels=%d", img.channels);
        return {};
    }
    if (!output || size == 0) {
        if (output) WebPFree(output);
        LOGE("WebPEncode failed");
        return {};
    }
    std::vector<uint8_t> result(output, output + size);
    WebPFree(output);
    return result;
#else
    LOGE("libwebp not available");
    return {};
#endif
}

Image8 decodeWebP(const uint8_t* data, size_t size) {
#if NC_HAS_WEBP
    int w = 0, h = 0;
    uint8_t* px = WebPDecodeRGBA(data, size, &w, &h);
    if (!px) { LOGE("WebPDecodeRGBA failed"); return {}; }
    Image8 img(px, w, h, 4);
    WebPFree(px);
    return img;
#else
    LOGE("libwebp not available");
    return {};
#endif
}

Image8 decodeWebP(const std::vector<uint8_t>& data) {
    return decodeWebP(data.data(), data.size());
}

} // namespace nc
