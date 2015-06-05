package submit.gui;

import submit.client.Parameters;

public final class WriteScripts {
   
   public static String unixShScript (Parameters p) {
      final StringBuffer b = new StringBuffer ();
      b.append ("#!/bin/sh\n");
      b.append ("JAVA=java;JAR=./client.jar\n");
      b.append ("PROJECT=$1;shift;FILES=$*\n");
      b.append ("$JAVA");
      b.append (" -Dserver="+p.getParameter("server"));
      b.append (" -Dport="+p.getParameter("port"));
      b.append ("\\\n");
      b.append (" -Dfirst_name="+p.getParameter("first_name"));
      b.append (" -Dlast_name="+p.getParameter("last_name"));
      b.append (" -Did="+p.getParameter("id"));
      b.append ("\\\n");
      b.append (" -Dcode="+p.getParameter("code"));
      b.append ("\\\n");
      b.append (" -Dclass="+p.getParameter("class"));
      b.append (" -Dproject=$PROJECT -Dfiles_only\\\n");
      b.append ("   -classpath $JAR submit.client.Submit $FILES\n");
      return b.toString();
   }
} 
