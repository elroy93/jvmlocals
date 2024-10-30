# Set JAVA_HOME path, you can modify this to point to your specific Java version
# JAVA_HOME=/root/.sdkman/candidates/java/11.0.24-amzn

# Determine if the OS is Windows or Linux

UNAME_S := $(shell uname -s)
CC_ARCH =
RM=rm -f
ifeq ($(OS),Windows_NT)
	CXX=g++
	TARGET_LIB=$(fileName_jvmLocalsAgentSo).dll
	JAVA_INCLUDE_PLATFORM=win32
	LDFLAGS=
	RM=del /F /Q
	RMR=del /S /Q
	PLATFORM=Windows
else ifeq ($(UNAME_S),Darwin)
	CXX=g++
	TARGET_LIB=lib$(fileName_jvmLocalsAgentSo).dylib
	JAVA_INCLUDE_PLATFORM=darwin
	LDFLAGS=
	CCFLAGS += -D OSX
	CC_ARCH = -arch x86_64
	PLATFORM=MacOs
else
	CXX=g++
	TARGET_LIB=lib$(fileName_jvmLocalsAgentSo).so
	JAVA_INCLUDE_PLATFORM=linux
	LDFLAGS=-lrt -lpthread
	PLATFORM=Linux
endif

# C++ compiler and compile options
CXXFLAGS=-shared -fPIC -O2 -std=c++11 -I${JAVA_HOME}/include -I${JAVA_HOME}/include/$(JAVA_INCLUDE_PLATFORM)

# Variables
fileName_javaJvmLocals=JvmLocals
fileName_jvmLocalsAgentSo=JvmLocalsAgent
dir_javaSrc=src/main/java
packageName_javaPackage=github.elroy93.jvmlocals

dir_javaFile=$(dir_javaSrc)/$(subst .,/,$(packageName_javaPackage))
path_javaFile=$(dir_javaFile)/$(fileName_javaJvmLocals).java
path_javaHeaderClass = $(dir_javaFile)/$(fileName_javaJvmLocals).class
fileName_agentCpp=$(fileName_jvmLocalsAgentSo).cpp

# Generated files
fileName_targetAgentLib=$(TARGET_LIB)
fileName_targetJavaHeader=github_elroy93_jvmlocals_${fileName_javaJvmLocals}.h

# Java compiler and options
JAVAC=javac
JAVA=java
JAVACFLAGS= -g:lines,vars,source -encoding UTF-8
JAVAFLAGS=-agentpath:./$(TARGET_LIB) -Djava.library.path=./ -XX:+ShowMessageBoxOnError -Xint -XX:-UseCompressedOops -XX:-TieredCompilation

##################################################################################

# Default target, compile the C++ library
all: $(TARGET_LIB)
	@echo "😜 === Compilation Complete on platform=${PLATFORM} uname=${UNAME_S}==="

# Compile the C++ shared library
$(TARGET_LIB): $(fileName_agentCpp) $(fileName_targetJavaHeader)
	$(CXX) $(CXXFLAGS) $(fileName_agentCpp) -o $(TARGET_LIB) $(LDFLAGS) ${CC_ARCH}
	@echo "😜 === Agent C++ Compilation Complete ==="

# Generate JNI header file
$(fileName_targetJavaHeader): $(path_javaFile)
	$(JAVAC) -h . $(path_javaFile)
	$(RM) $(path_javaHeaderClass)
	@echo "😜 === JNI Header Generation Complete ==="

##################################################################################

# Test
test: $(TARGET_LIB)
	gradle test
	@echo "😜 === JNI Program Test Complete ==="

genjni: $(fileName_targetJavaHeader)
	@echo "😜 === JNI Header Generation Complete ==="

# Clean generated files
clean:
	-$(RM) *.so *.dll *.log *.dylib
	-$(RM) $(path_javaHeaderClass)
	-gradle clean --warning-mode all
	@echo "😜 === Clean Complete ==="

# Check ldd 
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