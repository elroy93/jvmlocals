
/**
 * @author elroysu
 * @date 2024/10/9 14:17
 */
public class Hello {

    int id = 100;

    public static void main(String[] args) {
        print(" start ");
        int a = 1;
        Integer b = 2;
        try {
            new Hello().execute(a, b, "world");
        } catch (Exception e) {
        }
        // 空指针异常
        try {
            String npe = null;
            npe.length();
        } catch (Exception e) {
        }
        print(" end ");
    }

    public void execute(int a, Integer b, String c) {
        doExecute(a, b, c);
    }

    public void doExecute(int a, Integer b, String c) {
        int dd = 100;
        String ff = "fff";
        ff = null;
        var inner = new Inner();
        this.id++;

        throw new RuntimeException(">>>>> hello " + c);
    }

    public static class Inner {

        String name;
    }

    public static void print(String value) {
        System.out.println(">> java : " + value);
    }
}
