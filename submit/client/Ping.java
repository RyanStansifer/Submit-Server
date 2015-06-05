package submit.client;

import submit.client.Parameters;
import submit.client.ResponseObject;
import submit.shared.Response;

import java.io.*;
import java.net.*;

final public class Ping {

   public static void main (String[] args)   {
      System.out.print (analyzedPing());
   }

   public static Response ping () throws IOException, ClassNotFoundException {
      return ping (Parameters.currentParameters());
   }

   public static Response ping (final Parameters p) throws IOException, ClassNotFoundException {
      final String server = p.getServer();
      final int port      = p.getPort();
      /*
        Socket() raises IOException
      */
      final Socket s      = new Socket ();
      /*
      final SSLSocket s = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket ();
      */

      s.setSoTimeout (3500);  // Enable SO_TIMEOUT; a read() will block for no more than 3.5 sec

      /*
        connect() raises

        IOException - if an error occurs during the connection
          UnknownHostException - if IP address of a host could not be determined
          ConnectException - if an error while connecting to remote address and port
        SocketTimeoutException - if timeout expires before connecting
        IllegalBlockingModeException - if this socket has an associated channel, and the channel is in non-blocking mode
        IllegalArgumentException - if endpoint is null or is a SocketAddress subclass not supported by this sock
       */

      s.connect (new InetSocketAddress (server,port), 3500);  // 3.5 sec timeout

      // System.out.println (s.getRemoteSocketAddress());

      final ObjectOutputStream oos = new ObjectOutputStream (s.getOutputStream());
      final ObjectInputStream  ois = new ObjectInputStream  (s.getInputStream());

      p.writeObject ("ping", oos);  // may raise EOFException

      final Response r = (submit.shared.Response) ois.readObject ();
      oos.close();
      ois.close();
      return r;
   }

   public static ResponseObject analyzedPing () {
      return analyzedPing (Parameters.currentParameters());
   }

   public static ResponseObject analyzedPing (final Parameters p) {
      try {
	 return new ResponseObject (ping(p));
      } catch (Exception ex) {
         if (Defaults.VERBOSE>1) {
            ex.printStackTrace();
         }
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
      if (missing (p, "server")) message += "Missing server name\n";
      if (missing (p, "port"  )) message += "Missing port number\n";
      if (message.length()<1) {
	 return null;
      } else {
	 return message;
      }
   }


}
