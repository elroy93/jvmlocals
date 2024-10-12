package github.elroy93.jvmlocals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

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

}