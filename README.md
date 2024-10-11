# jvmlocals

使用 jvmti, 注册异常回调, 在抛出异常的地方, 获取线程的上下文变量信息 .

想法来源于 python 的 locals()函数, 返回当前栈帧的局部变量信息


1. libJvmLocalsExceptionAgent.so : 当出现异常时, 获取当前线程的局部变量信息
2. 
## 如何运行本项目

```shell
# 执行测试代码
make test

# 清理动态库和class文件
make clean

# 编译动态库
make
```



## 演示

> 1. 测试代码, 未经过生产验证.

```java
   public void doexecute(int a, Integer b, String c) {
       System.out.println(">>>>> execute");
       int dd = 100;
       String ff = "fff";
       ff = null;
       var inner = new Inner();
       throw new RuntimeException(">>>>> hello " + c);
   }
```

```shell
Agent_OnLoad(0x7fbd89b4cb60)
>>>>> execute
>>>> id 101
ExceptionCallback invoked
Exception occurred: Ljava/lang/NullPointerException;
In thread: main
Frame 0: LHello;.doexecute(ILjava/lang/Integer;Ljava/lang/String;)V
  ################## 变量信息 ##################
  Local variable this = test.Hello@4c3e4790
  Local variable a = 1
  Local variable b = 2
  Local variable c = world
  Local variable dd = 100
  Local variable ff = null
  Local variable inner = test.Hello$Inner@38cccef
################## 变量信息 ##################
Frame 1: LHello;.execute(ILjava/lang/Integer;Ljava/lang/String;)V
Frame 2: LHello;.main([Ljava/lang/String;)V
Exception in thread "main" java.lang.NullPointerException
        at test.Hello.doexecute(test.Hello.java:29)
        at test.Hello.execute(test.Hello.java:16)
        at test.Hello.main(test.Hello.java:12)
Agent_OnUnload(0x7fbd89b4cb60)
```
