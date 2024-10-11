package github.elroy93.jvmlocals;

import java.util.HashMap;
import java.util.Map;

/**
 * @author elroysu
 * @date 2024/10/11 20:52
 */
public class JvmLocals {

    // libJvmLocals.so
    static {
        // System.loadLibrary("JvmLocals");
        System.loadLibrary("JvmLocalsAgent");
    }

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
        //
        var result = (String)getLocals(new HashMap());
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
