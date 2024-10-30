package github.elroy93.jvmlocals;

import com.google.common.collect.ImmutableSet;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

final class AnnotationFileProcessorDemo extends AbstractProcessor {

    private Set<String> processedFiles = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> rootElements = roundEnv.getRootElements();
        // Get the Trees instance from the processing environment
        JavacTrees trees = JavacTrees.instance(processingEnv);

        // Get the context from the processing environment
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        // Get the TreeMaker and Names instances
        TreeMaker treeMaker = TreeMaker.instance(context);
        Names names = Names.instance(context);

        // Traverse all root elements
        for (Element element : rootElements) {
            // Get the package name and element name
            String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
            String elementName = element.getSimpleName().toString();
            String fileName = packageName + "." + elementName;
            // Print the file name
            System.out.println("Processing file: " + fileName);

            // Avoid processing the same file multiple times
            if (processedFiles.contains(fileName)) {
                continue;
            }
            processedFiles.add(fileName);

            // Get the syntax tree (AST) of the element
            JCTree jcTree = trees.getTree(element);
            if (jcTree == null) {
                continue;
            }

            // Create a TreeTranslator to modify the AST
            JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) trees.getPath(element).getCompilationUnit();
            jcTree.accept(new TreeTranslator() {

                Deque<Map<String, Object>> variableScopes = new ArrayDeque<>();
                JCTree.JCBlock currentBlock = null;

                @Override
                public void visitBlock(JCTree.JCBlock block) {
                    Map<String, Object> currentScope = new LinkedHashMap<>();
                    variableScopes.push(currentScope);

                    JCTree.JCBlock previousBlock = currentBlock;
                    currentBlock = block;

                    super.visitBlock(block);

                    currentBlock = previousBlock;

                    variableScopes.pop();
                }


                @Override
                public void visitVarDef(JCTree.JCVariableDecl varDef) {
                    super.visitVarDef(varDef);
                    variableScopes.peek().put(varDef.name.toString(), varDef.name);
                }

                @Override
                public void visitApply(JCTree.JCMethodInvocation jcMethodInvocation) {
                    System.out.println("visitApply: " + jcMethodInvocation);
                    super.visitApply(jcMethodInvocation);

                    // Check if the method invocation is test.Callee.execute()
                    if (jcMethodInvocation.meth instanceof JCTree.JCFieldAccess) {
                        JCTree.JCFieldAccess methodSelect = (JCTree.JCFieldAccess) jcMethodInvocation.meth;
                        String methodName = methodSelect.name.toString();
                        String qualifier = methodSelect.selected.toString();

                        if (methodName.equals("execute") && qualifier.equals("callee")) {

                            // Collect all accessible variables
                            Map<String, Object> accessibleVariables = new LinkedHashMap<>();
                            for (Map<String, Object> scope : variableScopes) {
                                accessibleVariables.putAll(scope);
                            }

                            // Generate and insert new code
                            insertNewCode(jcMethodInvocation, accessibleVariables);
                            if (true) {
                                return;
                            }

                            // Insert the call to test.Callee.beforeExecute() before this method invocation
                            JCTree.JCExpressionStatement beforeExecuteCall = createBeforeExecuteCall();
                            // Get the current block (statement list)
                            JCTree parent = getParent(jcMethodInvocation);
                            var invokeStatement = parent;
                            if (parent instanceof JCTree.JCExpression) {
                                parent = getParent(parent);
                                invokeStatement = getParent(parent);
                            }
                            // 这里是一个表达式
                            var blockParent = getParent(parent);

                            if (blockParent instanceof JCTree.JCBlock) {
                                JCTree.JCBlock block = (JCTree.JCBlock) blockParent;
                                ListBuffer<JCTree.JCStatement> newStatements = new ListBuffer<>();
                                for (JCTree.JCStatement statement : block.stats) {
                                    if (statement == invokeStatement) {
                                        // Insert beforeExecuteCall before execute()
                                        newStatements.add(beforeExecuteCall);
                                        System.out.println("statement: " + statement);
                                        System.out.println("beforeExecuteCall: " + beforeExecuteCall);
                                        System.out.println("jcMethodInvocation: " + jcMethodInvocation);
                                    }
                                    newStatements.add(statement);
                                }
                                // Update the block's statements
                                block.stats = newStatements.toList();
                                System.out.println("stats: " + block.stats);
                            }
                        }
                    }
                }

                // Helper method to create the call to test.Callee.beforeExecute()
                private JCTree.JCExpressionStatement createBeforeExecuteCall() {
                    // Build test.Callee.beforeExecute()
//                    JCTree.JCExpression callee = chainDots("test", "Callee");
                    JCTree.JCExpression callee = chainDots("callee");
                    Name methodName = names.fromString("beforeExecute");
                    JCTree.JCFieldAccess methodAccess = treeMaker.Select(callee, methodName);
                    JCTree.JCMethodInvocation methodInvocation = treeMaker.Apply(
                            List.nil(), // No type arguments
                            methodAccess,
                            List.nil()  // No arguments
                    );
                    return treeMaker.Exec(methodInvocation);
                }


                private JCTree.JCVariableDecl createMapDeclaration() {
                    // Map map = new HashMap();
                    JCTree.JCExpression mapType = chainDots("java", "util", "Map");
                    JCTree.JCExpression hashMapType = chainDots("java", "util", "HashMap");
                    JCTree.JCNewClass newHashMap = treeMaker.NewClass(
                            null,
                            List.nil(),
                            hashMapType,
                            List.nil(),
                            null
                    );
                    Name mapName = names.fromString("map");
                    return treeMaker.VarDef(
                            treeMaker.Modifiers(0),
                            mapName,
                            mapType,
                            newHashMap
                    );
                }

                private List<JCTree.JCStatement> generateMapPutCalls(Map<String, Object> variables) {
                    ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
                    Name mapName = names.fromString("map");
                    for (Map.Entry<String, Object> entry : variables.entrySet()) {
                        String varName = entry.getKey();
                        Object varSymbol = entry.getValue();
                        // map.put("varName", varValue);
                        JCTree.JCFieldAccess mapPutSelect = treeMaker.Select(
                                treeMaker.Ident(mapName),
                                names.fromString("put")
                        );
                        JCTree.JCExpression varNameLiteral = treeMaker.Literal(varName);
                        JCTree.JCExpression varValueIdent = treeMaker.Ident((Name)varSymbol);
                        JCTree.JCMethodInvocation putCall = treeMaker.Apply(
                                List.nil(),
                                mapPutSelect,
                                List.of(varNameLiteral, varValueIdent)
                        );
                        statements.add(treeMaker.Exec(putCall));
                    }
                    return statements.toList();
                }

                // Helper method to create a chain of field accesses
                private JCTree.JCExpression chainDots(String... elems) {
                    JCTree.JCExpression expr = treeMaker.Ident(names.fromString(elems[0]));
                    for (int i = 1; i < elems.length; i++) {
                        expr = treeMaker.Select(expr, names.fromString(elems[i]));
                    }
                    return expr;
                }

                private void insertNewCode(JCTree.JCMethodInvocation jcMethodInvocation,
                                           Map<String, Object> accessibleVariables) {
                    // Generate new statements
                    JCTree.JCVariableDecl mapDecl = createMapDeclaration();
                    List<JCTree.JCStatement> mapPutCalls = generateMapPutCalls(accessibleVariables);
                    JCTree.JCStatement beforeExecuteCall = createBeforeExecuteCall();

                    // Find the enclosing statement of jcMethodInvocation
                    JCTree.JCStatement enclosingStatement = getEnclosingStatement(jcMethodInvocation);

                    if (enclosingStatement != null && currentBlock != null) {
                        // Build the list of new statements
                        ListBuffer<JCTree.JCStatement> newStatements = new ListBuffer<>();
                        newStatements.add(mapDecl);
                        newStatements.addAll(mapPutCalls);
                        newStatements.add(beforeExecuteCall);
                        newStatements.add(enclosingStatement);

                        // Replace the enclosing statement with the new statements
                        ListBuffer<JCTree.JCStatement> newBlockStatements = new ListBuffer<>();
                        for (JCTree.JCStatement stmt : currentBlock.stats) {
                            if (stmt == enclosingStatement) {
                                newBlockStatements.addAll(newStatements);
                            } else {
                                newBlockStatements.add(stmt);
                            }
                        }
                        currentBlock.stats = newBlockStatements.toList();
                        for (JCTree.JCStatement stat : currentBlock.stats) {
                            System.out.println(stat + ";");
                        }
                    }
                }

                // Helper method to find the enclosing statement
                private JCTree.JCStatement getEnclosingStatement(JCTree node) {
                    TreePath path = trees.getPath(compilationUnit, node);
                    while (path != null) {
                        if (path.getLeaf() instanceof JCTree.JCStatement) {
                            return (JCTree.JCStatement) path.getLeaf();
                        }
                        path = path.getParentPath();
                    }
                    return null;
                }


                // Helper method to get the parent of a tree node
                private JCTree getParent(JCTree node) {
                    TreePath path = trees.getPath(compilationUnit, node);
                    if (path != null && path.getParentPath() != null) {
                        return (JCTree) path.getParentPath().getLeaf();
                    }
                    return null;
                }
            });
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of("*");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}