package github.elroy93;

import net.bytebuddy.agent.ByteBuddyAgent; // Added import
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
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class Main {
    public static void main(String[] args) throws Exception {

        ByteBuddyAgent.install(); // Install the agent before using it
        getAgent();
        var classloader = new ByteArrayClassLoader.ChildFirst(Main.class.getClassLoader(),
                ClassFileLocator.ForClassLoader.readToNames(TestFile.class),
                ByteArrayClassLoader.PersistenceHandler.MANIFEST);

        var clazz = classloader.loadClass(TestFile.class.getName());
        var object = clazz.newInstance();
        clazz.getMethod("testEnter").invoke(object);
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
                                        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                                        return new MethodVisitor(Opcodes.ASM9, mv) {
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
                                                    // Insert call to beforeExecute() before execute()
                                                    super.visitMethodInsn(
                                                            Opcodes.INVOKESTATIC,
                                                            "github/elroy93/TestFile",
                                                            "beforeExecute",
                                                            "()V",
                                                            false);
                                                }
                                                // Proceed with the original method instruction
                                                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                            }
                                        };
                                    }
                                };
                            }
                        });
                        // Removed the redundant 'return builder;' as we're returning the modified builder directly
                    }
                })
                .installOnByteBuddyAgent();
    }
}