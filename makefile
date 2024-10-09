# 设置JAVA_HOME路径, 可以修改为指定的Java版本
# JAVA_HOME=/root/.sdkman/candidates/java/11.0.24-amzn

# C++编译器和编译选项
CXX=g++
CXXFLAGS=-shared -fPIC -O2 -std=c++11 -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
LDFLAGS=-lrt -lpthread

# 目标输出文件
TARGET=libJvmLocals.so
SRC=src/JvmLocals.cpp

# Java编译器和运行时 (只用于测试)
JAVAC=javac
JAVA=java
JAVAFLAGS=-agentpath:./libJvmLocals.so -XX:+ShowMessageBoxOnError -Xint -XX:-UseCompressedOops -XX:-TieredCompilation
JAVA_SRC=test/Hello.java
JAVA_CLASS=test.Hello

# 默认目标，编译C++库
all: $(TARGET)

# 编译C++共享库
$(TARGET): $(SRC)
	$(CXX) $(CXXFLAGS) $(SRC) -o $(TARGET) $(LDFLAGS)
	@echo "=== C++ 编译完成 ==="

# 运行测试程序 (不作为默认编译目标)
test: $(TARGET)
	@echo "=== 测试程序开始运行 ==="
	$(JAVAC) -g:lines,vars,source $(JAVA_SRC)
	$(JAVA) $(JAVAFLAGS) $(JAVA_CLASS) a b c
	@echo "=== 测试程序运行完成 ==="

# 清理生成的文件
clean:
	rm -f $(TARGET) **/*.class
	@echo "=== 清理完成 ==="
