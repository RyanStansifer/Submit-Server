.PHONY	: store

JAVAC := javac
JOPT  := -Xlint:-serial -encoding latin1
DIR   := submit

%.class :       %.java
	$(JAVAC) $(JOPT) $<

default	:
	$(JAVAC) $(JOPT) ${DIR}/*/*.java

jar	: 	client.jar

shared_files := ${DIR}/entry/FileInfo.java ${DIR}/server/Response.java ${DIR}/server/BadPathNameException.java

client.jar	:  default
	jar cvef submit.gui.Main $@ ${DIR}/client/*.class ${DIR}/gui/*.class ${DIR}/shared/*.class resources/


install	:	~/public_html/client.jar 

~/public_html/client.jar :	client.jar
	/bin/cp client.jar ~/public_html
	chmod go=r $@

start	:
	java submit.server.SubmitServer &

server.jar	:  default
	jar cvef submit.server.SubmitServer $@ ${DIR}/server/*.class ${DIR}/email/*.class ${DIR}/shared/*.class

store:
	keytool -genkey -alias ryan -keypass 'pass>6chars' -keystore store -storepass storepass -validity 180

sclient.jar :	client.jar
	jarsigner -keystore store -keypass 'pass>6chars' -storepass storepass -signedjar sclient.jar client.jar ryan

clean	:
	/bin/rm ${DIR}/*/*.class
	/bin/rm ${DIR}/*/*~
