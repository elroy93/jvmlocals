package github.elroy93;

/**
 * @author elroysu
 * @date 2024/10/14 20:09
 */
public class TestFile {

    public void testEnter() {
        // 我希望在这里执行 testFile.beforeExecute 方法
        execute();
    }

    public void execute() {
        System.out.println("Executing...");
    }

    public static void beforeExecute() {
        System.out.println("Before executing...");
        var e = new RuntimeException();
        throw e;
    }

}
