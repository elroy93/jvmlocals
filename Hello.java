
/**
 * @author elroysu
 * @date 2024/10/9 14:17
 */
public class Hello {

    int id = 100;

    public static void main(String[] args) {
        try {
            int a = 1;
            Integer b = 2;
            new Hello().execute(a, b, "world");
        } finally {
            print(" finally end ");
        }
    }

    public void execute(int a, Integer b, String c) {
        doExecute(a, b, c);
    }

    public void doExecute(int a, Integer b, String c) {
        print("doExecute execute");
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
        System.out.println("############ java ########### " + value);
    }
}
