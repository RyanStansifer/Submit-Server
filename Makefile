JAVAC := javac
JOPT  := -Xlint:-serial -encoding us-ascii
DIR   := submit

%.class :       %.java
	$(JAVAC) $(JOPT) $<

all : server.jar client.jar

default	:
	$(JAVAC) $(JOPT) ${DIR}/*/*.java

shared_files := ${DIR}/entry/FileInfo.java ${DIR}/server/Response.java ${DIR}/server/BadPathNameException.java

client.jar	:  default
	jar cef submit.client.Ping $@ ${DIR}/client/*.class ${DIR}/shared/*.class resources/

server.jar	:  default
	jar cef submit.server.SubmitServer $@ ${DIR}/server/*.class ${DIR}/email/*.class ${DIR}/shared/*.class

clean	:
	/bin/rm -f ${DIR}/*/*.class
	/bin/rm -f ${DIR}/*/*~
	/bin/rm -f client.jar server.jar
