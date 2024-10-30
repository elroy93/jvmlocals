package fr.inria.gforge.spoon.view;

import java.util.function.Consumer;

/**
 * @author elroysu
 * @date 2024/10/31 星期四 1:13
 */
public class A {

    public void add(int a , int b) {
        int c = a + b;
        if (true) {
            int x = 100;
            Consumer<Integer> consumer = (y) -> {
                int z = y + x;
                printLocals();
            };
            printLocals();
        }
        // 不填入参数, 使用spoon在生成代码的时候, 自动填入参数
        printLocals();
        int d = 0;
    }

    public static void printLocals(Object... objs) {
        for (Object obj : objs) {
            System.out.println(obj);
        }
    }
}
