#ifndef _JNI_UTILS_H_
#define _JNI_UTILS_H_

#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

#ifdef __cplusplus
extern "C"
{
#endif

 #define LOGI(...) __android_log_print(ANDROID_LOG_DEBUG, "I2C", __VA_ARGS__);


int jniThrowException(JNIEnv* env, const char* className, const char* msg);

JNIEnv* getJNIEnv();

int jniRegisterNativeMethods(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods);

#ifdef __cplusplus
}
#endif

#endif
