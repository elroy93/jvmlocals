package github.elroy93;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.*;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        exe();
    }

    public static void exe() throws Exception {
        ByteBuddyAgent.install(); // Install the agent before using it
        getAgent();
        var classloader = new ByteArrayClassLoader.ChildFirst(Main.class.getClassLoader(),
                ClassFileLocator.ForClassLoader.readToNames(TestFile.class),
                ByteArrayClassLoader.PersistenceHandler.MANIFEST);

        var clazz = classloader.loadClass(TestFile.class.getName());
        var object = clazz.newInstance();
        clazz.getMethod("testEnter", Object.class).invoke(object,"1111");
    }

    public static ClassFileTransformer getAgent() {
        return new AgentBuilder.Default()
                // Specify which classes to instrument
                .type(ElementMatchers.nameStartsWith("github.elroy93"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {
                        // Return the modified builder
                        return builder.visit(new AsmVisitorWrapper() {
                            @Override
                            public int mergeWriter(int flags) {
                                // Ensure that frames and maxs are computed automatically
                                return flags | ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;
                            }

                            @Override
                            public int mergeReader(int flags) {
                                return flags;
                            }

                            @Override
                            public ClassVisitor wrap(
                                    TypeDescription instrumentedType,
                                    ClassVisitor classVisitor,
                                    Implementation.Context implementationContext,
                                    TypePool typePool,
                                    FieldList<FieldDescription.InDefinedShape> fields,
                                    MethodList<?> methods,
                                    int writerFlags,
                                    int readerFlags) {
                                return new ClassVisitor(Opcodes.ASM9, classVisitor) {
                                    @Override
                                    public MethodVisitor visitMethod(
                                            int access,
                                            String name,
                                            String descriptor,
                                            String signature,
                                            String[] exceptions) {
                                        String methodName = name;
                                        System.out.println("0000000000 localVariables: " + signature + " " + name);
                                        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                                        return new MethodVisitor(Opcodes.ASM9, mv) {

                                            List<LocalVariableNode> localVariables = new ArrayList<>();

                                            @Override
                                            public void visitLocalVariable(
                                                    String name,
                                                    String descriptor,
                                                    String signature,
                                                    Label start,
                                                    Label end,
                                                    int index) {
                                                // Collect local variable information
                                                localVariables.add(new LocalVariableNode(name, descriptor, signature, start, end, index));
                                                System.out.println("1111111111 localVariables: " + methodName + " " + name);
                                                super.visitLocalVariable(name, descriptor, signature, start, end, index);
                                            }

                                            @Override
                                            public void visitMethodInsn(
                                                    int opcode,
                                                    String owner,
                                                    String name,
                                                    String descriptor,
                                                    boolean isInterface) {
                                                // Check if the method call is to execute()
                                                if (opcode == Opcodes.INVOKEVIRTUAL
                                                        && owner.equals("github/elroy93/TestFile")
                                                        && name.equals("execute")
                                                        && descriptor.equals("()V")) {
                                                    System.out.println("2222222222 localVariables: " + methodName);

                                                    // Insert code to create Map and put local variables

                                                    // Create a Map<String, Object>
                                                    super.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
                                                    super.visitInsn(Opcodes.DUP);
                                                    super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
                                                    // Store the Map in a local variable
                                                    int mapVarIndex = getNextAvailableIndex(localVariables);
                                                    super.visitVarInsn(Opcodes.ASTORE, mapVarIndex);

                                                    // For each local variable, put it into the Map
                                                    for (LocalVariableNode localVar : localVariables) {
                                                        String varName = localVar.name;
                                                        int varIndex = localVar.index;
                                                        String varDesc = localVar.desc;

                                                        // Skip "this" reference
                                                        if (varName.equals("this")) {
                                                            continue;
                                                        }

                                                        // Load Map from local variable
                                                        super.visitVarInsn(Opcodes.ALOAD, mapVarIndex);
                                                        // Push variable name
                                                        super.visitLdcInsn(varName);
                                                        // Load variable value
                                                        loadVariable(super.mv, varDesc, varIndex);

                                                        // Box primitive types if necessary
                                                        if (isPrimitive(varDesc)) {
                                                            boxPrimitive(super.mv, varDesc);
                                                        }

                                                        // Call Map.put(Object key, Object value)
                                                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/HashMap", "put",
                                                                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
                                                        // Pop the result of put()
                                                        super.visitInsn(Opcodes.POP);
                                                    }

                                                    // Load the TestFile object (this)
                                                    super.visitVarInsn(Opcodes.ALOAD, 0); // 'this' is at index 0
                                                    // Load Map from local variable
                                                    super.visitVarInsn(Opcodes.ALOAD, mapVarIndex);
                                                    // Insert call to beforeExecute(Map)
                                                    System.out.println("3333333333 localVariables: " + localVariables);
                                                    super.visitMethodInsn(
                                                            Opcodes.INVOKEVIRTUAL,
                                                            "github/elroy93/TestFile",
                                                            "beforeExecute",
                                                            "(Ljava/util/Map;)V",
                                                            false);
                                                }

                                                // Proceed with the original method instruction
                                                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                            }

                                            private int getNextAvailableIndex(List<LocalVariableNode> localVariables) {
                                                int maxIndex = -1;
                                                for (LocalVariableNode var : localVariables) {
                                                    int varEndIndex = var.index + getVariableSize(var.desc);
                                                    if (varEndIndex > maxIndex) {
                                                        maxIndex = varEndIndex;
                                                    }
                                                }
                                                return maxIndex;
                                            }

                                            private int getVariableSize(String descriptor) {
                                                switch (descriptor.charAt(0)) {
                                                    case 'J': // long
                                                    case 'D': // double
                                                        return 2;
                                                    default:
                                                        return 1;
                                                }
                                            }

                                            private void loadVariable(MethodVisitor mv, String descriptor, int index) {
                                                switch (descriptor.charAt(0)) {
                                                    case 'I': // int
                                                    case 'B': // byte
                                                    case 'C': // char
                                                    case 'S': // short
                                                    case 'Z': // boolean
                                                        mv.visitVarInsn(Opcodes.ILOAD, index);
                                                        break;
                                                    case 'J': // long
                                                        mv.visitVarInsn(Opcodes.LLOAD, index);
                                                        break;
                                                    case 'F': // float
                                                        mv.visitVarInsn(Opcodes.FLOAD, index);
                                                        break;
                                                    case 'D': // double
                                                        mv.visitVarInsn(Opcodes.DLOAD, index);
                                                        break;
                                                    case 'L': // reference type
                                                    case '[': // array type
                                                        mv.visitVarInsn(Opcodes.ALOAD, index);
                                                        break;
                                                    default:
                                                        // Should not reach here
                                                        throw new IllegalArgumentException("Unsupported variable type: " + descriptor);
                                                }
                                            }

                                            private boolean isPrimitive(String descriptor) {
                                                switch (descriptor.charAt(0)) {
                                                    case 'I': // int
                                                    case 'B': // byte
                                                    case 'C': // char
                                                    case 'S': // short
                                                    case 'Z': // boolean
                                                    case 'J': // long
                                                    case 'F': // float
                                                    case 'D': // double
                                                        return true;
                                                    default:
                                                        return false;
                                                }
                                            }

                                            private void boxPrimitive(MethodVisitor mv, String descriptor) {
                                                String wrapperType;
                                                String valueOfDescriptor;

                                                switch (descriptor.charAt(0)) {
                                                    case 'I':
                                                        wrapperType = "java/lang/Integer";
                                                        valueOfDescriptor = "(I)Ljava/lang/Integer;";
                                                        break;
                                                    case 'B':
                                                        wrapperType = "java/lang/Byte";
                                                        valueOfDescriptor = "(B)Ljava/lang/Byte;";
                                                        break;
                                                    case 'C':
                                                        wrapperType = "java/lang/Character";
                                                        valueOfDescriptor = "(C)Ljava/lang/Character;";
                                                        break;
                                                    case 'S':
                                                        wrapperType = "java/lang/Short";
                                                        valueOfDescriptor = "(S)Ljava/lang/Short;";
                                                        break;
                                                    case 'Z':
                                                        wrapperType = "java/lang/Boolean";
                                                        valueOfDescriptor = "(Z)Ljava/lang/Boolean;";
                                                        break;
                                                    case 'J':
                                                        wrapperType = "java/lang/Long";
                                                        valueOfDescriptor = "(J)Ljava/lang/Long;";
                                                        break;
                                                    case 'F':
                                                        wrapperType = "java/lang/Float";
                                                        valueOfDescriptor = "(F)Ljava/lang/Float;";
                                                        break;
                                                    case 'D':
                                                        wrapperType = "java/lang/Double";
                                                        valueOfDescriptor = "(D)Ljava/lang/Double;";
                                                        break;
                                                    default:
                                                        throw new IllegalArgumentException("Unsupported primitive type: " + descriptor);
                                                }

                                                mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrapperType, "valueOf", valueOfDescriptor, false);
                                            }
                                        };
                                    }
                                };
                            }
                        });
                    }
                })
                .installOnByteBuddyAgent();
    }
}