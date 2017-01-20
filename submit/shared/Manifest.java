package submit.shared;

// Why is the Manifest shared?  Because it is downloaded by the course instructor
// using the client!

import submit.shared.Registration;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.jar.*;

public class Manifest extends HashSet <FileInfo> implements Comparable <Manifest>, Serializable {

   /*  Original UID of the object when it was first deployed.
   static final long serialVersionUID = 6558365029634269452L;

   Manfiests from older submissions (jar files) can no loger be read.
   */

   /* UID as 27 July 2012 */
   static final long serialVersionUID = -2120437137588296146L;

   private final long time_stamp;            // Unix epoch
   private final Registration registration;
   private final Properties properties;     // Submission information

   // New method; breaks serialVersionUID!!
   private Map<Object,Object> getProperties () {
      return Collections.unmodifiableMap (properties);
   }

   // Ignore time stamp
   public int compareTo (Manifest m) {
      return registration.compareTo (m.registration);
   }

   public Registration getRegistration ()   { return registration; }   // New method; breaks serialVersionUID!!
   public String getNameAsDirectory () { return registration.getNameAsDirectory(); }
   public Date   getTimeStamp ()      { return new Date(time_stamp); }
   public String getSubmittersName () { return registration.toString (false); }

   public Manifest () {
      this (new Registration ("Mouse", "Mickey", "mm@disney.com"));
   }

   public Manifest (final Registration r) {
      this (r, System.currentTimeMillis());
   }

   public Manifest (final Registration r, final long ts) {
      this (r, ts, null);
   }

   public Manifest (final Registration r, final long ts, final Properties args) {
      registration = r;
      time_stamp = ts;
      properties = args;
   }

   public Manifest (final Registration r, final Properties args) {
      this (r, Long.valueOf (args.getProperty("time_stamp")), args);
   }

   public static final String FILENAME = "00manifest.ser";

   @Override
   public boolean add (FileInfo f) {
      return super.add (f);
   }

   public static Manifest loadFromJar (final File f) throws ClassNotFoundException, IOException {
      final JarInputStream jis = new JarInputStream (new FileInputStream (f));
      Manifest man = null;
      try {
         while (true) {
            final JarEntry je = jis.getNextJarEntry();
            if (je==null) break;
            // We are expecting one entry to be a serialized Manifest file
            if (je.getName().equals(FILENAME)) {
               man = load (jis);
            }
         }
      } catch (InvalidClassException ex) {
         System.err.println (ex);
         man = null;
      } catch (ClassNotFoundException ex) {
         // An archived submission may have an old manifest version
         System.err.println (ex);
         man = null;
      } finally {
         jis.close();
      }
      return (man);
   }

   public static Manifest load (final File f) throws ClassNotFoundException, IOException {
      return load (new FileInputStream (f));
   }

   public static Manifest load (final InputStream fis) throws ClassNotFoundException, InvalidClassException, IOException {
      final ObjectInputStream ois = new ObjectInputStream (fis);
      final Manifest man = (Manifest) ois.readObject ();
      return (man);
   }

   public void save (final File f) throws IOException {
      final FileOutputStream   fos = new FileOutputStream (f);
      final ObjectOutputStream oos = new ObjectOutputStream (fos);
      oos.writeObject (this);
      oos.flush();
      oos.close();
   }

   public byte[] getAsByteArray () throws IOException {
      final ByteArrayOutputStream out = new ByteArrayOutputStream ();
      final ObjectOutputStream oos = new ObjectOutputStream (out);
      oos.writeObject (this);
      return out.toByteArray();
   }
      
   private InputStream getAsInputStream () throws IOException {
      return new ByteArrayInputStream (getAsByteArray());
   }

   private static final String df = "EEE, dd MMM yyyy, 'at' hh:mm a z";
   private static final DateFormat format = new SimpleDateFormat (df);
   public String formatLocalTimeStamp () {
      // Create an object with the local time zone
      final GregorianCalendar local = new GregorianCalendar();
      local.setTimeInMillis (time_stamp);  // UTC from Date object
      return format.format (local.getTime());
   }

   /*
     time stamp contains "at" so use "on" instead of "at"

     'Last, First on Wed, 23 Apr 2006 at 10:13:54 PM EST'
   */
   // Called by jar2jar to give the intructor meta info about the submission
   public void print (PrintStream out) {

      if (registration==null) {
         out.println ("WARNING:  No registration info with this manifest.");
      } else {
         out.format ("%s on %s%n", registration.toString(0), formatLocalTimeStamp());
      }

      // This manifest is a collection of FileInfo's
      for (FileInfo fi: this) {
         out.println (fi);
      }

      /*
        Let some other routine get submission parameters if required.
       */
      /*
      if (properties==null) {
         // It is as yet uncommon to store the properties in the manifest
         //out.println ("WARNING:  No submission properties with this manifest.");
      } else {
         try {
            properties.store (out, "submission parameters");
         } catch (IOException ex) {
            out.println ("ERROR: IO exception while printing submission parameters");
         }
      }
      */
   }

   public static void main (String args []) throws ClassNotFoundException, IOException  {
      if (args.length==0) {
         final Manifest man = new Manifest ();
         final OutputStream out = new FileOutputStream (FILENAME);
         out.write (man.getAsByteArray());
      } else if (System.getProperty ("dir")!=null) {
         /*
           The command:
              java -Ddir submit.entry.Manifest dir0 dir1 dir2
           will read Manifest files from each directory given.
          */
         for (int i=0; i<args.length; i++) {
            final Manifest man = load (new File(args[i],FILENAME));
            man.print (System.out);
         }
      } else if (System.getProperty ("jar")!=null) {
         for (int i=0; i<args.length; i++) {
            final Manifest man = loadFromJar (new File(args[i]));
            man.print (System.out);
         }
      }
   }
}
