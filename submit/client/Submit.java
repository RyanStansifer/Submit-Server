package submit.client;

import submit.client.Defaults;
import submit.client.Parameters;
import submit.client.ResponseObject;
import submit.shared.Response;
import submit.shared.FileInfo;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.regex.*;

final public class Submit {

   private final static int TIME_OUT = 8000;  // 8 seconds

   private static Map<String,String> propertiesInFile (final Set<FileInfo> f) throws IOException {
      /*
        Additional information comes from the files themselves.  This is passed
        along in the map "m".
      */
      final Map<String,String> m = new HashMap<String,String> ();
      for (final FileInfo fi: f) {
         final Map<String,String> x = new Route (fi.getFileRoot());
         m.putAll (x);
      }
      return m;
   }

   public static void main (final String[] args) throws IOException, ClassNotFoundException  {
      if (!Defaults.NOFILES && args.length<1) {
	 System.err.println ("No files given to submit.");
	 return;
      }
      
      final Parameters p = Parameters.currentParameters();
      if (p.getCourse()==null || p.getProject()==null) {
	 System.err.println ("ERROR: no attempt was made to submit any files.");
	 System.err.println ("Both 'course' and 'project' information required to submit.");
         return;
      }

      final Set<FileInfo> hs = new HashSet<FileInfo> ();

      try {
         if (p.fileOnly()) {
            // just the file is sent and the parent director information is lost
            for (int i=0; i<args.length; i++) hs.add (new FileInfo (args[i]));
         } else {
            // the file plus the parent directory information is sent
            for (int i=0; i<args.length; i++) hs.add (new FileInfo (new File(args[i]), true));
         }
      } catch (final IllegalArgumentException ex) {
	 System.err.println ("ERROR: no attempt was made to submit any files.");
         System.err.println ("It is not allowed with submit files with certain characters in the file name.");
         System.err.println ("Nor is not allowed with submit files above a certain size.");
	 System.err.println (ex.getMessage());
         return;
      }

      System.out.print (analyzedSubmit(hs,p));
   }

   @Deprecated
   private static Response submit (final Set<FileInfo> hs, final Parameters p) throws IOException, ClassNotFoundException {
      return submit (hs, p, null);
   }

   public static Response submit (final Set<FileInfo> hs, final Parameters p, final Map<String,String> m) throws IOException, ClassNotFoundException {
      final String server = p.getServer();
      final int port      = p.getPort();
      /*
        Socket() raises IOException
      */
      final Socket s      = new Socket ();

      // Enable SO_TIMEOUT; a read() will not block forever
      s.setSoTimeout (TIME_OUT);

      /*
        connect() raises

        IOException - if an error occurs during the connection
        SocketTimeoutException - if timeout expires before connecting
        IllegalBlockingModeException - if this socket has an associated channel, and the channel is in non-blocking mode
        IllegalArgumentException - if endpoint is null or is a SocketAddress subclass not supported by this sock
       */

      s.connect (new InetSocketAddress (server,port), 6000);  // 6000 sec timeout
      final ObjectOutputStream oos = new ObjectOutputStream (s.getOutputStream());
      final ObjectInputStream  ois = new ObjectInputStream  (s.getInputStream());

      p.writeObject ("submit", m, oos);  // merge 'm' and 'p' and write to socket
      Response r = (submit.shared.Response) ois.readObject ();

      if (r==null) {
	 throw new NullPointerException ("first response null!?!");
      } else if (!r.success) {
         // The server is not willing to take the submission
      } else {
	 copy (hs, oos);
	 r = (submit.shared.Response) ois.readObject ();
      }
      oos.close();
      ois.close();
      s.close();

      if (r==null) {
	 throw new NullPointerException ("second response null!?!");
      }
      return r;
   }

   public static ResponseObject analyzedSubmit (final File[] fa, final Parameters p) throws IOException {
      final Set<FileInfo> f = new HashSet<FileInfo> ();
      // creating FileInfo could raise FileNotFoundException
      for (int i=0; i<fa.length; i++) f.add (new FileInfo (fa[i]));
      return analyzedSubmit (f, p);
   }


   public static ResponseObject analyzedSubmit (final Set<FileInfo> f, final Parameters p) throws IOException {
      /*
        Additional information comes from the files themselves.  This is passed
        along in the map "m".
      */
      final Map<String,String> m;
      if (p.getPeek()) {
         m = propertiesInFile(f);
         if (Defaults.VERBOSE>1) {
            System.out.println ("Properties from files:  " + m);
         }
      } else {
         if (Defaults.VERBOSE>1) {
            System.out.println ("Files not searched for properties");
         }
         m = new HashMap<String,String> ();
      }
      return analyzedSubmit (f, p, m);
   }

   @Deprecated
   private static ResponseObject analyzedSubmit (final Set<FileInfo> f, final Map<String,String> m) {
      return analyzedSubmit (f, Parameters.currentParameters(), m);
   }

   public static ResponseObject analyzedSubmit (final Set<FileInfo> f, final Parameters p, final Map<String,String> m) {
      try {
	 return new ResponseObject (submit(f,p,m));
      } catch (final Exception ex) {
         // We don't really have a good way of logging errors in the client
         if (Defaults.VERBOSE>1) {
            ex.printStackTrace (System.err);
         }
	 return new ResponseObject (ex);
      }
   }

   private static void copy (Set<FileInfo> set, ObjectOutputStream oos) throws IOException {
      for (FileInfo f: set) copy (f, oos);
      oos.writeObject (null);   // No more files
      oos.flush();
   }

   private final static byte [] buffer = new byte [2048];

   private static void copy (FileInfo fi, ObjectOutputStream oos) throws IOException {
      if (Defaults.VERBOSE>2) {
         System.err.print ("Preparing to write file:  ");
         System.err.println (fi);
      }
      oos.writeObject (fi);
      final FileInputStream fis = new FileInputStream(fi.getFileRoot());
      while (true) {
	 final int nbytes = fis.read (buffer);
	 if (nbytes == -1) break;
         //	 oos.writeObject (slice (buffer, 0, nbytes));
	 oos.writeObject (Arrays.copyOfRange (buffer, 0, nbytes));
      }
      fis.close();
      oos.writeObject (null);   // No more byte arrays for this file
      oos.flush();
   }

   @Deprecated
   private static byte [] slice (byte [] a, int offset, int length) {
      byte [] r = new byte [length];
      System.arraycopy (a, offset, r, 0, length);
      return r;
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
      if (missing (p, "control"   )) message += "Missing control code\n";
      if (message.length()<1) {
	 return null;
      } else {
	 return message;
      }
   }

}
