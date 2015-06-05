package submit.client;

import submit.client.Parameters;
import submit.client.ResponseObject;
import submit.shared.Response;

import java.io.*;
import java.net.*;

final public class Mail {

   public static void main (String[] args)   {
      System.out.print (analyzedMail());
   }

   public static Response mail (final Parameters p) throws IOException, ClassNotFoundException {
      final String server = p.getServer();
      final int port      = p.getPort();
      final Socket s      = new Socket (server, port);
      final ObjectOutputStream oos = new ObjectOutputStream (s.getOutputStream());
      final ObjectInputStream  ois = new ObjectInputStream  (s.getInputStream());

      p.writeObject ("mail", oos);
      final Response r = (submit.shared.Response) ois.readObject ();
      oos.close();
      ois.close();
      return r;
   }

   public static ResponseObject analyzedMail () {
      return analyzedMail (Parameters.currentParameters());
   }

   public static ResponseObject analyzedMail (final Parameters p) {
      try {
	 return new ResponseObject (mail(p));
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
      if (missing (p, "first_name")) message += "Missing first name name\n";
      if (missing (p, "last_name" )) message += "Missing last name\n";
      if (message.length()<1) {
	 return null;
      } else {
	 return message;
      }
   }


}
