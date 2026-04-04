#include "jni_helpers.h"
#include "../pdf/generation/pdf_builder.h"
#include "../pdf/crypto/pdf_crypto.h"
#include "../pdf/manipulation/pdf_merge_split.h"
#include <jni.h>
#include <vector>
#include <string>

using namespace nc;

#define JPDF(name) Java_io_github_dracula101_nativecore_NativePdfManager_##name

extern "C" {

// ── buildPdf(jpegPaths[], outputPath, pageSize, quality, hasMargin) → bool
JNIEXPORT jboolean JNICALL
JPDF(nativeBuildPdf)(JNIEnv* env, jclass,
                     jobjectArray jpegPathsArr, jstring outputPathJ,
                     jint pageSizeOrdinal, jint qualityOrdinal,
                     jboolean hasMargin) {
    const jsize count = env->GetArrayLength(jpegPathsArr);
    std::vector<std::string> paths;
    paths.reserve(count);
    for (jsize i = 0; i < count; ++i) {
        auto jstr = (jstring)env->GetObjectArrayElement(jpegPathsArr, i);
        paths.push_back(jstringToString(env, jstr));
        env->DeleteLocalRef(jstr);
    }
    PdfBuildOptions opts;
    opts.pageSize  = static_cast<PdfPageSize>(pageSizeOrdinal);
    opts.quality   = static_cast<PdfQuality>(qualityOrdinal);
    opts.hasMargin = (hasMargin == JNI_TRUE);
    return buildPdfFromJpegs(paths, jstringToString(env, outputPathJ), opts)
           ? JNI_TRUE : JNI_FALSE;
}

// ── encryptPdf(input, output, userPw, ownerPw) → bool
JNIEXPORT jboolean JNICALL
JPDF(nativeEncryptPdf)(JNIEnv* env, jclass,
                       jstring inputJ, jstring outputJ,
                       jstring userPwJ, jstring ownerPwJ) {
    return encryptPdf(jstringToString(env, inputJ),
                      jstringToString(env, outputJ),
                      jstringToString(env, userPwJ),
                      jstringToString(env, ownerPwJ))
           ? JNI_TRUE : JNI_FALSE;
}

// ── decryptPdf(input, output, password) → bool
JNIEXPORT jboolean JNICALL
JPDF(nativeDecryptPdf)(JNIEnv* env, jclass,
                       jstring inputJ, jstring outputJ, jstring pwJ) {
    return decryptPdf(jstringToString(env, inputJ),
                      jstringToString(env, outputJ),
                      jstringToString(env, pwJ))
           ? JNI_TRUE : JNI_FALSE;
}

// ── pdfHasPassword(path) → bool
JNIEXPORT jboolean JNICALL
JPDF(nativePdfHasPassword)(JNIEnv* env, jclass, jstring pathJ) {
    return pdfHasPassword(jstringToString(env, pathJ)) ? JNI_TRUE : JNI_FALSE;
}

// ── pdfCheckPassword(path, password) → bool
JNIEXPORT jboolean JNICALL
JPDF(nativePdfCheckPassword)(JNIEnv* env, jclass, jstring pathJ, jstring pwJ) {
    return pdfCheckPassword(jstringToString(env, pathJ),
                            jstringToString(env, pwJ))
           ? JNI_TRUE : JNI_FALSE;
}

// ── mergePdfs(paths[], output) → bool
JNIEXPORT jboolean JNICALL
JPDF(nativeMergePdfs)(JNIEnv* env, jclass,
                      jobjectArray pathsArr, jstring outputJ) {
    const jsize count = env->GetArrayLength(pathsArr);
    std::vector<std::string> paths;
    for (jsize i = 0; i < count; ++i) {
        auto jstr = (jstring)env->GetObjectArrayElement(pathsArr, i);
        paths.push_back(jstringToString(env, jstr));
        env->DeleteLocalRef(jstr);
    }
    return mergePdfs(paths, jstringToString(env, outputJ)) ? JNI_TRUE : JNI_FALSE;
}

// ── splitPdf(input, outputDir) → String[]
JNIEXPORT jobjectArray JNICALL
JPDF(nativeSplitPdf)(JNIEnv* env, jclass, jstring inputJ, jstring outputDirJ) {
    auto result = splitPdf(jstringToString(env, inputJ),
                           jstringToString(env, outputDirJ));
    jclass strClass = env->FindClass("java/lang/String");
    jobjectArray arr = env->NewObjectArray(static_cast<jsize>(result.size()), strClass, nullptr);
    for (size_t i = 0; i < result.size(); ++i) {
        jstring jstr = env->NewStringUTF(result[i].c_str());
        env->SetObjectArrayElement(arr, static_cast<jsize>(i), jstr);
        env->DeleteLocalRef(jstr);
    }
    return arr;
}

} // extern "C"
