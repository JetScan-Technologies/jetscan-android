#include "canny.h"
#include "sobel.h"
#include "../filter/gaussian_blur.h"
#include <cmath>
#include <algorithm>
#include <queue>
#include <android/log.h>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#define TAG "nc::canny"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace nc {

void canny(const Image8& src, Image8& dst, double lowThresh, double highThresh) {
    if (src.empty()) { LOGE("canny: empty src"); return; }
    if (lowThresh > highThresh) std::swap(lowThresh, highThresh);

    const Image8* gray = &src;
    Image8 grayBuf;
    if (src.channels != 1) {
        grayBuf = Image8(src.width, src.height, 1);
        for (int y = 0; y < src.height; ++y) {
            const uint8_t* s = src.rowPtr(y);
            uint8_t* d = grayBuf.rowPtr(y);
            for (int x = 0; x < src.width; ++x, s += src.channels, ++d)
                *d = s[0];
        }
        gray = &grayBuf;
    }

    Image8 blurred = gaussianBlur(*gray, 5, 1.4f);

    Image32 gx, gy;
    sobel(blurred, gx, gy);
    if (gx.empty()) return;

    const int w = src.width, h = src.height;

    // Gradient magnitude and quantized direction (0, 45, 90, 135 degrees)
    Image32 mag(w, h, 1);
    Image8  dir(w, h, 1);
    for (int y = 0; y < h; ++y) {
        const float* gxr = gx.rowPtr(y);
        const float* gyr = gy.rowPtr(y);
        float*       mr  = mag.rowPtr(y);
        uint8_t*     dr  = dir.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            float mx = gxr[x], my = gyr[x];
            mr[x] = std::sqrt(mx*mx + my*my);
            float angle = std::atan2(my, mx) * 180.0f / static_cast<float>(M_PI);
            if (angle < 0) angle += 180.0f;
            if      (angle < 22.5f || angle >= 157.5f) dr[x] = 0;
            else if (angle < 67.5f)                     dr[x] = 45;
            else if (angle < 112.5f)                    dr[x] = 90;
            else                                        dr[x] = 135;
        }
    }

    // Non-maximum suppression
    Image8 nms(w, h, 1);
    for (int y = 1; y < h - 1; ++y) {
        const float*   mr = mag.rowPtr(y);
        const uint8_t* dr = dir.rowPtr(y);
        uint8_t*       nr = nms.rowPtr(y);
        for (int x = 1; x < w - 1; ++x) {
            float m = mr[x];
            float n1 = 0, n2 = 0;
            switch (dr[x]) {
                case 0:
                    n1 = mag.rowPtr(y)[x-1];
                    n2 = mag.rowPtr(y)[x+1];
                    break;
                case 45:
                    n1 = mag.rowPtr(y-1)[x+1];
                    n2 = mag.rowPtr(y+1)[x-1];
                    break;
                case 90:
                    n1 = mag.rowPtr(y-1)[x];
                    n2 = mag.rowPtr(y+1)[x];
                    break;
                default:
                    n1 = mag.rowPtr(y-1)[x-1];
                    n2 = mag.rowPtr(y+1)[x+1];
                    break;
            }
            nr[x] = (m >= n1 && m >= n2) ? static_cast<uint8_t>(std::clamp(m, 0.0f, 255.0f)) : 0;
        }
    }

    // Double threshold + hysteresis via BFS
    const float lo = static_cast<float>(lowThresh);
    const float hi = static_cast<float>(highThresh);
    dst = Image8(w, h, 1);
    std::queue<std::pair<int,int>> queue;

    // 0=suppressed, 1=weak, 2=strong
    std::vector<uint8_t> state(static_cast<size_t>(w) * static_cast<size_t>(h), 0);
    for (int y = 0; y < h; ++y) {
        const uint8_t* nr = nms.rowPtr(y);
        for (int x = 0; x < w; ++x) {
            float m = nr[x];
            if (m >= hi) {
                state[static_cast<size_t>(y) * w + x] = 2;
                queue.push({x, y});
            } else if (m >= lo) {
                state[static_cast<size_t>(y) * w + x] = 1;
            }
        }
    }

    static const int dx8[] = {-1,0,1,-1,1,-1,0,1};
    static const int dy8[] = {-1,-1,-1,0,0,1,1,1};
    while (!queue.empty()) {
        auto [cx, cy] = queue.front(); queue.pop();
        dst.rowPtr(cy)[cx] = 255;
        for (int k = 0; k < 8; ++k) {
            int nx = cx + dx8[k], ny = cy + dy8[k];
            if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
            size_t idx = static_cast<size_t>(ny) * w + nx;
            if (state[idx] == 1) {
                state[idx] = 2;
                queue.push({nx, ny});
            }
        }
    }
}

Image8 canny(const Image8& src, double lowThresh, double highThresh) {
    Image8 dst; canny(src, dst, lowThresh, highThresh); return dst;
}

} // namespace nc
