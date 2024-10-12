# Set JAVA_HOME path, you can modify this to point to your specific Java version
# JAVA_HOME=/root/.sdkman/candidates/java/11.0.24-amzn

# Determine if the OS is Windows or Linux
PLATFORM=Linux
ifeq ($(OS),Windows_NT)
	# Windows settings
	CXX=g++
	TARGET_LIB=$(JVM_LOCALS_AGENT).dll
	JAVA_INCLUDE_PLATFORM=win32
	LDFLAGS=
	RM=del /F /Q
	RMR=del /S /Q
	PLATFORM=Windows
else
	# Linux settings
	CXX=g++
	TARGET_LIB=lib$(JVM_LOCALS_AGENT).so
	JAVA_INCLUDE_PLATFORM=linux
	LDFLAGS=-lrt -lpthread
	RM=rm -f
endif

# C++ compiler and compile options
CXXFLAGS=-shared -fPIC -O2 -std=c++11 -I${JAVA_HOME}/include -I${JAVA_HOME}/include/$(JAVA_INCLUDE_PLATFORM)

# Variables
JVM_LOCALS_JAVA_FILE_NAME=JvmLocals
JVM_LOCALS_AGENT=JvmLocalsAgent
JAVA_DIR_SRC_PATH=src/main/java
JAVA_PACKAGE_NAME=github.elroy93.jvmlocals
JAVA_PACKAGE_PATH=$(subst .,/,$(JAVA_PACKAGE_NAME))
JAVA_DIR_PATH=$(JAVA_DIR_SRC_PATH)/$(JAVA_PACKAGE_PATH)

# Source file list
SRC_AGENT_CPP=$(JVM_LOCALS_AGENT).cpp
SRC_TARGET_FILE_JVM_LOCALS_JNI_HEADER_JAVA=$(JAVA_DIR_PATH)/$(JVM_LOCALS_JAVA_FILE_NAME).java

# Generated files
TARGET_FILE_AGENT=$(TARGET_LIB)
TARGET_FILE_JVM_LOCALS_JNI_HEADER=github_elroy93_jvmlocals_${JVM_LOCALS_JAVA_FILE_NAME}.h

# Java compiler and options
JAVAC=javac
JAVA=java
JAVACFLAGS= -g:lines,vars,source -encoding UTF-8
JAVAFLAGS=-agentpath:./$(TARGET_LIB) -Djava.library.path=./ -XX:+ShowMessageBoxOnError -Xint -XX:-UseCompressedOops -XX:-TieredCompilation

##################################################################################

# Default target, compile the C++ library
all: $(TARGET_LIB)
	@echo "😜 === Compilation Complete on ${PLATFORM}==="

# Compile the C++ shared library
$(TARGET_LIB): $(SRC_AGENT_CPP) $(TARGET_FILE_JVM_LOCALS_JNI_HEADER)
	$(CXX) $(CXXFLAGS) $(SRC_AGENT_CPP) -o $(TARGET_LIB) $(LDFLAGS)
	@echo "😜 === Agent C++ Compilation Complete ==="

# Generate JNI header file
$(TARGET_FILE_JVM_LOCALS_JNI_HEADER): $(SRC_TARGET_FILE_JVM_LOCALS_JNI_HEADER_JAVA)
	$(JAVAC) -h . $(SRC_TARGET_FILE_JVM_LOCALS_JNI_HEADER_JAVA)
	$(RM) $(JVM_LOCALS_JAVA_FILE_NAME).class
	@echo "😜 === JNI Header Generation Complete ==="

##################################################################################

# Test
test: $(TARGET_LIB)
	@echo "😜 === Starting JNI Program Test ==="
	$(JAVAC) $(JAVACFLAGS) $(SRC_TARGET_FILE_JVM_LOCALS_JNI_HEADER_JAVA)
	$(JAVA) $(JAVAFLAGS) -classpath $(JAVA_DIR_SRC_PATH) $(JAVA_PACKAGE_NAME).$(JVM_LOCALS_JAVA_FILE_NAME)
	@echo "😜 === JNI Program Test Complete ==="

genjni: $(TARGET_FILE_JVM_LOCALS_JNI_HEADER)

# Clean generated files
clean:
	-$(RM) *.so *.dll *.class *.log
ifeq ($(OS),Windows_NT)
	-$(RMR) *.class
else
	find . -name "*.class" -type f -delete
endif
	@echo "😜 === Clean Complete ==="

check_ldd: $(TARGET_LIB)
ifeq ($(OS),Windows_NT)
	@echo "😜 ldd command is not available on Windows"
else
	# 😜 ldd version
	ldd --version
	# 😜 gcc version
	$(CXX) --version
	# 😜 Show dynamic library dependencies
	ldd $(TARGET_LIB)
endif