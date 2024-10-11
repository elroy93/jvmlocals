package github.elroy93.jvmlocals;

import java.util.Map;

/**
 * @author elroysu
 * @date 2024/10/11 20:52
 */
public class JvmLocals {

    // libJvmLocals.so
    static {
        System.loadLibrary("JvmLocals");
    }

    public native static Map<String, String> getLocals(String name);


    public static void main(String[] args) {
        var result = getLocals("hello world");
        System.out.println("jvmLocals test result = " + result);
    }
}
