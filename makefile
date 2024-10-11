# 设置JAVA_HOME路径, 可以修改为指定的Java版本
# JAVA_HOME=/root/.sdkman/candidates/java/11.0.24-amzn

# C++编译器和编译选项
CXX=g++
CXXFLAGS=-shared -fPIC -O2 -std=c++11 -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
LDFLAGS=-lrt -lpthread

# 变量
JVM_LOCALS=JvmLocals
JVM_LOCALS_AGENT=JvmLocalsAgent

# 源码文件列表
AGENT_SRC=${JVM_LOCALS_AGENT}.cpp
JVM_LOCALS_JNI_HEADER_JAVA_SRC=github/elroy93/jvmlocals/${JVM_LOCALS}.java
JVM_LOCALS_JNI_SO_SRC=${JVM_LOCALS}.cpp
JAVA_TEST_SRC=test/Hello.java
JAVA_TEST_CLASS=test.Hello

# 生成的文件 
AGENT_SO=lib${JVM_LOCALS_AGENT}.so
JVM_LOCALS_JNI_HEADER=github_elroy93_jvmlocals_${JVM_LOCALS}.h
JVM_LOCALS_JNI_SO=lib${JVM_LOCALS}.so

# java的编译阐述
JAVAC=javac
JAVA=java
JAVAFLAGS=-agentpath:./${AGENT_SO} -XX:+ShowMessageBoxOnError -Xint -XX:-UseCompressedOops -XX:-TieredCompilation

##################################################

# 默认目标，编译C++库
all: $(AGENT_SO) $(JVM_LOCALS_JNI_SO)
	@echo "=== 编译完成 ==="

# 编译C++共享库
$(AGENT_SO): $(AGENT_SRC) $(JVM_LOCALS_JNI_HEADER)
	$(CXX) $(CXXFLAGS) $(AGENT_SRC) -o $(AGENT_SO) $(LDFLAGS)
	@echo "=== Agent C++ 编译完成 ==="

$(JVM_LOCALS_JNI_HEADER): $(JVM_LOCALS_JNI_HEADER_JAVA_SRC)
	${JAVAC} -encoding UTF-8 -h . $(JVM_LOCALS_JNI_HEADER_JAVA_SRC)
	rm -rf $(JVM_LOCALS).class
	@echo "=== JNI 头文件生成完成 ==="
	
$(JVM_LOCALS_JNI_SO): $(JVM_LOCALS_JNI_HEADER)
	$(CXX) $(CXXFLAGS) $(JVM_LOCALS_JNI_SO_SRC) -o $(JVM_LOCALS_JNI_SO) $(LDFLAGS)
	@echo "=== JNI C++ 编译完成 ==="

##################################################################################

test: $(AGENT_SO) $(JVM_LOCALS_JNI_SO)
	@echo "=== 测试jni程序开始运行 ==="
	# 测试JvmLocals动态库, 使用agent加载jvmti, 通过jni调用动态库
	$(JAVAC) -g:lines,vars,source ./github/elroy93/jvmlocals/$(JVM_LOCALS).java
	$(JAVA) $(JAVAFLAGS) -Djava.library.path=./ github/elroy93/jvmlocals/$(JVM_LOCALS)
	@echo "=== 测试jni程序运行完成 ==="

genjni: $(JVM_LOCALS_JNI_HEADER)

# 清理生成的文件
clean:
	rm -f *.so *.class *.h *.log
	find . -name "*.class" -type f -delete
	@echo "=== 清理完成 ==="
