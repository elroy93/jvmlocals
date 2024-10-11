#include "stdio.h"
#include "github_elroy93_jvmlocals_JvmLocals.h"

using namespace std;

JNIEXPORT jstring JNICALL Java_github_elroy93_jvmlocals_JvmLocals_getLocals(JNIEnv *, jclass, jstring)
{

    printf("Hello from C++\n");
    return NULL;
}
