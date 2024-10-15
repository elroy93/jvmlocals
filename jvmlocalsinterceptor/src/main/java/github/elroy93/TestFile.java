package github.elroy93;

import java.util.Map;

/**
 * @author elroysu
 * @date 2024/10/14 20:09
 */
public class TestFile {

    int gx1 = 100;

    public void testEnter(Object obj) {
        // 我希望在这里执行 testFile.beforeExecute 方法
        int a = 1;
        long b = 2;
        if (obj == null) {
            a = 200;
        }

        // 我希望在这里执行 testFile.beforeExecute 方法, paramMap中包含上下文能访问的所有的变量
        // 例如a,b
        execute();
        System.out.println(a + b);
    }

    public void execute() {
        System.out.println("Executing...");
    }

    public static void beforeExecute(Map paramMap) {
        System.out.println("Before executing... " + paramMap);
        var e = new RuntimeException();
        throw e;
    }

}
