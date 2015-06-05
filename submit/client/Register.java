package submit.client;

import submit.client.Parameters;
import submit.client.ResponseObject;
import submit.shared.Response;

import java.io.*;
import java.net.*;

final public class Register {

   public static void main (String[] args) throws IOException, ClassNotFoundException  {
      System.out.print (analyzedRegister());
   }

   public static Response register (final Parameters p) throws IOException, ClassNotFoundException {
      final String server = p.getServer();
      final int port      = p.getPort();
      final Socket s      = new Socket ();
      s.setSoTimeout (2500);  // Enable SO_TIMEOUT; a read() will block for no more than 2.5 sec
      s.connect (new InetSocketAddress (server,port), 1500);  // 1.5 sec timeout
      final ObjectOutputStream oos = new ObjectOutputStream (s.getOutputStream());
      final ObjectInputStream  ois = new ObjectInputStream  (s.getInputStream());

      p.writeObject ("register", oos);
      final Response r = (submit.shared.Response) ois.readObject ();

      oos.close();
      ois.close();
      
      return r;
   }

   public static ResponseObject analyzedRegister () {
      return analyzedRegister (Parameters.currentParameters());
   }
      
   public static ResponseObject analyzedRegister (final Parameters p) {
      try {
	 return new ResponseObject (register(p));
      } catch (Exception ex) {
	 return new ResponseObject (ex);
      }
   }

   public static boolean missing (Parameters p, String name) {
      final String x = p.getParameter (name);
      return (x==null || x.length()<1);
   }

   public static String checkParameters () {
      return checkParameters (Parameters.currentParameters());
   }

   public static String checkParameters (Parameters p) {
      String message = "";
      if (missing (p, "first_name")) message += "Missing first name\n";
      if (missing (p, "last_name" )) message += "Missing last name\n";
      if (missing (p, "email"     )) message += "Missing e-mail address id\n";
      /*
        Legal name, email!
       */
      if (message.length()<1) {
	 return null;
      } else {
	 return message;
      }
   }


}
