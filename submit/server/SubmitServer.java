package submit.server;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

import java.util.Date;
import java.util.ArrayList;

public final class SubmitServer  {

   private static final int    DEFAULT_PORT = 13263;

   /*
     Parameter "VERBOSE" for controlling messages
   */
   public static final int VERBOSE;
   static {
      final String v = System.getProperty ("verbose");
      if (v==null) {
	 VERBOSE = 1;
      } else if (v.equals ("")) {
	 VERBOSE = 2;
      } else {
	 int x=2;
	 try {
	    x = Integer.parseInt (v);
	 } catch (NumberFormatException ex) {
	    x = 2;
	    ex.printStackTrace (System.err);
	    System.err.println ("Property \"verbose\" must be an integer; found -Dverbose=\""+v+"\"");
	    System.err.println ("Continuing as if -Dverbose=\""+x+"\"");
	 } finally {
	    VERBOSE = x;
	 }
      }
   }

   // Initialize parameter "HOST" for identifying the user
   private final static String HOST;
   static {
      String x = null;
      try {
	 x = InetAddress.getLocalHost().toString();
      } catch (Exception e) {
	 x = "[unknown]";
      }
      HOST = x;
   }

   public static void printStars () {
      System.out.println ("*********************************************************");
   }

   // Informational Banner
   static {
      if (VERBOSE>=1) {
	 printStars();
	 System.out.printf  ("Module to implement the submit server (verbosity %d)%n", VERBOSE);
	 System.out.printf  ("Date and time:  %s%n", new java.util.Date());
	 System.out.println ("Java version: " + System.getProperty ("java.version") +
	   " from " + System.getProperty ("java.vendor") + "  API version: " +
	   System.getProperty ("java.class.version"));
	 System.out.printf ("Running on host %s by %s%n", HOST, System.getProperty ("user.name"));
	 System.out.println ("Operating system: " + System.getProperty ("os.name") +
           " " + System.getProperty ("os.version") +
           " on " + System.getProperty ("os.arch"));
	 System.out.println ("The current directory is \""+System.getProperty("user.dir")+"\"");
	 printStars();
      }
   }

   /* Although 'db' is not used in 'SubmitServer', it is important
      to immediately load the Database file to make sure there are no
      errors.  OW the program could have problems with many of the necessary
      dynamically loaded classes, yet no error would appear until someone
      connects to the server. */

   public static final Database db;
   static {
      Database temp = null;
      try {
	 temp = Database.init ();
      } catch (Exception ex) {
	 ex.printStackTrace (System.err);
	 System.err.println ("Error.  Could not open or create database.");
	 temp = null;
      } finally {
	 db = temp;
      }
   }

   public static File getCourseDirectory (String course) throws NullPointerException {
      return new File (db.root, course);
   }

   public static File getRealCourseDirectory (String course) {
      try {
         return new File (db.root, course).getCanonicalFile();
      } catch (IOException ex) {
         return null;
      }
   }

   public static File getProjectDirectory (File course, String project) {
      return new File (course, project);
   }
      
   public static File getProjectDirectory (String c, String proj) {
      return new File (SubmitServer.getCourseDirectory (c), proj);
   }



   private static boolean cont = true;
   public static void haltServer () { cont=false; }

   public static void main (String args[]) throws IOException {
      int port = DEFAULT_PORT;
      try {
	 final String port_string = System.getProperty("port");
	 if (port_string != null) {
	    port = Integer.parseInt(port_string);
	 }

      } catch (NumberFormatException e) {
	 System.err.println ("Argument was not a number,");
	 System.err.println ("so using default port instead.");
	 port = DEFAULT_PORT;
      }
   
      if (db == null) {
	 System.err.println ("No database; no point in continuing.");
	 return;
      }

      System.out.println ("Starting SubmitServer at "+new Date()+" on port="+port+".");

      ServerSocket listen_socket=null;  // closed in 'finally'
      try {
	 listen_socket = new ServerSocket(port);
	 while (cont) {

	    // Socket.accept occasionally raises: java.net.SocketException: Software caused connection abort
	    // Some people think we can try again
            Socket client_socket=null;  // closed in 'finally'
	    try {
	       client_socket = listen_socket.accept();

	       // fork off an independent thread to deal with the client
	       final Thread t = new Thread (new Connection (client_socket));  // construct and start thread
               t.start();
	       t.join ();  // One connection at a time makes everything simpler

	    } catch (InterruptedException e) {
	       e.printStackTrace (System.out);
	       // continue ...

	    } catch (SocketException e) {
	       e.printStackTrace (System.out);
	       // continue ...

	    } catch (IOException e) {
               // eg., EOFException raised by 'Connection' with a telnet connection
               if (SubmitServer.VERBOSE>0) {
                  System.out.printf ("SubmitServer at %s got exception=%s%n", new Date(), e);
               }
	       // continue ...

	    } finally {
               if (client_socket!=null) client_socket.close();
            }

	 } // while loop

      } catch (IOException e) {
	 e.printStackTrace (System.out);
	 // We are halting!
      } finally {
         // Even though we are halting anyway ...
         if (listen_socket!=null) listen_socket.close();
      }
      System.out.println ("Halting SubmitServer at "+new Date()+" on port="+port+".");
   }
   
}
