# Set JAVA_HOME path, you can modify this to point to your specific Java version
# JAVA_HOME=/root/.sdkman/candidates/java/11.0.24-amzn

# Determine if the OS is Windows or Linux
PLATFORM=Linux
ifeq ($(OS),Windows_NT)
	# Windows settings
	CXX=g++
	TARGET_LIB=$(fileName_jvmLocalsAgentSo).dll
	JAVA_INCLUDE_PLATFORM=win32
	LDFLAGS=
	RM=del /F /Q
	RMR=del /S /Q
	PLATFORM=Windows
else
	# Linux settings
	CXX=g++
	TARGET_LIB=lib$(fileName_jvmLocalsAgentSo).so
	JAVA_INCLUDE_PLATFORM=linux
	LDFLAGS=-lrt -lpthread
	RM=rm -f
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
	@echo "😜 === Compilation Complete on ${PLATFORM}==="

# Compile the C++ shared library
$(TARGET_LIB): $(fileName_agentCpp) $(fileName_targetJavaHeader)
	$(CXX) $(CXXFLAGS) $(fileName_agentCpp) -o $(TARGET_LIB) $(LDFLAGS)
	@echo "😜 === Agent C++ Compilation Complete ==="

# Generate JNI header file
$(fileName_targetJavaHeader): $(path_javaFile)
	$(JAVAC) -h . $(path_javaFile)
	$(RM) $(fileName_javaJvmLocals).class
	@echo "😜 === JNI Header Generation Complete ==="

##################################################################################

# Test
test: $(TARGET_LIB)
	@echo "😜 === Starting JNI Program Test ==="
	# 😜 Compile Java program use gradle , only show err message 
	gradle build -x test -q
	# 😜 Start to run Java
	$(JAVA) $(JAVAFLAGS) -classpath ./build/classes/java/main $(packageName_javaPackage).$(fileName_javaJvmLocals)
	@echo "😜 === JNI Program Test Complete ==="

genjni: $(fileName_targetJavaHeader)

# Clean generated files
clean:
	-$(RM) *.so *.dll *.log
	gradle clean --warning-mode all
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