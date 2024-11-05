package fr.inria.gforge.spoon.view;
/**
 *
 * @author elroysu
 * @date 2024/10/31 星期四 1:13
 */
public class JvmLocalsTestFileOutput {
    private static int staticVar = 5;

    private int instanceVar = 10;

    public void add(int a, int b) {
        int c = a + b;
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a, b, c);
        int d = 0;
    }

    public void testForLoop() {
        for (int i = 0; i < 10; i++) {
            int loopVar = i * 2;
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(i, loopVar);
        }
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals();
    }

    public void testLambda() {
        java.util.function.Consumer<java.lang.Integer> consumer = y -> {
            int z = y + 100;
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(y, z);
        };
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(consumer);
    }

    public void testAnonymousClass() {
        java.util.function.Consumer<java.lang.Integer> consumer = new java.util.function.Consumer<java.lang.Integer>() {
            @java.lang.Override
            public void accept(java.lang.Integer i) {
                int z = i + 100;
                fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(i, z);
            }
        };
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(consumer);
    }

    public void testIfElse() {
        int a = 1;
        int b = 2;
        if (a > 0) {
            int positiveVar = a;
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a, b, positiveVar);
        } else {
            int negativeVar = -a;
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a, b, negativeVar);
        }
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a, b);
    }

    // tryCatchTest
    public void testTryCatch() {
        int a = 200;
        try {
            int tryVar = 1;
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a, tryVar);
        } catch (java.lang.Exception e) {
            int catchVar = 2;
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a, catchVar);
        } finally {
            int finallyVar = 3;
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a, finallyVar);
        }
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a);
    }

    // shadowingTest
    public void testShadowing() {
        int var1 = 1;
        {
            int var2 = 2;// 变量遮蔽

            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(var1, var2);
        }
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(var1);
    }

    // recursiveTest
    public void recursiveTest(int n) {
        int a = 100;
        if (n > 0) {
            int recursiveVar = n;
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(n, a, recursiveVar);
            recursiveTest(n - 1);
        }
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(n, a);
    }

    // arrayAndCollectionTest
    public void testArrayAndCollection() {
        int a = 1;
        int[] intArray = new int[]{ 1, 2, 3 };
        java.util.List<java.lang.String> stringList = java.util.Arrays.asList("a", "b", "c");
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(a, intArray, stringList);
    }

    // switch case
    public void testSwitchCase() {
        int value = 100;
        switch (value) {
            case 1 :
                int caseOneVar = 1;
                fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(value, caseOneVar);
                break;
            case 2 :
                int caseTwoVar = 2;
                fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(value, caseTwoVar);
                break;
            default :
                int defaultVar = 0;
                fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(value, defaultVar);
        }
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(value);
    }

    public void testNestedLoops() {
        for (int i = 0; i < 3; i++) {
            int outerVar = i;
            for (int j = 0; j < 2; j++) {
                int innerVar = j;
                fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(i, outerVar, j, innerVar);
            }
            fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(i, outerVar);
        }
    }

    public void testMethodReference() {
        java.util.List<java.lang.String> list = java.util.Arrays.asList("A", "B", "C");
        list.forEach(this::printString);
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(list);
    }

    public void testStream() {
        var list = java.util.Arrays.asList("1", "2", "3");
        java.util.List<java.lang.Integer> list1 = list.stream().map(it1 -> java.lang.Integer.valueOf(it1)).filter(it2 -> it2 > 2).peek(it3 -> {
            int a = 200;
            printLocals(list, it3, a);
        }).toList();
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(list, list1);
    }

    private void printString(java.lang.String s) {
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(s);
    }

    public void testSwitchStatement(int value) {
        java.lang.Integer i = 100;
        switch (value) {
            case 1 :
                int caseVar1 = 100;
                fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(value, i, caseVar1);
                break;
            case 2 :
                int caseVar2 = 200;
                fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(value, i, caseVar2);
                break;
            default :
                int defaultVar = 0;
                fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(value, i, defaultVar);
                break;
        }
        fr.inria.gforge.spoon.view.JvmLocalsTestFile.printLocals(value, i);
    }

    public static void printLocals(java.lang.Object... objs) {
        for (java.lang.Object obj : objs) {
            java.lang.System.out.println(obj);
        }
    }
}