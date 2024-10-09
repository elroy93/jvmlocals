#include <iostream>
#include <string.h>
#include <stdio.h>
#include <jvmti.h>
#include <fstream>
#include <map>
#include <list>
#include <sstream>

using namespace std;

std::map<std::string, std::list<std::string>> config;

// 声明 readConfigFile 函数
void readConfigFile(const std::string &fileName);

JNIEXPORT void JNICALL ExceptionCallback(jvmtiEnv *jvmti_env,
                                         JNIEnv *jni_env,
                                         jthread thread,
                                         jmethodID method,
                                         jlocation location,
                                         jobject exception,
                                         jmethodID catch_method,
                                         jlocation catch_location)
{
    // 获取异常的类名
    jclass exc_class = jni_env->GetObjectClass(exception);

    // 校验是否在ignore的列表中
    if (true)
    {
        jmethodID mid = jni_env->GetMethodID(exc_class, "getClass", "()Ljava/lang/Class;");
        jobject clsObj = jni_env->CallObjectMethod(exception, mid);
        jclass clazzz = jni_env->GetObjectClass(clsObj);
        mid = jni_env->GetMethodID(clazzz, "getName", "()Ljava/lang/String;");
        jstring strObj = (jstring)jni_env->CallObjectMethod(clsObj, mid);

        const char *str = jni_env->GetStringUTFChars(strObj, NULL);

        auto ignoreExceptions = config["ignore"];
        // 如果在ignore列表中, 则不处理, 直接返回
        for (auto &ignoreException : ignoreExceptions)
        {
            if (strcmp(str, ignoreException.c_str()) == 0)
            {
                cout << ">> 跳过当前异常: " << str << endl;
                return;
            }
        }
        jni_env->ReleaseStringUTFChars(strObj, str);
        jni_env->DeleteLocalRef(clsObj); // 释放 Class 对象引用
        jni_env->DeleteLocalRef(clazzz); // 释放 Class 对象的类引用
        jni_env->DeleteLocalRef(strObj); // 释放字符串对象引用
    }

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
    readConfigFile("config.ini");

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

// 读取当前文件夹下的 config.ini文件, 放到一个map<string, list>变量中

// 读取并解析 config.ini 文件
void readConfigFile(const std::string &fileName)
{
    std::ifstream file(fileName);
    if (!file.is_open())
    {
        std::cerr << "无法打开配置文件: " << fileName << std::endl;
        return;
    }

    std::string line;
    while (std::getline(file, line))
    {
        // 跳过注释和空行
        if (line.empty() || line[0] == '#')
            continue;

        // 查找等号位置
        size_t pos = line.find('=');
        if (pos != std::string::npos)
        {
            std::string key = line.substr(0, pos);
            std::string value = line.substr(pos + 1);

            // 去除首尾空格
            key.erase(0, key.find_first_not_of(' '));
            key.erase(key.find_last_not_of(' ') + 1);
            value.erase(0, value.find_first_not_of(' '));
            value.erase(value.find_last_not_of(' ') + 1);

            // 将值拆分为多个值（使用逗号作为分隔符）
            std::stringstream ss(value);
            std::string item;
            while (std::getline(ss, item, ','))
            {
                item.erase(0, item.find_first_not_of(' ')); // 去除前导空格
                item.erase(item.find_last_not_of(' ') + 1); // 去除尾随空格
                config[key].push_back(item);                // 将项添加到对应的键中
            }
        }
    }
    file.close();

    // 打印
    std::cout << ">> 配置文件 " << fileName << " 开始读取" << std::endl;
    for (const auto &kv : config)
    {
        std::cout << kv.first << ": ";
        for (const auto &value : kv.second)
        {
            std::cout << value << " ";
        }
        std::cout << std::endl;
    }
    std::cout << ">> 配置文件 " << fileName << " 已读取" << std::endl;
}

// 打印解析的配置
void printConfig()
{
    for (const auto &kv : config)
    {
        std::cout << kv.first << ": ";
        for (const auto &value : kv.second)
        {
            std::cout << value << " ";
        }
        std::cout << std::endl;
    }
}
