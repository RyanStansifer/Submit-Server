package submit.server;

import submit.email.Mail;

import submit.shared.FileInfo;
import submit.shared.Manifest;
import submit.shared.Registration;
import submit.shared.Response;

import submit.server.Projects;

import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.io.*;
import java.util.jar.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Properties;
import java.util.Date;
import java.util.Map;
import java.util.Iterator;

public class Connection implements Runnable {

   private final static int TIME_OUT = 9000; // 9 seconds

   private final Socket client;
   private ObjectInputStream ois;
   private ObjectOutputStream oos;

   public Connection (Socket client_socket) throws IOException {
      client = client_socket;
      ois = new ObjectInputStream  (client.getInputStream());  // where is this closed?
      oos = new ObjectOutputStream (client.getOutputStream());

      /* Socket timeout will be 0 (infinity), unless we set it. */
      /* Possibly throws java.net.SocketException subclass of java.io.IOException */
      client.setSoTimeout (TIME_OUT);  /* 8.5 seconds; what is reasonable? */
   }


   private Properties createProperties (Object o) throws ClassNotFoundException, IOException, javax.crypto.BadPaddingException {
      if (o instanceof javax.crypto.SealedObject) {
         // Could return null
         return ((Properties)submit.shared.Encryption.unsealObject ((javax.crypto.SealedObject)o));
      } else if (o instanceof Properties) {
         return (Properties)o;
      } else {
         if (SubmitServer.VERBOSE>0) System.out.println ("The client sent an object of an unexpected type.");
         return null;
      }
   }

   private Parameters args;  // non-local input to the commands
   private Response resp = new Response ();  // non-local output from the commands

   public void run () {
      if (ois==null) return;  // construction failed and reported, so just give up
      try {
         // Could raise SocketException "connection reset"  
         final Object o = ois.readObject();         //  1. Read properties

         // No reverse name service lookup is performed, so hostname may be empty string
         final String host = client.getInetAddress().toString();

         args = new Parameters (host, createProperties (o));

         if (SubmitServer.VERBOSE>0) {
            System.out.println (args);
         }

         SubmitServer.db.activateRegistration (args.getControl());   //  if needed

         final String cmd = args.getProperty("command", "[none]");

         try {
            if (cmd.equals ("submit")) {
               SubmitCommand.submit (args, resp, ois, oos);
            } else if (cmd.equals ("ping")) {
               PingAction.ping (args,resp);
            } else if (cmd.equals ("register")) {
               register ();
            } else if (cmd.equals ("mail")) {
               mail ();
            } else if (cmd.equals ("stop")) {
               // No graceful way to stop
            } else if (cmd.equals ("create")) {
               CreateProject.create (args, resp);
            } else if (cmd.equals ("turnon")) {
               CreateProject.turnon (args, resp);
            } else if (cmd.equals ("turnoff")) {
               CreateProject.turnoff (args, resp);
            } else if (cmd.equals ("report")) {
               CreateProject.report (args, resp);
            } else if (cmd.equals ("status")) {
               CreateProject.status (args, resp);
            } else if (cmd.equals ("download")) {
               Download.download (args, resp, ois, oos);
               if (resp.success) resp=null;  // signal end of protocol
            } else if (cmd.equals ("bad")) {
               resp.success = false;
               resp.add_line ("Client protocol error.  Bad version of client?");
            } else {
               System.out.println ("command '"+cmd+"' not understood");
               resp.add_line ("command '"+cmd+"' not understood");
            }

         } catch (Exception ex) {
            reportException (ex);
            resp.success = false;
            resp.add_line ("The "+cmd+" command failed");
         }

         if (resp!=null) {
            if (SubmitServer.VERBOSE>1) {
               System.out.println ("Response to client:  ");
               System.out.println (resp);
            }
            assert (resp.toString()!=null);
            resp.projects = Projects.allProjects(SubmitServer.db.root);
            oos.writeObject (resp);
         }
         oos.close();   // necessary or client hangs!

      } catch (SocketException ex) {
         //Unavoidable, unfixable, unremarkable; don't bother to report
         //reportException (ex);
      } catch (IOException ex) {
         // Including SocketTimeoutException
         reportException (ex);
      } catch (Exception ex) {
         reportException (ex);
      }
      /* Let the submit server close the socket.  */
   }


   private void register () {
      final String first_name = args.getProperty ("first_name");
      final String last_name  = args.getProperty ("last_name");
      final String email      = args.getProperty ("email");

      if (first_name==null || last_name==null || email==null) {
         resp.success = false;
         resp.add_line ("Registration failed: insufficient information.");
         return;
      }

      /*
        First name, last name, email address all well-formed?
      */

      resp.success = false;
      try {
         // Try registering; if it succeeds the data base has been saved to the file
         // system, if it fails an exception is raised.

         // Communicate control code to the new registered user via email.
         // final String code  =
         SubmitServer.db.register (last_name, first_name, email);

         /*
           We don't know if this is the first or a repeat registration.
         */

         resp.success = true;
         resp.add_line ("You have successfully registered.");
         resp.add_line ("The control code will be sent to the e-mail address you provided.  This may");
         resp.add_line ("take a long time to reach you, if network or e-mail circumstances are adverse.");
         resp.add_line ("If your name or e-mail was incorrectly given, you may register again and");
         resp.add_line ("receive a new control code.  The old one becomes invalid.");
         resp.add_line ("Contacting the server again with the control code activates your registration. ");
         resp.add_line ("Please do not register again after activating your registration.");

      } catch (IOException ex) {
         resp.add_line ("Registration failed: "+ex.getMessage()+".");
      } catch (IllegalArgumentException ex) {
         resp.add_line ("Registration failed: "+ex.getMessage());
      } catch (RuntimeException ex) {
         resp.add_line ("Registration failed: "+ex.getMessage());
         resp.add_line ("Please do not register more than once.");
      } catch (Exception ex) {
         reportException (ex);
         resp.add_line ("Registration failed: "+ex.toString());
      }
   }

   private void mail () {
      if (! SubmitServer.db.isRegistered (args.getControl())) {
         final String ln = args.getProperty("last_name");
         final String fn = args.getProperty("first_name");
         if (ln!=null && fn!=null) {
            final Registration r = SubmitServer.db.isRegistered (fn, ln);
            if (r==null) {
               resp.success = false;
               resp.add_line ("'"+fn + " " +ln+"' is not registered.");
               resp.add_line ("First and last names must be exactly like the original registration.");
            } else if (!r.isActivated()) {
               // Very important not to send the control code before the registration
               // has been activated.  Otherwise anyone can steal the control code during
               // that period after registration and before activation.
               resp.success = false;
               resp.add_line ("Must activate registration first, or register again.");
            } else {
               final String recipient = r.getEMail();
               if (recipient==null) {
                  resp.success = false;
                  resp.add_line ("No e-mail address registered with server.");
               } else {
                  try {
                     Connection.mail (r, "submit server help", r.getHelpMessage());
                     resp.success = true;
                     resp.add_line ("The control code for "+ r.fullName() +" has been sent to the previously registered e-mail address.");
                     if (SubmitServer.VERBOSE>1) {
                        System.out.printf ("Help sent to:  %s%n", r);
                     }

                  } catch (IOException ex) {
                     reportException (ex);
                     resp.success = false;
                     resp.add_line ("System error in sending e-mail");
                  }
               }
            }
         } else {
            resp.success = false;
            resp.add_line ("Full name not provided");
         }
      } else {
         // Control code provided and is registered in DB
         resp.success = false;
         resp.add_line ("Control code was provided, so no point in e-mailing it.");
      }
   }

   private static final DateFormat format = new SimpleDateFormat ("EEE, dd MMM yyyy 'at' hh:mm a z");
   static String date () { return format.format (new Date()); }

   static void mail (final Registration r, final String subject, final String message) throws IOException {
      final String email = r.getEMail();
      if (email==null) throw new RuntimeException ("null e-mail address");
      final String name = r.fullName();
      if (name==null) throw new RuntimeException ("null e-mail recepient");
      final String from = "sysadmin@cs.fit.edu";
      if (SubmitServer.VERBOSE>5) {
         System.out.printf ("host=%s%n", SubmitServer.db.mailhost);
         System.out.printf ("email=%s%n", email);
         System.out.printf ("name=%s%n", name);
         System.out.printf ("subject=%s%n", subject);
         System.out.printf ("from=%s%n", from);
      }
      Mail.main (SubmitServer.db.mailhost, email, name, subject, from, message);
   }

   private boolean isInDirectory (File f, File d) throws IOException {
      // Assume d is directory!  Not OS independent!
      final String f_name = f.getCanonicalPath();
      final String d_name = f.getCanonicalPath();
      return (f_name.startsWith (d_name));
   }

   private void reportException (Exception ex) {
      if (SubmitServer.VERBOSE>0) {
         System.out.printf ("submit.server.Connection.reportException(): EXCEPTION at %s%n", date());
         if (client==null) {
            System.out.println ("Null client socket.");
         } else {
            System.out.println (client);
         }
         if (args==null) {
            System.out.println ("Null client parameters.");
         } else {
            System.out.println (args);
         }
         ex.printStackTrace (System.out);
         System.out.println ();
      }
   }

   static void closeIgnoringException (Closeable c) {
      if (c!=null) {
         try {
            c.close();
         } catch (IOException ex) {
            // There is nothing we can do if close() fails
         }
      }
   }
}
