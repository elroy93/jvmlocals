#include <iostream>
#include <string.h>
#include <stdio.h>
#include <jvmti.h>
#include <fstream>
#include <map>
#include <list>
#include <sstream>
#include "github_elroy93_jvmlocals_JvmLocals.h"

using namespace std;

// 全局变量
JavaVM *global_vm = nullptr;
jvmtiEnv *global_jvmti = nullptr;

template <typename K, typename V>
std::string mapToString(const std::map<K, V> &m)
{
    std::ostringstream oss;
    for (auto it = m.begin(); it != m.end(); ++it)
    {
        if (it != m.begin())
        {
            oss << ",";
        }
        oss << it->first << ": " << it->second;
    }
    return oss.str();
}

JNIEXPORT jobject JNICALL Java_github_elroy93_jvmlocals_JvmLocals_getLocals(JNIEnv *jni_env, jclass, jobject)
{
    // 没有使用agent
    if (global_vm == nullptr || global_jvmti == nullptr)
    {
        cerr << "Agent not loaded!" << endl;
        return nullptr;
    }
    // 引用一下jvmti的环境
    auto jvmti = global_jvmti;

    // 获取当前的线程
    jthread thread;
    jvmtiError err = jvmti->GetCurrentThread(&thread);
    if (err != JVMTI_ERROR_NONE)
    {
        cerr << "Unable to get current thread!" << endl;
        return nullptr;
    }

    // 获取堆栈帧信息
    const int max_frames = 10;
    jvmtiFrameInfo frames[max_frames];
    jint frame_count = 0;

    auto jvmti_env = global_jvmti;
    err = jvmti_env->GetStackTrace(thread, 0, max_frames, frames, &frame_count);

    if (err != JVMTI_ERROR_NONE || frame_count <= 0)
    {
        cerr << "Unable to get stack trace! err=" << err << ", frame_count=" << frame_count << endl;
        return nullptr;
    }
    cout << "Frame_Count: " << frame_count << endl;

    std::map<std::string, std::string> kvMap;
    for (int i = 0; i < frame_count; i++)
    {
        if (i != 1)
        {
            continue;
        }
        // 获取本地变量表
        jint entry_count = 0;
        jvmtiLocalVariableEntry *table = NULL;
        err = global_jvmti->GetLocalVariableTable(frames[i].method, &entry_count, &table);
        if (err != JVMTI_ERROR_NONE)
        {
            cerr << "Error getting local variable table: " << err << endl;
            continue; // or handle appropriately
        }
        if (entry_count <= 0)
        {
            continue;
        }
        {
            // 处理逻辑上下文变量信息
            // 使用 String.valueOf 获取对象的字符串表示
            jclass stringClass = jni_env->FindClass("java/lang/String");
            jmethodID valueOfMethod = jni_env->GetStaticMethodID(stringClass, "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");

            for (int j = 0; j < entry_count; j++)
            {
                // 获取变量名称，签名和槽位
                char *var_name = table[j].name;
                char *var_signature = table[j].signature;
                jint var_slot = table[j].slot;
                jstring jstringKey = jni_env->NewStringUTF(var_name);

                // byte	jbyte	B               *
                // char	jchar	C               *
                // double	jdouble	D           *
                // float	jfloat	F           *
                // int	jint	I               *
                // short	jshort	S           *
                // long	jlong	J               *
                // boolean	jboolean	Z       *

                // 根据变量类型获取值
                if (var_signature[0] == 'B' || var_signature[0] == 'C' || var_signature[0] == 'I' || var_signature[0] == 'S' || var_signature[0] == 'Z')
                { // 整数类型
                    jint value;
                    err = jvmti_env->GetLocalInt(thread, i, var_slot, &value);
                    if (err == JVMTI_ERROR_NONE)
                    {
                        std::string strValue = std::to_string(value);
                        if (var_signature[0] == 'C')
                        {
                            strValue = "char_" + strValue;
                        }
                        else if (var_signature[0] == 'Z')
                        {
                            strValue = "bool_" + strValue;
                        }
                        kvMap[var_name] = strValue;
                    }
                }
                else if (var_signature[0] == 'F')
                {
                    jfloat value;
                    err = jvmti_env->GetLocalFloat(thread, i, var_slot, &value);
                    if (err == JVMTI_ERROR_NONE)
                    {
                        std::string strValue = std::to_string(value);
                        kvMap[var_name] = strValue;
                    }
                }
                else if (var_signature[0] == 'D')
                {
                    jdouble value;
                    err = jvmti_env->GetLocalDouble(thread, i, var_slot, &value);
                    if (err == JVMTI_ERROR_NONE)
                    {
                        std::string strValue = std::to_string(value);
                        kvMap[var_name] = strValue;
                    }
                }
                else if (var_signature[0] == 'J')
                {
                    jlong value;
                    err = jvmti_env->GetLocalLong(thread, i, var_slot, &value);
                    if (err == JVMTI_ERROR_NONE)
                    {
                        std::string strValue = std::to_string(value);
                        kvMap[var_name] = strValue;
                    }
                }
                else
                {
                    // 对象类型
                    jobject obj;
                    err = jvmti_env->GetLocalObject(thread, i, var_slot, &obj);
                    if (err == JVMTI_ERROR_NONE /* && obj != NULL */)
                    {
                        jstring jstringValue = (jstring)jni_env->CallStaticObjectMethod(stringClass, valueOfMethod, obj);
                        // jstringvalue转成c++的字符串
                        const char *str = jni_env->GetStringUTFChars(jstringValue, NULL);
                        kvMap[var_name] = str;
                        // 释放资源
                        jni_env->ReleaseStringUTFChars(jstringValue, str);
                        jni_env->DeleteLocalRef(jstringValue);
                        jni_env->DeleteLocalRef(obj);
                    }
                    else
                    {
                        // cout << "  4 Local variable " << var_name << " = " << "un_resolve" << ", err = " << err << endl;
                    }
                }
                jni_env->DeleteLocalRef(jstringKey);
            }

            // 释放本地变量表
            jvmti_env->Deallocate((unsigned char *)table);
            jni_env->DeleteLocalRef(stringClass);
        }
    }
    //
    std::string kvMapStr = mapToString(kvMap);
    jstring jstringKvMapStr = jni_env->NewStringUTF(kvMapStr.c_str());

    return jstringKvMapStr;
}

/**
 * agent初始化
 */
JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
{
    cout << "Agent_OnLoad(" << vm << ")" << endl;

    global_jvmti = nullptr;
    jvmtiEnv *jvmti;

    // 初始化 JVMTI 环境
    {
        jint result = vm->GetEnv((void **)&jvmti, JVMTI_VERSION_1_1);
        if (result != JNI_OK)
        {
            cerr << "Unable to access JVMTI!" << endl;
            return result;
        }
        global_vm = vm;       // 将传入的 JavaVM* vm 保存成全局变量
        global_jvmti = jvmti; // 将 JVMTI 环墋保存成全局变量
    }
    // 请求需要的能力
    {

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
    }
    return JNI_OK;
}

/**
 * agent卸载
 */
JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm)
{
    cout << "Agent_OnUnload(" << vm << ")" << endl;
}
