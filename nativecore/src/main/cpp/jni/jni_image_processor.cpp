#include "jni_helpers.h"
#include "../image/core/bitmap_bridge.h"
#include "../image/core/image.h"
#include "../image/color/yuv_converter.h"
#include "../image/document/document_detector.h"
#include "../image/document/page_cropper.h"
#include "../image/document/deskewer.h"
#include "../image/geometry/warp_affine.h"
#include "../image/geometry/quad_utils.h"
#include "../image/effect/effect_dispatcher.h"
#include <jni.h>

using namespace nc;

// JNI package: io.github.dracula101.nativecore
#define JFUNC(name) Java_io_github_dracula101_nativecore_NativeImageProcessor_##name

extern "C" {

// ── Initialize (load-time) ───────────────────────────────────────────────
JNIEXPORT jboolean JNICALL
JFUNC(nativeInit)(JNIEnv*, jclass) {
    return JNI_TRUE;
}

// ── detectDocument(bitmap) → float[8] = [tlX,tlY, trX,trY, blX,blY, brX,brY] or null
JNIEXPORT jfloatArray JNICALL
JFUNC(nativeDetectDocument)(JNIEnv* env, jclass, jobject bitmap) {
    Image8 img = bitmapToImage(env, bitmap);
    if (img.empty()) return nullptr;
    auto result = detectCornersA(img);
    if (!result) return nullptr;
    jfloatArray arr = env->NewFloatArray(8);
    float vals[8] = {
        result->topLeft.x,     result->topLeft.y,
        result->topRight.x,    result->topRight.y,
        result->bottomLeft.x,  result->bottomLeft.y,
        result->bottomRight.x, result->bottomRight.y
    };
    env->SetFloatArrayRegion(arr, 0, 8, vals);
    return arr;
}

// ── detectSingleDocument(bitmap) → float[8] or null
JNIEXPORT jfloatArray JNICALL
JFUNC(nativeDetectSingleDocument)(JNIEnv* env, jclass, jobject bitmap) {
    Image8 img = bitmapToImage(env, bitmap);
    if (img.empty()) return nullptr;
    auto result = detectSingleDocument(img);
    if (!result) return nullptr;
    jfloatArray arr = env->NewFloatArray(8);
    float vals[8] = {
        result->topLeft.x,     result->topLeft.y,
        result->topRight.x,    result->topRight.y,
        result->bottomLeft.x,  result->bottomLeft.y,
        result->bottomRight.x, result->bottomRight.y
    };
    env->SetFloatArrayRegion(arr, 0, 8, vals);
    return arr;
}

// ── cropDocument(bitmap, float[8]) → Bitmap
JNIEXPORT jobject JNICALL
JFUNC(nativeCropDocument)(JNIEnv* env, jclass, jobject bitmap, jfloatArray corners) {
    Image8 img = bitmapToImage(env, bitmap);
    if (img.empty()) return nullptr;
    float vals[8];
    env->GetFloatArrayRegion(corners, 0, 8, vals);
    Quad q;
    q.tl = {vals[0], vals[1]};
    q.tr = {vals[2], vals[3]};
    q.bl = {vals[4], vals[5]};
    q.br = {vals[6], vals[7]};
    Image8 cropped = cropDocument(img, q);
    if (cropped.empty()) return nullptr;
    return anyImageToBitmap(env, cropped);
}

// ── rotateDocument(bitmap, degrees) → Bitmap
JNIEXPORT jobject JNICALL
JFUNC(nativeRotateDocument)(JNIEnv* env, jclass, jobject bitmap, jfloat degrees) {
    Image8 img = bitmapToImage(env, bitmap);
    if (img.empty()) return nullptr;
    Point2f center = {img.width / 2.0f, img.height / 2.0f};
    Affine2x3 M = rotationMatrix2D(center, degrees, 1.0f);
    Image8 rotated = warpAffine(img, M, img.width, img.height);
    return anyImageToBitmap(env, rotated);
}

// ── applyFilter(bitmap, filterOrdinal) → Bitmap
JNIEXPORT jobject JNICALL
JFUNC(nativeApplyFilter)(JNIEnv* env, jclass, jobject bitmap, jint filterOrdinal) {
    Image8 img = bitmapToImage(env, bitmap);
    if (img.empty()) return nullptr;
    ImageFilter filter = static_cast<ImageFilter>(filterOrdinal);
    Image8 result = applyEffect(img, filter);
    return anyImageToBitmap(env, result);
}

// ── applyColorAdjustment(bitmap, brightness, contrast, saturation) → Bitmap
JNIEXPORT jobject JNICALL
JFUNC(nativeApplyColorAdjustment)(JNIEnv* env, jclass, jobject bitmap,
                                   jfloat brightness, jfloat contrast, jfloat saturation) {
    Image8 img = bitmapToImage(env, bitmap);
    if (img.empty()) return nullptr;
    Image8 result = colorAdjust(img, brightness, contrast, saturation);
    return anyImageToBitmap(env, result);
}

// ── convertYuvFrame(nv21 byte[], width, height) → Bitmap
JNIEXPORT jobject JNICALL
JFUNC(nativeConvertYuvFrame)(JNIEnv* env, jclass, jbyteArray nv21Arr,
                              jint width, jint height) {
    jsize len = env->GetArrayLength(nv21Arr);
    std::vector<uint8_t> nv21(static_cast<size_t>(len));
    env->GetByteArrayRegion(nv21Arr, 0, len, reinterpret_cast<jbyte*>(nv21.data()));
    Image8 img = nv21ToRgba(nv21.data(), width, height);
    if (img.empty()) return nullptr;
    return imageToBitmap(env, img);
}

// ── getLines(bitmap) → float[] = interleaved [slope, intercept, ...]
JNIEXPORT jfloatArray JNICALL
JFUNC(nativeGetLines)(JNIEnv* env, jclass, jobject bitmap) {
    Image8 img = bitmapToImage(env, bitmap);
    if (img.empty()) return env->NewFloatArray(0);
    auto lines = getLines(img);
    const int n = static_cast<int>(lines.size());
    jfloatArray arr = env->NewFloatArray(n * 2);
    std::vector<float> vals;
    vals.reserve(n * 2);
    for (const auto& l : lines) { vals.push_back(l.slope); vals.push_back(l.yIntercept); }
    if (!vals.empty())
        env->SetFloatArrayRegion(arr, 0, static_cast<jsize>(vals.size()), vals.data());
    return arr;
}

// ── applyFilters(bitmap) → Bitmap[] (all 7 filters)
JNIEXPORT jobjectArray JNICALL
JFUNC(nativeApplyFilters)(JNIEnv* env, jclass, jobject bitmap) {
    Image8 img = bitmapToImage(env, bitmap);
    if (img.empty()) return nullptr;
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jobjectArray arr = env->NewObjectArray(7, bitmapClass, nullptr);
    for (int i = 0; i < 7; ++i) {
        Image8 result = applyEffect(img, static_cast<ImageFilter>(i));
        jobject bm = anyImageToBitmap(env, result);
        if (bm) {
            env->SetObjectArrayElement(arr, i, bm);
            env->DeleteLocalRef(bm);
        }
    }
    return arr;
}

} // extern "C"
