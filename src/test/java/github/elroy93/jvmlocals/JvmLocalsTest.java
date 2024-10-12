package github.elroy93.jvmlocals;

import org.junit.jupiter.api.*;

/**
 * @author elroysu
 */
class JvmLocalsTest {

    @BeforeAll
    static void init() {
        System.loadLibrary("JvmLocalsAgent");
    }

    @BeforeEach
    void setUp() {
    }

    // 在执行完成后执行
    @AfterAll
    static void tearDown() {
    }

    @Test
    public void testSimple() {
        // 查看jvm的启动参数
        int a = 1;
        int b = 2;
        var values = (String) JvmLocals.getLocals("testSimple");
        System.out.println(">>>>>>>>>>> jvmlocals start <<<<<<<<<<<<");
        for (String item : values.split(",")) {
            System.out.println("\t " + item);
        }
        System.out.println(">>>>>>>>>>> jvmlocals done <<<<<<<<<<<<");
    }
    // byte	        jbyte	    B  *
    // char	        jchar	    C  *
    // double	    jdouble	    D  *
    // float	    jfloat	    F  *
    // int	        jint	    I  *
    // short	    jshort	    S  *
    // long	        jlong	    J  *
    // boolean	    jboolean	Z  *

    @Test
    public void testByte() {
        byte aa = 1;
        var result = JvmLocals.getLocals("testByte");
        Assertions.assertEquals(result, "aa: 1");
    }

    @Test
    public void testChar() {
        char aa = 'a';
        var result = JvmLocals.getLocals("testChar");
        Assertions.assertEquals(result, "aa: a");
    }

    @Test
    public void testDouble() {
        double aa = 1.0;
        var result = JvmLocals.getLocals("testDouble");
        Assertions.assertEquals(result, "aa: 1.000000");
    }

    @Test
    public void testFloat() {
        float aa = 1.0f;
        var result = JvmLocals.getLocals("testFloat");
        Assertions.assertEquals(result, "aa: 1.000000");
    }

    @Test
    public void testShort() {
        short aa = 1;
        var result = JvmLocals.getLocals("testShort");
        Assertions.assertEquals(result, "aa: 1");
    }

    @Test
    public void testLong() {
        long aa = 1L;
        var result = JvmLocals.getLocals("testLong");
        Assertions.assertEquals(result, "aa: 1");
    }

    @Test
    public void testBoolean() {
        boolean aa = true;
        var result = JvmLocals.getLocals("testBoolean");
        Assertions.assertEquals(result, "aa: true");
    }

    @Test
    public void testInt() {
        int aa = 1;
        var result = JvmLocals.getLocals("testInt");
        Assertions.assertEquals(result, "aa: 1");
    }

}