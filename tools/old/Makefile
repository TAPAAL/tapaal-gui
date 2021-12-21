## Makefile for the TAPAAL project

#Copyright (c) 2009, Kenneth Yrke Jørgensen <kyrke@cs.aau.dk>
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
SOURCE_DIR  := .
OUTPUT_DIR  := classes



PROJECTNAME := TAPAAL

JAVA_HOME := /usr/lib/jvm/default-java
JAVA_LIB  := $(JAVA_HOME)/lib


DEPEND := jpowergraph-0.2-common.jar:jpowergraph-0.2-swing.jar:gtkjfilechooser.jar:gtkjfilechooser.jar:GOLDEngine.jar:commons-cli-1.2.jar


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

default: RunGui.class

%.class : %.java
	$(JAVAC) $(JFLAGS) $*.java


$(all_javas):
	$(FIND) $(SOURCE_DIR) -name '*.java' > $@
	sed -ie 's#\.java#\.class#' $@

#all: $(all_javas)
#	for arg in `cat $<`; do make $${arg}; done

buildsrc: clean
	tar czf tapaal_version.orig.tar.gz Makefile pipe/ jpowergraph-0.2-common.jar jpowergraph-0.2-swing.jar gtkjfilechooser.jar schema/ jpowergraph/ java_cup/ jama/ Images/ expressions/  Example\ nets/ Docs/ dk/ cfg/ run-unix/  xslt/ RunGui.java

install: $(compile)
	mkdir -p $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/classes/* $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/Images $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/Example\ nets $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/Docs $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/expressions $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/jama $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/java_cup $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/schema $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/xslt $(DESTDIR)/usr/lib/tapaal/
	cp -r $(CURDIR)/jpowergraph $(DESTDIR)/usr/lib/tapaal/
	cp $(CURDIR)/jpowergraph-0.2-common.jar $(DESTDIR)/usr/lib/tapaal/ 
	cp $(CURDIR)/jpowergraph-0.2-swing.jar $(DESTDIR)/usr/lib/tapaal/ 
	cp $(CURDIR)/gtkjfilechooser.jar $(DESTDIR)/usr/lib/tapaal/ 
	cp $(CURDIR)/run-unix $(DESTDIR)/usr/bin/tapaal

.PHONY : clean
clean:
	@rm -f ${all_javas}
	@find . -name '*.class' | xargs -l rm -f
	@rm -f tapaal_version.orig.tar.gz
	@rm -rf ${OUTPUT_DIR}

release: clean 
	@mkdir $(OUTPUT_DIR)
	$(JAVAC) $(JFLAGS) -d $(OUTPUT_DIR)/ TAPAAL.java


#Remove BYTE ORDER MARK
removeBOM:  
	sed -i '1 s/^\xef\xbb\xbf//' $*.java
