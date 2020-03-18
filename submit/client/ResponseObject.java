package submit.client;

import submit.shared.Response;
import submit.shared.BadFileException;

import java.util.Set;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.ConnectException;

public class ResponseObject {
   public final Response r;
   public final Exception ex;
   public final String analysis;

   public boolean getSuccess () {
      return (r!=null && r.success);
   }

   public Set<String> getAvailableClasses () {
      if (r==null || ! r.projectsAvailable()) {
	 return null;
      } else {
	 return r.getAvailableClasses();
      }
   }

   private ResponseObject (final Response r, final Exception ex, final String a) {
      this.r = r;
      this.ex = ex;
      if (a==null) {
	 this.analysis = "Internal error [submit.client.ResponseObject]:  response analysis null!  r="+r+", ex="+ex+"\n";
      } else {
	 this.analysis = a;
      }
   }

   public ResponseObject (Response r)    { this (r, null, r.toString());   }
   public ResponseObject (Exception ex)  { this (null, ex, makeAnalysis (ex));   }

   // Dispatch explicitly based on type of exception
   // Does this work?  Yes, but it is dangerously close to a self-recursive loop

   private static String makeAnalysis (final Exception ex) {
      if      (ex instanceof InvalidClassException)     return makeAnalysis ((InvalidClassException) ex);
      else if (ex instanceof ClassNotFoundException)    return makeAnalysis ((ClassNotFoundException) ex);
      // FileNotFoundException <: IOException
      else if (ex instanceof FileNotFoundException)     return makeAnalysisFNFE ((FileNotFoundException) ex);
      // UnknownHostException <: IOException
      else if (ex instanceof UnknownHostException)      return makeAnalysis ((UnknownHostException) ex);
      else if (ex instanceof SocketTimeoutException)    return makeAnalysis ((SocketTimeoutException) ex);
      // submit.shared.BadFileException <: java.lang.IllegalArgumentException
      else if (ex instanceof BadFileException)          return makeAnalysis ((BadFileException) ex);
      else if (ex instanceof IllegalArgumentException)  return makeAnalysis ((IllegalArgumentException) ex);
      else if (ex instanceof ConnectException)          return makeAnalysis ((ConnectException) ex);
      else if (ex instanceof EOFException)              return makeAnalysis ((EOFException) ex);
      // We really don't want this case as the more unlikely it is, the more info we need
      else return "Internal error [submit.client.ResponseObject]: " + ex.toString() + "\n";
   }

   private static String makeAnalysis (final InvalidClassException ex) {
      String message = "Internal protocol problem\n";
      message += "   " + ex.toString() + "\n";
      message += "Differing versions caused by changes to class files?\n";
      return (message);
   }

   private static String makeAnalysis (ClassNotFoundException ex) {
      String message = "Internal protocol problem\n";
      message += "   " + ex.toString() + "\n";
      message += "Perhaps a compilation problem, or a connection to incompatible submit server.\n";
      return (message);
   }

   private static String makeAnalysis (UnknownHostException ex) {
      String message = "Unable to make a connection to the submit server.\n";
      message += "   " + ex.toString() + "\n";
      message += "The hostname was not recognized, was it misspelled?\n";
      return (message);
   }

   private static String makeAnalysis (SocketTimeoutException ex) {
      String message = "Unable to make a connection to the submit server.\n";
      message += "   " + ex.toString() + "\n";
      message += "The network connection may be slow, the server might be hung, ...\n";
      return (message);
   }

   // FileInfo ()        rasies BadFileException extends IllegalArgumentException
   private static String makeAnalysis (BadFileException ex) {
      String message = "File not transferable.\n";
      message += "   " + ex.toString() + "\n";
      message += "Nothing sent to server.\n";
      return message;
   }

   private static String makeAnalysisFNFE (FileNotFoundException ex) {
      ex.printStackTrace (System.err);
      String message = "File not transferable.\n";
      message += "   " + ex.toString() + "\n";
      message += "Nothing sent to server.\n";
      return message;
   }

   // java.net.Socket () rasies IllegalArgumentException
   private static String makeAnalysis (IllegalArgumentException ex) {
      String message = "Unable to make a connection to the submit server.\n";
      message += "   " + ex.toString() + "\n";
      message += "The port number being used is not a legal Internet port number.\n";
      return message;
   }


   private static String makeAnalysis (final ConnectException ex) {
	 final Parameters p = Parameters.currentParameters();
         final String host = p.getServer();
         final int port = p.getPort();
	 String message = "Unable to make a connection to the submit server.\n";
	 message += "   " + ex.toString() + "\n";
	 message += "Perhaps the Internet is inaccessible at this moment, the host is down, or the server is not running.\n";
         message += "Or maybe the hostname ("+host+") or port ("+port+") is wrong.\n";
         if (!p.isDefaultServer()) {
	    message += "By the way, this is not the default host.  The default host is " + Defaults.DEFAULT_SERVER + "\n";
	 }
         if (port != Defaults.DEFAULT_PORT) {
	    message += "By the way, the port number "+port+" is not the default port.\n";
            message += "The default port is " + Defaults.DEFAULT_PORT + "; try using it instead.\n";
	 }
	 return message;
   }

   private static String makeAnalysis (final EOFException ex) {
      String message = "Problem!  Internal server error?\n";
      message += "   " + ex.toString() + "\n";
      if (ex.getCause()!=null) {
         message += String.format ("   %s\n", ex.getCause());
      }
      return (message);
   }

   public String toString () { return analysis; }
}
