package github.elroy93.jvmlocals;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.testing.compile.Compiler.javac;

/**
 * @author elroysu
 * @date 2024/10/15 11:38
 */
public class TestAnnotationProcessor {

    private static final JavaFileObject Caller_RESOURCE = JavaFileObjects.forResource("test/Caller.java");
    private static final JavaFileObject Callee_RESOURCE = JavaFileObjects.forResource("test/Callee.java");

    @Test
    public void testAnnotation() throws IOException {
        AnnotationFileProcessorDemo processor = new AnnotationFileProcessorDemo();
        Compiler compiler = javac().withProcessors(processor).withAnnotationProcessorPath(ImmutableList.of());
        Compilation compilation = compiler.compile(Caller_RESOURCE, Callee_RESOURCE);
        for (JavaFileObject generatedFile : compilation.generatedFiles()) {
            saveInMemoryFile(generatedFile);
        }
    }

    public static String getFileContent(JavaFileObject fileObject) throws IOException {
        return fileObject.getCharContent(true).toString();
    }

    public static void saveInMemoryFile(JavaFileObject inMemoryFileObject) throws IOException {
        String outputPath = "C:\\temp\\jvmlocals\\" + inMemoryFileObject.getName();

        // 1. 从 InMemoryJavaFileObject 获取字节码
        byte[] byteCode = inMemoryFileObject.openInputStream().readAllBytes();

        // 2. 创建输出文件路径
        Path outputFilePath = Paths.get(outputPath);
        File outputFile = outputFilePath.toFile();

        // 3. 创建父目录（如果不存在）
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        // 4. 将字节码写入文件
        Files.write(outputFilePath, byteCode);

        System.out.println("文件已保存到: " + outputFilePath);
    }

}
