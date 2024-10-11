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
package github.elroy93.jvmlocals;

import java.util.HashMap;
import java.util.Map;

/**
 * @author elroysu
 * @date 2024/10/11 20:52
 */
public class JvmLocals {
    static {
        System.loadLibrary("JvmLocalsExceptionAgent");
    }

    // jni接口
    public native static Object getLocals(Map varHolder);

    public static void main(String[] args) {
        execute();
    }
    public static void execute() {
        final byte _byte_a = 1;
        final short _short_b = 2;
        final int _int_c = 3;
        final long _long_d = 4;
        final float _float_e = 5;
        final double _double_f = 6;
        final char _char_g = 'g';
        final boolean _boolean_h = true;
        int[] _arr_int = new int[]{1, 2, 3};
        var _string_y = "y";
        String _string_z = "z";

        // 获取变量信息, 返回的string类型
        String result = (String)getLocals(new HashMap());
        //
        String string_null = null;

        var varArr = result.split(",");
        System.out.println(">>>>>>>>>>> jvmlocals start <<<<<<<<<<<<");
        for (String item : varArr) {
            System.out.println(item);
        }
        System.out.println(">>>>>>>>>>> jvmlocals done <<<<<<<<<<<<");
    }

}

```

```shell
Frame 1: Lgithub/elroy93/jvmlocals/JvmLocals;.execute()V
>>>>>>>>>>> jvmlocals start <<<<<<<<<<<<
_arr_int: [I@2a139a55
_boolean_h: bool_1
_byte_a: 1
_char_g: char_103
_double_f: 6.000000
_float_e: 5.000000
_int_c: 3
_long_d: 4
_short_b: 2
_string_y: y
_string_z: z
>>>>>>>>>>> jvmlocals done <<<<<<<<<<<<
```
