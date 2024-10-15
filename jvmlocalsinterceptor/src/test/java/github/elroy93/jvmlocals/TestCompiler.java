package github.elroy93.jvmlocals;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * @author elroysu
 * @date 2024/10/15 11:38
 */
public class TestCompiler {

    private static final JavaFileObject HELLO_WORLD_RESOURCE =
            JavaFileObjects.forResource("test/HelloWorld.java");

    @Test
    public void testSourceCodeCompiler() throws IOException {
        Compilation compilation = javac().compile(HELLO_WORLD_RESOURCE);
        // 保存class文件
        compilation.generatedFiles().forEach(file -> {
            try {
                // 保存文件到指定目录
                String outputPath = "C:\\temp\\jvmlocals\\" + file.getName();
                saveInMemoryFile(file, outputPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        assertThat(compilation).succeeded();
    }

    public static String getFileContent(JavaFileObject fileObject) throws IOException {
        return fileObject.getCharContent(true).toString();
    }

    public static void saveInMemoryFile(JavaFileObject inMemoryFileObject, String outputPath) throws IOException {
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
