package fr.inria.gforge.spoon.transformation;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

// super simple transformation: creating a class and adding a method
public class BasicTransfoExampleTest {
    @Test
    public void main() {
        Launcher launcher = new Launcher();


        Factory factory = launcher.getFactory();
        CtClass aClass = factory.createClass("my.org.MyClass");

        aClass.setSimpleName("myNewName");
        CtMethod myMethod = factory.createMethod();
        myMethod.setSimpleName("myMethod");
        // 添加实现 : 打印一句话
        myMethod.setBody(factory.createCodeSnippetStatement("System.out.println(\"Hello World\");"));
        // 设置没有返回值类型
        myMethod.setType(factory.Type().VOID);
        // 增加连个参数, 分别   int a, int b
        factory.createParameter(myMethod, factory.Type().INTEGER, "a");
        factory.createParameter(myMethod, factory.Type().STRING, "b");
        aClass.addMethod(myMethod);

        System.out.println(aClass);
    }
}
