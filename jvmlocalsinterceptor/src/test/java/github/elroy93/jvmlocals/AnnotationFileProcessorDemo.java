package github.elroy93.jvmlocals;

import com.google.common.collect.ImmutableSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

final class AnnotationFileProcessorDemo extends AbstractProcessor {

  private ProcessingEnvironment processingEnv = null;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // 获取所有根元素
    Set<? extends Element> rootElements = roundEnv.getRootElements();

    // 遍历所有根元素
    for (Element element : rootElements) {
      // 获取元素所在的包名
      String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
      // 获取元素的简单名称（类名、接口名等）
      String elementName = element.getSimpleName().toString();
      // 构造完整的文件名
      String fileName = packageName + "." + elementName;
      // 打印文件名
      System.out.println("Processing file: " + fileName);

      // 修改文件, 如果调用的是 test.Callee#execute , 则在这个之前插入调用test.Callee#beforeExecute的代码, 这是个静态函数
      // 需要注意roundEnv, 不要多次插入代码


    }

    return false;
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
