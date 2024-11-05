package fr.inria.gforge.spoon.view;

import org.eclipse.jdt.internal.core.SourceConstructorWithChildrenInfo;
import spoon.Launcher;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.support.JavaOutputProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpoonVariableInjector {
    public static void main(String[] args1) {
        var fileName = "src/main/java/fr/inria/gforge/spoon/view/JvmLocalsTestFileOutput.java";
        // 如果文件存在,先删除
        new File(fileName).delete();

        // Initialize Spoon
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/main/java/fr/inria/gforge/spoon/view/JvmLocalsTestFile.java");
        launcher.buildModel();

        // Get the class
        CtClass<?> ctClass = launcher.getFactory().Class().get("fr.inria.gforge.spoon.view.JvmLocalsTestFile");

        // Get the method

        for (CtMethod<?> method : ctClass.getMethods()) {
            method.accept(new VariableCollectorVisitor());
        }

        ctClass.setSimpleName("JvmLocalsTestFileOutput");
        JavaOutputProcessor outputWriter = launcher.createOutputWriter();
        outputWriter.createJavaFile(ctClass);
        List<File> createdFiles = outputWriter.getCreatedFiles();
        System.out.println(createdFiles);
        // 复制文件到 src/main/java/fr/inria/gforge/spoon/view/ 这个目录下
        for (File file : createdFiles) {
            new File(fileName).delete();
            file.renameTo(new File(fileName));
        }
    }
}

class VariableCollectorVisitor extends CtScanner {
    private List<CtVariableReference<?>> variablesInScope = new ArrayList<>();
    private boolean isForLoop = false;

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
        backUpAndRecover(() -> {
            super.visitCtBlock(block);
        });
    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        // 循环遍历, 先访问变量,后访问block,并且block中能访问这个变量.
        super.visitCtLocalVariable(localVariable);
        variablesInScope.add(localVariable.getReference());
    }



    @Override
    public <S> void visitCtCase(CtCase<S> caseStatement) {
        backUpAndRecover(() -> super.visitCtCase(caseStatement));
    }

    @Override
    public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
        backUpAndRecover(() -> super.visitCtSwitch(switchStatement));
    }

    @Override
    public void visitCtFor(CtFor forLoop) {
        var backIsForLoop = this.isForLoop;
        this.isForLoop = true;
        backUpAndRecover(() -> {
            super.visitCtFor(forLoop);
        });
        this.isForLoop = backIsForLoop;
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
        backUpAndRecover(() -> {
            for (CtParameter<?> param : lambda.getParameters()) {
                variablesInScope.add(param.getReference());
            }
            super.visitCtLambda(lambda);
        });
    }

    public void backUpAndRecover(Runnable run) {
        // 备份variablesInScope
        List<CtVariableReference<?>> variablesBeforeBlock = new ArrayList<>(variablesInScope);
        run.run();
        // 恢复variablesInScope
        variablesInScope = variablesBeforeBlock;
    }
}