package fr.inria.gforge.spoon.view;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import fr.inria.gforge.spoon.view.JvmLocalsTestFile.*;
/**
 * @author elroysu
 * @date 2024/10/31 星期四 1:13
 */
public class JvmLocalsTestFile {

    private static int staticVar = 5;
    private int instanceVar = 10;

    public void add(int a, int b) {
        int c = a + b;
        printLocals();
        int d = 0;
    }

    public void testForLoop() {
        for (int i = 0; i < 10; i++) {
            int loopVar = i * 2;
            printLocals();
        }
        printLocals();
    }

    public void testLambda() {
        Consumer<Integer> consumer = (y) -> {
            int z = y + 100;
            printLocals();
        };
        printLocals();
    }

    public void testAnonymousClass() {
        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer i) {
                int z = i + 100;
                printLocals();
            }
        };
        printLocals();
    }

    public void testIfElse() {
        int a = 1;
        int b = 2;
        if (a > 0) {
            int positiveVar = a;
            printLocals();
        } else {
            int negativeVar = -a;
            printLocals();
        }
        printLocals();
    }

    // tryCatchTest
    public void testTryCatch() {
        int a = 200;
        try {
            int tryVar = 1;
            printLocals();
        } catch (Exception e) {
            int catchVar = 2;
            printLocals();
        } finally {
            int finallyVar = 3;
            printLocals();
        }
        printLocals();
    }

    // shadowingTest
    public void testShadowing() {
        int var1 = 1;
        {
            int var2 = 2;// 变量遮蔽
            printLocals();
        }
        printLocals();
    }

    // recursiveTest
    public void recursiveTest(int n) {
        int a = 100;
        if (n > 0) {
            int recursiveVar = n;
            printLocals();
            recursiveTest(n - 1);
        }
        printLocals();
    }

    // arrayAndCollectionTest
    public void testArrayAndCollection() {
        int a = 1;
        int[] intArray = {1, 2, 3};
        List<String> stringList = Arrays.asList("a", "b", "c");
        printLocals();
    }

    // switch case
    public void testSwitchCase() {
        int value = 100;
        switch (value) {
            case 1:
                int caseOneVar = 1;
                printLocals();
                break;
            case 2:
                int caseTwoVar = 2;
                printLocals();
                break;
            default:
                int defaultVar = 0;
                printLocals();
        }
        printLocals();
    }

    public void testNestedLoops() {
        for (int i = 0; i < 3; i++) {
            int outerVar = i;
            for (int j = 0; j < 2; j++) {
                int innerVar = j;
                printLocals();
            }
            printLocals();
        }
    }

    public void testMethodReference() {
        List<String> list = Arrays.asList("A", "B", "C");
        list.forEach(this::printString);
        printLocals();
    }

    public void testStream() {
        var list = Arrays.asList("1", "2", "3");
        List<Integer> list1 = list.stream()
                .map(it1 -> Integer.valueOf(it1))
                .filter(it2 -> it2 > 2)
                .peek(it3 -> {
                    int a = 200;
                    printLocals();
                })
                .toList();
        printLocals();
    }

    private void printString(String s) {
        printLocals();
    }

    public void testSwitchStatement(int value) {
        Integer i = 100;
        switch (value) {
            case 1:
                int caseVar1 = 100;
                printLocals();
                break;
            case 2:
                int caseVar2 = 200;
                printLocals();
                break;
            default:
                int defaultVar = 0;
                printLocals();
                break;
        }
        printLocals();
    }

    public static void printLocals(Object... objs) {
        for (Object obj : objs) {
            System.out.println(obj);
        }
    }
}
