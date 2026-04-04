#include "jni_helpers.h"
#include <cstring>

namespace nc {

std::string jstringToString(JNIEnv* env, jstring jstr) {
    if (!jstr) return {};
    const char* chars = env->GetStringUTFChars(jstr, nullptr);
    std::string result(chars ? chars : "");
    if (chars) env->ReleaseStringUTFChars(jstr, chars);
    return result;
}

std::vector<uint8_t> jbyteArrayToVector(JNIEnv* env, jbyteArray arr) {
    if (!arr) return {};
    const jsize len = env->GetArrayLength(arr);
    std::vector<uint8_t> vec(static_cast<size_t>(len));
    env->GetByteArrayRegion(arr, 0, len,
                            reinterpret_cast<jbyte*>(vec.data()));
    return vec;
}

jbyteArray vectorToJbyteArray(JNIEnv* env, const std::vector<uint8_t>& vec) {
    jbyteArray arr = env->NewByteArray(static_cast<jsize>(vec.size()));
    if (arr && !vec.empty())
        env->SetByteArrayRegion(arr, 0, static_cast<jsize>(vec.size()),
                                reinterpret_cast<const jbyte*>(vec.data()));
    return arr;
}

std::vector<float> jfloatArrayToVector(JNIEnv* env, jfloatArray arr) {
    if (!arr) return {};
    const jsize len = env->GetArrayLength(arr);
    std::vector<float> vec(static_cast<size_t>(len));
    env->GetFloatArrayRegion(arr, 0, len, vec.data());
    return vec;
}

void throwException(JNIEnv* env, const char* className, const char* msg) {
    jclass cls = env->FindClass(className);
    if (cls) env->ThrowNew(cls, msg);
}

} // namespace nc
