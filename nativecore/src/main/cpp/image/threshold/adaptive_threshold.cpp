#include "adaptive_threshold.h"
#include "../filter/gaussian_blur.h"
#include <algorithm>
#include <android/log.h>

#define TAG "nc::adaptive_threshold"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

namespace {

// Integral image for fast box sums
struct IntegralImage {
    std::vector<int64_t> data;
    int w, h;
    IntegralImage(const Image8& src) : w(src.width + 1), h(src.height + 1) {
        data.assign(static_cast<size_t>(w) * h, 0);
        for (int y = 0; y < src.height; ++y) {
            const uint8_t* row = src.rowPtr(y);
            for (int x = 0; x < src.width; ++x) {
                data[(y + 1) * w + (x + 1)] =
                    row[x]
                    + data[y * w + (x + 1)]
                    + data[(y + 1) * w + x]
                    - data[y * w + x];
            }
        }
    }
    int64_t sum(int x1, int y1, int x2, int y2) const {
        // x2, y2 are exclusive
        return data[y2 * w + x2]
             - data[y1 * w + x2]
             - data[y2 * w + x1]
             + data[y1 * w + x1];
    }
};

} // anonymous

void adaptiveThreshMean(const Image8& src, Image8& dst, int blockSize, double C) {
    if (src.empty() || src.channels != 1) {
        LOGE("adaptiveThreshMean: expected 1-channel image");
        return;
    }
    if (blockSize % 2 == 0) blockSize++;
    const int half = blockSize / 2;
    const int w = src.width, h = src.height;
    dst = Image8(w, h, 1);
    IntegralImage ii(src);
    const int area = blockSize * blockSize;
    for (int y = 0; y < h; ++y) {
        const uint8_t* srow = src.rowPtr(y);
        uint8_t*       drow = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            int x1 = std::max(0, x - half);
            int y1 = std::max(0, y - half);
            int x2 = std::min(w, x + half + 1);
            int y2 = std::min(h, y + half + 1);
            int cnt = (x2 - x1) * (y2 - y1);
            double mean = static_cast<double>(ii.sum(x1, y1, x2, y2)) / cnt;
            drow[x] = (srow[x] > mean - C) ? 255 : 0;
        }
    }
}

void adaptiveThreshGaussian(const Image8& src, Image8& dst, int blockSize, double C) {
    if (src.empty() || src.channels != 1) {
        LOGE("adaptiveThreshGaussian: expected 1-channel image");
        return;
    }
    if (blockSize % 2 == 0) blockSize++;
    // Gaussian-weighted local mean = Gaussian blur of src
    Image8 blurred;
    gaussianBlur(src, blurred, blockSize, 0.0f);
    const int w = src.width, h = src.height;
    dst = Image8(w, h, 1);
    for (int y = 0; y < h; ++y) {
        const uint8_t* srow = src.rowPtr(y);
        const uint8_t* brow = blurred.rowPtr(y);
        uint8_t*       drow = dst.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            double mean = brow[x];
            drow[x] = (srow[x] > mean - C) ? 255 : 0;
        }
    }
}

Image8 adaptiveThreshGaussian(const Image8& src, int blockSize, double C) {
    Image8 dst; adaptiveThreshGaussian(src, dst, blockSize, C); return dst;
}

Image8 adaptiveThreshMean(const Image8& src, int blockSize, double C) {
    Image8 dst; adaptiveThreshMean(src, dst, blockSize, C); return dst;
}

} // namespace nc
