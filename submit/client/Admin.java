package submit.client;

import submit.shared.Response;

import submit.client.Parameters;
import submit.client.ResponseObject;

import java.io.*;
import java.net.*;

final public class Admin {

   public static void main (String[] args)   {
      final String command = System.getProperty ("command");
      if (command!=null && command.equals ("download")) {
         Download.main (args);
      } else {
         // Don't use Admin class for downloading jar files
         System.out.print (analyzedAdmin());
      }
   }

   public static Response admin () throws IOException, ClassNotFoundException {
      return admin (Parameters.currentParameters());
   }

   public static Response admin (final Parameters p) throws IOException, ClassNotFoundException {
      final String server = p.getServer();
      final int port      = p.getPort();
      final Socket s      = new Socket ();
      s.setSoTimeout (4500);  // Enable SO_TIMEOUT; a read() will block for no more than 4.5 sec
      s.connect (new InetSocketAddress (server,port), 4500);  // 4.5 sec timeout
      final ObjectOutputStream oos = new ObjectOutputStream (s.getOutputStream());
      final ObjectInputStream  ois = new ObjectInputStream  (s.getInputStream());

      //      final String command = System.getProperty ("command", "report");
      // If no command is given the default is "status"
      final String command = System.getProperty ("command", "status");
      p.writeObject (command, oos);
      final Response r = (submit.shared.Response) ois.readObject ();
      oos.close();
      ois.close();
      return r;
   }

   public static ResponseObject analyzedAdmin () {
      try {
	 return new ResponseObject (admin());
      } catch (Exception ex) {
	 return new ResponseObject (ex);
      }
   }

}
