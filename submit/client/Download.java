package submit.client;

import submit.client.Parameters;
import submit.client.ResponseObject;

import submit.shared.Encryption;
import submit.shared.Response;

import java.io.*;
import java.net.*;

import javax.crypto.CipherInputStream;


final public class Download {

   private static final byte[] buffer = new byte[4096];
   private static OutputStream out;

   public static void main (String[] args) {
      if (args.length==1) {
         try {
            out = new FileOutputStream (args[0]);
         final BufferedReader reader =
            new BufferedReader (new InputStreamReader (System.in));
         System.out.print ("Passphrase: ");
         System.out.flush();
         final String line = reader.readLine(); // get next line
         System.out.print (analyzedDownload(line));
         } catch (IOException ex) {
            ex.printStackTrace (System.err);
         }
      } else {
         System.out.format ("This command requires the file name to which to write the jar file as a command line argument.%n");
      }
   }

   public static Response download (String p) throws IOException, ClassNotFoundException {
      return download (Parameters.currentParameters(), p);
   }

   public static Response download (final Parameters p, String pp) throws IOException, ClassNotFoundException {

      p.setParameter ("password", pp);
      final String server = p.getServer();
      final int port      = p.getPort();
      final Socket s      = new Socket ();
      s.setSoTimeout (4500);  // Enable SO_TIMEOUT; a read() will block for no more than 4.5 sec
      s.connect (new InetSocketAddress (server,port), 4500);  // 4.5 sec timeout
      final ObjectOutputStream oos = new ObjectOutputStream (s.getOutputStream());
      final ObjectInputStream  ois = new ObjectInputStream  (s.getInputStream());
      p.writeObject ("download", oos);
      Response r = (submit.shared.Response) ois.readObject ();
      if (! r.success) {
         System.out.format ("The server is not going to send us the jar file.%n");
         return r;
      }

      oos.writeObject (null); // Acknowledge

      final Encryption e = new Encryption (pp);
      final InputStream in = new CipherInputStream (ois, e.getCipher (false));

      System.out.format ("Download receiving with pass phrase: %s%n", pp);
      System.out.flush();

      for (;;) {
         final int len = in.read(buffer);
         if (len < 0) break;
         System.out.print (".");
         System.out.flush();
         out.write (buffer, 0, len);
      }
      System.out.println ("!");
      System.out.flush();

      oos.close();
      ois.close();
      
      r = new Response ();
      r.add_line ("Finished");
      return r;
   }

   public static ResponseObject analyzedDownload (String p) {
      try {
	 return new ResponseObject (download(p));
      } catch (Exception ex) {
	 return new ResponseObject (ex);
      }
   }

}
