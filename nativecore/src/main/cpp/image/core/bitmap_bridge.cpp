#include "bitmap_bridge.h"
#include "simd_utils.h"
#include <algorithm>
#include <android/bitmap.h>
#include <android/log.h>
#include <cstring>

#define TAG "nc::bitmap"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#if __has_include("libyuv.h")
#  include "libyuv.h"
#  define NC_HAS_LIBYUV 1
#elif __has_include("libyuv/convert.h")
#  include "libyuv/convert.h"
#  include "libyuv/convert_argb.h"
#  define NC_HAS_LIBYUV 1
#else
#  define NC_HAS_LIBYUV 0
#endif

namespace nc {

Image8 bitmapToImage(JNIEnv* env, jobject bitmap) {
    if (!env || !bitmap) return {};
    AndroidBitmapInfo info{};
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        LOGE("AndroidBitmap_getInfo failed");
        return {};
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("unsupported format %d (expected RGBA_8888)", info.format);
        return {};
    }

    void* pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        LOGE("lockPixels failed");
        return {};
    }

    const int w = static_cast<int>(info.width);
    const int h = static_cast<int>(info.height);
    Image8 img(w, h, 4);
    const auto* src = static_cast<const uint8_t*>(pixels);
    for (int y = 0; y < h; ++y)
        std::memcpy(img.rowPtr(y), src + static_cast<ptrdiff_t>(y) * info.stride,
                    static_cast<size_t>(w) * 4);

    AndroidBitmap_unlockPixels(env, bitmap);
    return img;
}

jobject imageToBitmap(JNIEnv* env, const Image8& img) {
    if (!env || img.empty() || img.channels != 4) return nullptr;

    jclass bitmapClass  = env->FindClass("android/graphics/Bitmap");
    jclass configClass  = env->FindClass("android/graphics/Bitmap$Config");
    if (!bitmapClass || !configClass) return nullptr;
    jfieldID argb8888   = env->GetStaticFieldID(configClass, "ARGB_8888",
                                                 "Landroid/graphics/Bitmap$Config;");
    jobject configObj   = env->GetStaticObjectField(configClass, argb8888);
    jmethodID createMid = env->GetStaticMethodID(bitmapClass, "createBitmap",
                           "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject bitmapObj   = env->CallStaticObjectMethod(bitmapClass, createMid,
                           img.width, img.height, configObj);
    if (!bitmapObj) return nullptr;

    void* pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, bitmapObj, &pixels) < 0) return bitmapObj;

    AndroidBitmapInfo info{};
    AndroidBitmap_getInfo(env, bitmapObj, &info);
    auto* dst = static_cast<uint8_t*>(pixels);
    for (int y = 0; y < img.height; ++y)
        std::memcpy(dst + static_cast<ptrdiff_t>(y) * info.stride, img.rowPtr(y),
                    static_cast<size_t>(img.width) * 4);

    AndroidBitmap_unlockPixels(env, bitmapObj);
    return bitmapObj;
}

jobject grayImageToBitmap(JNIEnv* env, const Image8& gray) {
    if (!env || gray.empty() || gray.channels != 1) return nullptr;
    Image8 rgba(gray.width, gray.height, 4);
    for (int y = 0; y < gray.height; ++y) {
        const uint8_t* src = gray.rowPtr(y);
        uint8_t* dst = rgba.rowPtr(y);
        int x = 0;
#if NC_HAS_NEON
        const uint8x8_t alpha = vdup_n_u8(255);
        for (; x <= gray.width - 8; x += 8) {
            uint8x8_t g = vld1_u8(src + x);
            uint8x8x4_t rgba_v;
            rgba_v.val[0] = g;
            rgba_v.val[1] = g;
            rgba_v.val[2] = g;
            rgba_v.val[3] = alpha;
            vst4_u8(dst + x * 4, rgba_v);
        }
#endif
        for (; x < gray.width; ++x) {
            const uint8_t v = src[x];
            dst[x * 4]     = v;
            dst[x * 4 + 1] = v;
            dst[x * 4 + 2] = v;
            dst[x * 4 + 3] = 255;
        }
    }
    return imageToBitmap(env, rgba);
}

jobject anyImageToBitmap(JNIEnv* env, const Image8& img) {
    if (!env || img.empty()) return nullptr;
    if (img.channels == 4) return imageToBitmap(env, img);
    if (img.channels == 1) return grayImageToBitmap(env, img);
    if (img.channels == 3) {
        Image8 rgba(img.width, img.height, 4);
        for (int y = 0; y < img.height; ++y) {
            const uint8_t* src = img.rowPtr(y);
            uint8_t* dst = rgba.rowPtr(y);
            int x = 0;
#if NC_HAS_NEON
            const uint8x8_t alpha = vdup_n_u8(255);
            for (; x <= img.width - 8; x += 8) {
                uint8x8x3_t rgb = vld3_u8(src + x * 3);
                uint8x8x4_t rgba_v;
                rgba_v.val[0] = rgb.val[0];
                rgba_v.val[1] = rgb.val[1];
                rgba_v.val[2] = rgb.val[2];
                rgba_v.val[3] = alpha;
                vst4_u8(dst + x * 4, rgba_v);
            }
#endif
            for (; x < img.width; ++x) {
                dst[x * 4]     = src[x * 3];
                dst[x * 4 + 1] = src[x * 3 + 1];
                dst[x * 4 + 2] = src[x * 3 + 2];
                dst[x * 4 + 3] = 255;
            }
        }
        return imageToBitmap(env, rgba);
    }
    LOGE("anyImageToBitmap: unsupported channel count %d", img.channels);
    return nullptr;
}

Image8 nv21ToImage(const uint8_t* nv21, int width, int height) {
    if (!nv21 || width <= 0 || height <= 0) return {};
    Image8 out(width, height, 4);

#if NC_HAS_LIBYUV
    const uint8_t* y_plane  = nv21;
    const uint8_t* vu_plane = nv21 + width * height;
    libyuv::NV21ToARGB(y_plane, width, vu_plane, width,
                       out.data.data(), width * 4, width, height);
    // libyuv outputs BGRA on little-endian; swap R and B
    const size_t total = static_cast<size_t>(width) * height * 4;
    size_t i = 0;
#if NC_HAS_NEON
    for (; i + 32 <= total; i += 32) {
        uint8x8x4_t px = vld4_u8(out.data.data() + i);
        // Swap R(val[0]) and B(val[2])
        uint8x8_t tmp = px.val[0];
        px.val[0] = px.val[2];
        px.val[2] = tmp;
        vst4_u8(out.data.data() + i, px);
    }
#endif
    for (; i < total; i += 4)
        std::swap(out.data[i], out.data[i + 2]);
#else
    const uint8_t* Y  = nv21;
    const uint8_t* VU = nv21 + width * height;
    for (int row = 0; row < height; ++row) {
        for (int col = 0; col < width; ++col) {
            int yv = Y[row * width + col];
            int uvIdx = (row / 2) * width + (col & ~1);
            int v = VU[uvIdx]     - 128;
            int u = VU[uvIdx + 1] - 128;
            uint8_t* px = out.rowPtr(row) + col * 4;
            px[0] = static_cast<uint8_t>(std::clamp(yv + ((1436 * v) >> 10), 0, 255));
            px[1] = static_cast<uint8_t>(std::clamp(yv - ((352 * u + 731 * v) >> 10), 0, 255));
            px[2] = static_cast<uint8_t>(std::clamp(yv + ((1815 * u) >> 10), 0, 255));
            px[3] = 255;
        }
    }
#endif
    return out;
}

} // namespace nc
