package fr.inria.gforge.spoon.view;

import spoon.Launcher;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.List;

public class SpoonVariableInjector {
    public static void main(String[] args1) {
        // Initialize Spoon
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/main/java/fr/inria/gforge/spoon/view/JvmLocalsTestFile.java");
        launcher.buildModel();

        // Get the class
        CtClass<?> ctClass = launcher.getFactory().Class().get("fr.inria.gforge.spoon.view.JvmLocalsTestFile");

        // Get the method
        CtMethod<?> method = ctClass.getMethodsByName("add").get(0);

        // Visit the method to inject variables
        method.accept(new VariableCollectorVisitor());

        // 更改文件名称为JvmLocalsTestFileOutput.java
        ctClass.setSimpleName("JvmLocalsTestFileOutput");
        // Output the modified code
        System.out.println(ctClass);

        // 写入文件, JvmLocalsTestFileOutput.java
        launcher.createOutputWriter().createJavaFile(ctClass);
    }
}

class VariableCollectorVisitor extends CtScanner {
    private List<CtVariableReference<?>> variablesInScope = new ArrayList<>();

    @Override
    public <T> void visitCtMethod(CtMethod<T> m) {
        // Add method parameters to variablesInScope
        m.getParameters().forEach(param -> variablesInScope.add(param.getReference()));
        super.visitCtMethod(m);
        // Clear variablesInScope after finishing the method
        variablesInScope.clear();
    }

    @Override
    public <T> void visitCtBlock(CtBlock<T> block) {
        // 进入block之前保存variablesInScope
        List<CtVariableReference<?>> variablesBeforeBlock = new ArrayList<>(variablesInScope);
        super.visitCtBlock(block);
        // Restore variablesInScope after exiting the block
        variablesInScope = variablesBeforeBlock;
    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        // Add local variable to variablesInScope
        super.visitCtLocalVariable(localVariable);
        variablesInScope.add(localVariable.getReference());
    }

    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        if (invocation.getExecutable().getSimpleName().equals("printLocals")) {
            // Set arguments to the variables currently in scope
            List<CtVariableReference<?>> vars = new ArrayList<>(variablesInScope);
            List<CtExpression<?>> args = new ArrayList<>();
            for (CtVariableReference<?> varRef : vars) {
                args.add(invocation.getFactory().Code().createVariableRead(varRef, false));
            }
            invocation.setArguments(args);
        }
        super.visitCtInvocation(invocation);
    }

    @Override
    public <T> void visitCtLambda(CtLambda<T> lambda) {
        // 进入block之前保存variablesInScope
        List<CtVariableReference<?>> variablesBeforeBlock = new ArrayList<>(variablesInScope);
        super.visitCtLambda(lambda);
        // Restore variablesInScope after exiting the block
        variablesInScope = variablesBeforeBlock;
    }
}