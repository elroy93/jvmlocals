#include <iostream>

#include <iostream>
#include <string.h>
#include <stdio.h>
#include <jvmti.h>

using namespace std;

JNIEXPORT void JNICALL ExceptionCallback(jvmtiEnv *jvmti_env,
                                         JNIEnv *jni_env,
                                         jthread thread,
                                         jmethodID method,
                                         jlocation location,
                                         jobject exception,
                                         jmethodID catch_method,
                                         jlocation catch_location)
{
    cout << "ExceptionCallback invoked" << endl;

    // 获取异常的类名
    jclass exc_class = jni_env->GetObjectClass(exception);
    char *class_signature = NULL;
    jvmtiError err = jvmti_env->GetClassSignature(exc_class, &class_signature, NULL);
    if (err == JVMTI_ERROR_NONE && class_signature != NULL)
    {
        printf("Exception occurred: %s\n", class_signature);
        jvmti_env->Deallocate((unsigned char *)class_signature);
    }

    // 获取线程信息
    jvmtiThreadInfo thread_info;
    memset(&thread_info, 0, sizeof(thread_info));
    err = jvmti_env->GetThreadInfo(thread, &thread_info);
    if (err == JVMTI_ERROR_NONE && thread_info.name != NULL)
    {
        printf("In thread: %s\n", thread_info.name);
        jvmti_env->Deallocate((unsigned char *)thread_info.name);
    }

    // 获取堆栈帧信息
    const int max_frames = 10;
    jvmtiFrameInfo frames[max_frames];
    jint frame_count = 0;
    err = jvmti_env->GetStackTrace(thread, 0, max_frames, frames, &frame_count);
    if (err == JVMTI_ERROR_NONE && frame_count > 0)
    {
        for (int i = 0; i < frame_count; i++)
        {
            // 获取方法名和类名
            char *method_name = NULL;
            char *method_signature = NULL;
            char *method_generic = NULL;
            err = jvmti_env->GetMethodName(frames[i].method, &method_name, &method_signature, &method_generic);
            if (err != JVMTI_ERROR_NONE)
            {
                continue;
            }

            jclass declaring_class;
            err = jvmti_env->GetMethodDeclaringClass(frames[i].method, &declaring_class);
            if (err != JVMTI_ERROR_NONE)
            {
                jvmti_env->Deallocate((unsigned char *)method_name);
                jvmti_env->Deallocate((unsigned char *)method_signature);
                jvmti_env->Deallocate((unsigned char *)method_generic);
                continue;
            }

            char *class_name = NULL;
            err = jvmti_env->GetClassSignature(declaring_class, &class_name, NULL);
            if (err != JVMTI_ERROR_NONE)
            {
                jvmti_env->Deallocate((unsigned char *)method_name);
                jvmti_env->Deallocate((unsigned char *)method_signature);
                jvmti_env->Deallocate((unsigned char *)method_generic);
                continue;
            }

            printf("Frame %d: %s.%s%s\n", i, class_name, method_name, method_signature);

            // 检查是否是您的目标方法（main 方法）
            if (true)
            {
                // 获取本地变量表
                jint entry_count = 0;
                jvmtiLocalVariableEntry *table = NULL;
                err = jvmti_env->GetLocalVariableTable(frames[i].method, &entry_count, &table);
                // TODO 如果当前函数不是静态函数, 获取当前this对象的所有属性进行打印
                if (err == JVMTI_ERROR_NONE && entry_count > 0)
                {
                    for (int j = 0; j < entry_count; j++)
                    {
                        // 获取变量名称，签名和槽位
                        char *var_name = table[j].name;
                        char *var_signature = table[j].signature;
                        jint var_slot = table[j].slot;

                        // printf("  0Local variable %s \n", var_name);

                        // 检查变量是否在当前位置可见
                        if (location < table[j].start_location || location >= table[j].start_location + table[j].length)
                        {
                            continue;
                        }

                        // 根据变量类型获取值
                        if (var_signature[0] == 'I')
                        { // 整数类型
                            jint value;
                            err = jvmti_env->GetLocalInt(thread, i, var_slot, &value);
                            if (err == JVMTI_ERROR_NONE)
                            {
                                printf("  Local variable %s = %d\n", var_name, value);
                            }
                        }
                        // else if (var_signature[0] == 'L' || var_signature[0] == '[')
                        else
                        { // 对象类型
                            jobject obj;
                            err = jvmti_env->GetLocalObject(thread, i, var_slot, &obj);
                            if (err == JVMTI_ERROR_NONE /* && obj != NULL */)
                            {
                                // 如果是 Integer 类型的对象，获取其 intValue()
                                if (strcmp(var_signature, "Ljava/lang/Integer;") == 0)
                                {
                                    jclass integerClass = jni_env->FindClass("java/lang/Integer");
                                    jmethodID intValueMethod = jni_env->GetMethodID(integerClass, "intValue", "()I");
                                    jint intValue = jni_env->CallIntMethod(obj, intValueMethod);
                                    printf("  Local variable %s = %d\n", var_name, intValue);
                                    jni_env->DeleteLocalRef(integerClass);
                                }
                                else
                                {
                                    // 对于其他对象，调用 toString() 方法
                                    // jclass objClass = jni_env->GetObjectClass(obj);
                                    // jmethodID toStringMethod = jni_env->GetMethodID(objClass, "toString", "()Ljava/lang/String;");
                                    // jstring strObj = (jstring)jni_env->CallObjectMethod(obj, toStringMethod);
                                    // const char *str = jni_env->GetStringUTFChars(strObj, NULL);
                                    // printf("  Local variable %s = %s\n", var_name, str);
                                    // jni_env->ReleaseStringUTFChars(strObj, str);
                                    // jni_env->DeleteLocalRef(strObj);
                                    // jni_env->DeleteLocalRef(objClass);

                                    // 使用 String.valueOf 获取对象的字符串表示
                                    jclass stringClass = jni_env->FindClass("java/lang/String");
                                    jmethodID valueOfMethod = jni_env->GetStaticMethodID(stringClass, "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
                                    jstring strObj = (jstring)jni_env->CallStaticObjectMethod(stringClass, valueOfMethod, obj);
                                    const char *str = jni_env->GetStringUTFChars(strObj, NULL);
                                    printf("  Local variable %s = %s\n", var_name, str);
                                    jni_env->ReleaseStringUTFChars(strObj, str);
                                    jni_env->DeleteLocalRef(strObj);
                                    jni_env->DeleteLocalRef(stringClass);
                                }
                                jni_env->DeleteLocalRef(obj);
                            }
                        }
                        // 您可以根据需要添加对其他类型的支持
                    }

                    // 释放本地变量表
                    jvmti_env->Deallocate((unsigned char *)table);
                }
                else
                {
                    printf("Unable to get local variable table. Error: %d\n", err);
                }
            }

            // 释放资源
            jvmti_env->Deallocate((unsigned char *)method_name);
            jvmti_env->Deallocate((unsigned char *)method_signature);
            if (method_generic != NULL)
                jvmti_env->Deallocate((unsigned char *)method_generic);
            jvmti_env->Deallocate((unsigned char *)class_name);
        }
    }
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
{
    cout << "Agent_OnLoad(" << vm << ")" << endl;

    // 初始化 JVMTI 环境
    jvmtiEnv *jvmti;
    jint result = vm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_1);
    if (result != JNI_OK)
    {
        cerr << "Unable to access JVMTI!" << endl;
        return result;
    }

    // 请求需要的能力
    jvmtiCapabilities capabilities;
    memset(&capabilities, 0, sizeof(capabilities));
    capabilities.can_generate_exception_events = 1;
    capabilities.can_access_local_variables = 1;

    jvmtiError err = jvmti->AddCapabilities(&capabilities);
    if (err != JVMTI_ERROR_NONE)
    {
        cerr << "Unable to add capabilities (" << err << ")" << endl;
        return JNI_ERR;
    }

    // 注册异常事件回调
    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.Exception = &ExceptionCallback;
    err = jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
    if (err != JVMTI_ERROR_NONE)
    {
        cerr << "Cannot set event callbacks (" << err << ")" << endl;
        return JNI_ERR;
    }

    // 启用异常事件通知
    err = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION, NULL);
    if (err != JVMTI_ERROR_NONE)
    {
        cerr << "Cannot enable exception event notification (" << err << ")" << endl;
        return JNI_ERR;
    }

    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm)
{
    cout << "Agent_OnUnload(" << vm << ")" << endl;
}
