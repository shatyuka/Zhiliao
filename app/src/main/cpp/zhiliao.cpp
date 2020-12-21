#include <jni.h>
#include <android/log.h>
#include <cstdio>
#include <cstring>
#include <unistd.h>
#include "dobby.h"

const char *modulePath;
FILE *(*orig_fopen)(const char *filename, const char *mode);

FILE *fake_fopen(const char *filename, const char *mode) {
    size_t len = strlen(filename);
    if (len >= 8) {
        if (!strcmp(filename + len - 8, "base.apk")) {
            char *libsandhook = new char[len - 8 + 24];
            memset(libsandhook, 0, len - 8 + 24);
            strncpy(libsandhook, filename, len - 8);
            strcat(libsandhook, "/lib/arm/libsandhook.so");
            if (!access(libsandhook, F_OK) && !access(modulePath, F_OK)) {
                delete[] libsandhook;
                FILE *result = orig_fopen(modulePath, mode);
                return result;
            }
            delete[] libsandhook;
        }
    }
    FILE *result = orig_fopen(filename, mode);
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_shatyuka_zhiliao_MainHook_initNative(JNIEnv *env, jobject, jstring module_path) {
    modulePath = env->GetStringUTFChars(module_path, nullptr);
    DobbyHook((void*)fopen, (void*)fake_fopen, (void**)&orig_fopen);
}
