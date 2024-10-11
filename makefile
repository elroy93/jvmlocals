# 设置JAVA_HOME路径, 可以修改为指定的Java版本
# JAVA_HOME=/root/.sdkman/candidates/java/11.0.24-amzn

# C++编译器和编译选项
CXX=g++
CXXFLAGS=-shared -fPIC -O2 -std=c++11 -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
LDFLAGS=-lrt -lpthread

# 变量
JVM_LOCALS=JvmLocals

# 源码文件列表
AGENT_SRC=JvmLocalsExceptionAgent.cpp
JVM_LOCALS_JNI_HEADER_JAVA_SRC=${JVM_LOCALS}.java
JVM_LOCALS_JNI_SO_SRC=${JVM_LOCALS}.cpp
JAVA_TEST_SRC=test/Hello.java
JAVA_TEST_CLASS=test.Hello

# 生成的文件 
AGENT_SO=libJvmLocalsExceptionAgent.so
JVM_LOCALS_JNI_HEADER=github_elroy93_jvmlocals_${JVM_LOCALS}.h
JVM_LOCALS_JNI_SO=lib${JVM_LOCALS}.so

# java的编译阐述
JAVAC=javac
JAVA=java
JAVAFLAGS=-agentpath:./${AGENT_SO} -XX:+ShowMessageBoxOnError -Xint -XX:-UseCompressedOops -XX:-TieredCompilation

##################################################

# 默认目标，编译C++库
all: clean $(AGENT_SO) $(JVM_LOCALS_JNI_SO)
	@echo "=== 编译完成 ==="

# 编译C++共享库
$(AGENT_SO): $(AGENT_SRC)
	$(CXX) $(CXXFLAGS) $(AGENT_SRC) -o $(AGENT_SO) $(LDFLAGS)
	@echo "=== Agent C++ 编译完成 ==="

$(JVM_LOCALS_JNI_HEADER): $(JVM_LOCALS_JNI_HEADER_JAVA_SRC)
	${JAVAC} -encoding UTF-8 -h . $(JVM_LOCALS_JNI_HEADER_JAVA_SRC)
	rm -rf $(JVM_LOCALS).class
	@echo "=== JNI 头文件生成完成 ==="
	
$(JVM_LOCALS_JNI_SO): $(JVM_LOCALS_JNI_HEADER)
	$(CXX) $(CXXFLAGS) $(JVM_LOCALS_JNI_SO_SRC) -o $(JVM_LOCALS_JNI_SO) $(LDFLAGS)
	@echo "=== JNI C++ 编译完成 ==="

# 运行测试程序 (不作为默认编译目标)
test: $(AGENT_SO) $(JVM_LOCALS_JNI_SO)
	@echo "=== 测试程序开始运行 ==="
	# 测试ExceptionAgent
	$(JAVAC) -g:lines,vars,source $(JAVA_TEST_SRC)
	$(JAVA) $(JAVAFLAGS) $(JAVA_TEST_CLASS) a b c
	# 测试JvmLocals动态库
	$(JAVA) -Djava.library.path=./ $(JVM_LOCALS).java
	@echo "=== 测试程序运行完成 ==="

genjni: $(JVM_LOCALS_JNI_HEADER)

# 清理生成的文件
clean:
	rm -f *.so *.class *.h **/*.class
	@echo "=== 清理完成 ==="
