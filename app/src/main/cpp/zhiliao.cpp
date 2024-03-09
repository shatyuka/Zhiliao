#include <jni.h>
#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <pthread.h>
#include <sys/mman.h>
#include "dobby.h"

FILE *(*orig_fopen)(const char *filename, const char *mode);

FILE *fake_fopen(const char *filename, const char *mode) {
    size_t len = strlen(filename);
    if (len >= 8) {
        if (!strcmp(filename + len - 8, "base.apk")) {
            DobbyDestroy((void*)fopen);
            pthread_exit(nullptr);
        }
    }
    FILE *result = orig_fopen(filename, mode);
    return result;
}

#define PAGE_ALIGN(address, page_size) ((address) & (-(page_size)))

extern "C"
JNIEXPORT void JNICALL
Java_com_shatyuka_zhiliao_MainHook_initNative(JNIEnv *env, jobject) {
    const auto page_size = sysconf(_SC_PAGE_SIZE);
    const auto aligned = PAGE_ALIGN((uintptr_t)fopen, page_size);
    mprotect((void*)aligned, page_size, PROT_READ | PROT_WRITE | PROT_EXEC);
    DobbyHook((void*)fopen, (dobby_dummy_func_t)fake_fopen, (dobby_dummy_func_t*)&orig_fopen);
}
