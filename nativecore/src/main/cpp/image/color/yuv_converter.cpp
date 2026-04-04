#include "yuv_converter.h"
#include <android/log.h>
#include <cstring>
#include <algorithm>

#define TAG "nc::yuv_converter"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#if __has_include("libyuv.h")
#  include "libyuv.h"
#  define NC_HAS_LIBYUV 1
#elif __has_include("libyuv/convert_argb.h")
#  include "libyuv/convert.h"
#  include "libyuv/convert_argb.h"
#  define NC_HAS_LIBYUV 1
#else
#  define NC_HAS_LIBYUV 0
#endif

namespace nc {

namespace {

// BT.601 YVU -> RGBA (NV21 is VU interleaved)
static void nv21ToRgbaScalar(const uint8_t* nv21, int w, int h, uint8_t* dst) {
    const uint8_t* Y  = nv21;
    const uint8_t* VU = nv21 + w * h;
    for (int row = 0; row < h; ++row) {
        uint8_t* out = dst + row * w * 4;
        for (int col = 0; col < w; ++col) {
            int y = Y[row * w + col];
            int uvRow = row / 2;
            int uvCol = col & ~1;
            int v = static_cast<int>(VU[uvRow * w + uvCol])     - 128;
            int u = static_cast<int>(VU[uvRow * w + uvCol + 1]) - 128;
            int r = y + static_cast<int>(1.402f   * v);
            int g = y - static_cast<int>(0.344f   * u + 0.714f * v);
            int b = y + static_cast<int>(1.772f   * u);
            *out++ = static_cast<uint8_t>(std::clamp(r, 0, 255));
            *out++ = static_cast<uint8_t>(std::clamp(g, 0, 255));
            *out++ = static_cast<uint8_t>(std::clamp(b, 0, 255));
            *out++ = 255;
        }
    }
}

} // anonymous namespace

Image8 nv21ToRgba(const uint8_t* nv21, int width, int height) {
    if (!nv21 || width <= 0 || height <= 0) return {};
    Image8 out(width, height, 4);
#if NC_HAS_LIBYUV
    const uint8_t* y_plane  = nv21;
    const uint8_t* vu_plane = nv21 + width * height;
    // libyuv NV21ToARGB writes BGRA byte order on little-endian ARM
    libyuv::NV21ToARGB(y_plane,  width,
                       vu_plane, width,
                       out.data.data(), width * 4,
                       width, height);
    // Swap B<->R to get RGBA
    uint8_t* px = out.data.data();
    const int n = width * height;
    for (int i = 0; i < n; ++i, px += 4) {
        std::swap(px[0], px[2]);
    }
#else
    nv21ToRgbaScalar(nv21, width, height, out.data.data());
#endif
    return out;
}

Image8 yuv420ToRgba(const uint8_t* yPlane, int yStride,
                    const uint8_t* uvPlane, int uvStride,
                    int width, int height) {
    if (!yPlane || !uvPlane || width <= 0 || height <= 0) return {};
    Image8 out(width, height, 4);
#if NC_HAS_LIBYUV
    libyuv::NV12ToARGB(yPlane,  yStride,
                       uvPlane, uvStride,
                       out.data.data(), width * 4,
                       width, height);
    uint8_t* px = out.data.data();
    const int n = width * height;
    for (int i = 0; i < n; ++i, px += 4) {
        std::swap(px[0], px[2]);
    }
#else
    const uint8_t* Y  = yPlane;
    const uint8_t* VU = uvPlane;
    for (int row = 0; row < height; ++row) {
        uint8_t* out_row = out.rowPtr(row);
        for (int col = 0; col < width; ++col) {
            int y = Y[row * yStride + col];
            int uvRow = row / 2;
            int uvCol = col & ~1;
            int v = static_cast<int>(VU[uvRow * uvStride + uvCol])     - 128;
            int u = static_cast<int>(VU[uvRow * uvStride + uvCol + 1]) - 128;
            int r = y + static_cast<int>(1.402f * v);
            int g = y - static_cast<int>(0.344f * u + 0.714f * v);
            int b = y + static_cast<int>(1.772f * u);
            uint8_t* px = out_row + col * 4;
            px[0] = static_cast<uint8_t>(std::clamp(r, 0, 255));
            px[1] = static_cast<uint8_t>(std::clamp(g, 0, 255));
            px[2] = static_cast<uint8_t>(std::clamp(b, 0, 255));
            px[3] = 255;
        }
    }
#endif
    return out;
}

} // namespace nc
