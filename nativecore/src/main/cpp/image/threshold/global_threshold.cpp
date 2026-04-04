#include "global_threshold.h"

namespace nc {

void threshold(const Image8& src, Image8& dst, double thresh, double maxVal) {
    if (src.empty()) return;
    dst = Image8(src.width, src.height, src.channels);
    const int n = src.width * src.height * src.channels;
    const uint8_t t  = static_cast<uint8_t>(thresh < 0 ? 0 : thresh > 255 ? 255 : thresh);
    const uint8_t mv = static_cast<uint8_t>(maxVal < 0 ? 0 : maxVal > 255 ? 255 : maxVal);
    const uint8_t* s = src.data.data();
    uint8_t*       d = dst.data.data();
    for (int i = 0; i < n; ++i)
        d[i] = (s[i] > t) ? mv : 0;
}

Image8 threshold(const Image8& src, double thresh, double maxVal) {
    Image8 dst;
    threshold(src, dst, thresh, maxVal);
    return dst;
}

} // namespace nc
