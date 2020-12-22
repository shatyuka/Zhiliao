#include <jni.h>
#include <android/log.h>
#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <pthread.h>
#include "dobby.h"

FILE *(*orig_fopen)(const char *filename, const char *mode);

FILE *fake_fopen(const char *filename, const char *mode) {
    size_t len = strlen(filename);
    if (len >= 8) {
        if (!strcmp(filename + len - 8, "base.apk")) {
            pthread_exit(nullptr);
        }
    }
    FILE *result = orig_fopen(filename, mode);
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_shatyuka_zhiliao_MainHook_initNative(JNIEnv *env, jobject) {
    DobbyHook((void*)fopen, (void*)fake_fopen, (void**)&orig_fopen);
}
