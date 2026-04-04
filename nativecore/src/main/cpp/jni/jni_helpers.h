#pragma once
#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>

#define JNI_TAG "nc::jni"
#define JNI_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, JNI_TAG, __VA_ARGS__)
#define JNI_LOGI(...) __android_log_print(ANDROID_LOG_INFO,  JNI_TAG, __VA_ARGS__)

namespace nc {

// Convert jstring to std::string (UTF-8)
std::string jstringToString(JNIEnv* env, jstring jstr);

// Convert jbyteArray to std::vector<uint8_t>
std::vector<uint8_t> jbyteArrayToVector(JNIEnv* env, jbyteArray arr);

// Convert std::vector<uint8_t> to jbyteArray
jbyteArray vectorToJbyteArray(JNIEnv* env, const std::vector<uint8_t>& vec);

// Convert jfloatArray to std::vector<float>
std::vector<float> jfloatArrayToVector(JNIEnv* env, jfloatArray arr);

// Throw Java exception
void throwException(JNIEnv* env, const char* className, const char* msg);

} // namespace nc
