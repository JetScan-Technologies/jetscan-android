#pragma once
#include "image.h"
#include <jni.h>

namespace nc {

// ─── Android Bitmap ↔ Image<uint8_t> ─────────────────────────────────────────

// Lock ARGB_8888 Bitmap pixels, copy into Image8 (RGBA layout, 4 channels).
// Caller owns the returned Image8.
Image8 bitmapToImage(JNIEnv* env, jobject bitmap);

// Create a new ARGB_8888 Bitmap from Image8 (must be 4-channel RGBA).
// Returns a local JNI reference; caller manages lifetime.
jobject imageToBitmap(JNIEnv* env, const Image8& img);

// Create a new grayscale (ALPHA_8) Bitmap from a 1-channel Image8.
jobject grayImageToBitmap(JNIEnv* env, const Image8& gray);

// Convert any Image8 (1/3/4-channel) to an ARGB_8888 Bitmap.
// Handles channel expansion automatically:
//   4-ch → direct copy
//   3-ch → adds alpha=255
//   1-ch → expands to gray RGBA
jobject anyImageToBitmap(JNIEnv* env, const Image8& img);

// NV21 (YUV_420_888 from CameraX) → RGBA Image8 via libyuv.
Image8 nv21ToImage(const uint8_t* nv21, int width, int height);

} // namespace nc
