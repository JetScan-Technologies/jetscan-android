#include "type_convert.h"
#include <algorithm>

namespace nc {

static inline uint8_t floatToU8(float v) {
    return static_cast<uint8_t>(std::clamp(v, 0.0f, 255.0f));
}

void convertToFloat(const Image8& src, Image32& dst) {
    if (src.empty()) return;
    dst = Image32(src.width, src.height, src.channels);
    const int n = src.width * src.height * src.channels;
    const uint8_t* s = src.data.data();
    float*         d = dst.data.data();
    for (int i = 0; i < n; ++i)
        d[i] = s[i] / 255.0f;
}

void convertToUint8(const Image32& src, Image8& dst) {
    if (src.empty()) return;
    dst = Image8(src.width, src.height, src.channels);
    const int n = src.width * src.height * src.channels;
    const float* s = src.data.data();
    uint8_t*     d = dst.data.data();
    for (int i = 0; i < n; ++i)
        d[i] = floatToU8(s[i] * 255.0f);
}

void convertToUint8Scaled(const Image32& src, Image8& dst, float scale) {
    if (src.empty()) return;
    dst = Image8(src.width, src.height, src.channels);
    const int n = src.width * src.height * src.channels;
    const float* s = src.data.data();
    uint8_t*     d = dst.data.data();
    for (int i = 0; i < n; ++i)
        d[i] = floatToU8(s[i] * scale);
}

} // namespace nc
