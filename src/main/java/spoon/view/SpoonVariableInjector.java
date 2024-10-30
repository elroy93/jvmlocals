package spoon.view;

import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.List;

public class SpoonVariableInjector {
    public static void main(String[] args1) {
        // Initialize Spoon
        Launcher launcher = new Launcher();
        launcher.addInputResource("src/main/java/fr/inria/gforge/spoon/view/A.java");
        launcher.buildModel();

        // Get the class
        CtClass<?> ctClass = launcher.getFactory().Class().get("fr.inria.gforge.spoon.view.A");

        // Get the method
        CtMethod<?> method = ctClass.getMethodsByName("add").get(0);

        // Visit the method to inject variables
        method.accept(new VariableCollectorVisitor());

        // Output the modified code
        System.out.println(ctClass);
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
        variablesInScope.add(localVariable.getReference());
        super.visitCtLocalVariable(localVariable);
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
}