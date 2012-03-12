## Makefile for the TAPAAL project (Mac)

#Copyright (c) 2009-2011, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
#All rights reserved.
#
#Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
#
#    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
#    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
#    * Neither the name of the TAPAAL nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
#
#THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.   
#

# Location of trees.
SOURCE_DIR  := src
RESOURCE_DIR := $(SOURCE_DIR)/resources
OUTPUT_DIR  := classes

RELEASE_DIR := release-version
RELEASEJAR_DIR := releasejar-version

PROJECTNAME := TAPAAL

JAR_DIR := buildjar
JAR_FILE := $(JAR_DIR)/tapaal.jar

#Set location of java home
ifeq (exists, $(shell [ -d /usr/lib/jvm/default-java ]  && echo exists ))
	JAVA_HOME := /usr/lib/jvm/default-java
endif

ifeq (exists, $(shell [ -d /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home ]  && echo exists ))
	JAVA_HOME := /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
endif

JAVA_LIB  := $(JAVA_HOME)/lib

DEPEND_DIR := libs
DEPEND := $(DEPEND_DIR)/\*

JFLAGS      := -sourcepath $(SOURCE_DIR) \
		-cp $(DEPEND)

# Unix tools
AWK         := awk
FIND        := /usr/bin/find
MKDIR       := mkdir -p
RM          := rm -rf
SHELL       := /bin/bash
all_javas := /tmp/tmp

# Java tools

JAVA        := $(JAVA_HOME)/bin/java
JAVAC       := $(JAVA_HOME)/bin/javac
JAR       := $(JAVA_HOME)/bin/jar

	MAINCLASS := $(SOURCE_DIR)/TAPAAL
default: ${MAINCLASS}.class


%.class : %.java
	$(JAVAC) $(JFLAGS) $*.java


$(all_javas):
	$(FIND) $(SOURCE_DIR) -name '*.java' > $@
	sed -ie 's#\.java#\.class#' $@

.PHONY : clean
clean:
	@rm -f ${all_javas}
	@find . -name '*.class' | xargs -L 1 rm -f
	@rm -f tapaal_version.orig.tar.gz
	@rm -rf '${OUTPUT_DIR}'
	@rm -rf '$(RELEASE_DIR)'
	@rm -rf '$(JAR_DIR)'
	@rm -rf '$(RELEASEJAR_DIR)'


release: clean 
	@mkdir $(OUTPUT_DIR)
	@mkdir $(RELEASE_DIR)
	$(JAVAC) $(JFLAGS) -d $(OUTPUT_DIR)/ '$(MAINCLASS).java'
	cp -R $(OUTPUT_DIR)/* '$(RELEASE_DIR)'
	cp -R '$(RESOURCE_DIR)' '$(RELEASE_DIR)'
	cp -R $(DEPEND_DIR)/* '$(RELEASE_DIR)'

releasejar: clean jar
	@mkdir $(RELEASEJAR_DIR)
	cp '$(JAR_FILE)' '$(RELEASEJAR_DIR)'
	cp -R $(DEPEND_DIR)/* '$(RELEASEJAR_DIR)'


jar: release 
	@mkdir $(JAR_DIR)
	$(JAR) -cfe $(JAR_FILE) TAPAAL -C $(RELEASE_DIR) . 

#Remove BYTE ORDER MARK
removeBOM:  
	sed -i '1 s/^\xef\xbb\xbf//' $*.java

TOOLS_DIR := tools

install: release
	mkdir -p $(DESTDIR)/usr/lib/tapaal/
	cp -R $(CURDIR)/$(RELEASE_DIR)/* $(DESTDIR)/usr/lib/tapaal/
	cp -R $(CURDIR)/$(TOOLS_DIR)/run-unix $(DESTDIR)/usr/bin/tapaal

