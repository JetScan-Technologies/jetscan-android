#include "normalizer.h"
#include <algorithm>
#include <limits>

namespace nc {

void normalizeMinMax(const Image8& src, Image8& dst, uint8_t outMin, uint8_t outMax) {
    if (src.empty()) return;
    const int n = src.width * src.height * src.channels;
    const uint8_t* s = src.data.data();
    uint8_t minV = std::numeric_limits<uint8_t>::max();
    uint8_t maxV = std::numeric_limits<uint8_t>::lowest();
    for (int i = 0; i < n; ++i) {
        minV = std::min(minV, s[i]);
        maxV = std::max(maxV, s[i]);
    }
    if (dst.width != src.width || dst.height != src.height || dst.channels != src.channels)
        dst = Image8(src.width, src.height, src.channels);
    uint8_t* d = dst.data.data();
    if (maxV == minV) {
        for (int i = 0; i < n; ++i) d[i] = outMin;
        return;
    }
    const float scale = static_cast<float>(outMax - outMin) / (maxV - minV);
    for (int i = 0; i < n; ++i)
        d[i] = static_cast<uint8_t>(outMin + (s[i] - minV) * scale + 0.5f);
}

void normalizeMinMaxF(const Image32& src, Image32& dst, float outMin, float outMax) {
    if (src.empty()) return;
    const int n = src.width * src.height * src.channels;
    const float* s = src.data.data();
    float minV = std::numeric_limits<float>::max();
    float maxV = std::numeric_limits<float>::lowest();
    for (int i = 0; i < n; ++i) {
        minV = std::min(minV, s[i]);
        maxV = std::max(maxV, s[i]);
    }
    if (dst.width != src.width || dst.height != src.height || dst.channels != src.channels)
        dst = Image32(src.width, src.height, src.channels);
    float* d = dst.data.data();
    if (maxV - minV < 1e-7f) {
        for (int i = 0; i < n; ++i) d[i] = outMin;
        return;
    }
    const float scale = (outMax - outMin) / (maxV - minV);
    for (int i = 0; i < n; ++i)
        d[i] = outMin + (s[i] - minV) * scale;
}

void normalizeChannel(Image32& channel, float outMin, float outMax) {
    Image32 tmp;
    normalizeMinMaxF(channel, tmp, outMin, outMax);
    channel = std::move(tmp);
}

} // namespace nc
