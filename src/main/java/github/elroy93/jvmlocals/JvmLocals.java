package github.elroy93.jvmlocals;

import java.util.HashMap;
import java.util.Map;

/**
 * @author elroysu
 * @date 2024/10/11 20:52
 */
public class JvmLocals {

    static {
        System.loadLibrary("JvmLocalsAgent");
    }

    public native static String getLocals(String name);

    public static void main(String[] args) {
        //
    }

}
