package submit.shared;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import java.util.Date;
import java.util.jar.JarEntry;

import java.util.jar.JarOutputStream;

import javax.crypto.CipherOutputStream;


class WriteJar {

   public static void main (String[] args) throws IOException {
      if (args.length < 2) {
	 System.out.println ("Usage: java Main <jar-file> <file1>...");
      } else {
         main (args[0], slice (args));
      }
   } 

   public static void main (String jarName, String[] fileNames) throws IOException {

      final List<File> files = new ArrayList <File> ();
      for (String file: fileNames) {
         final File f = new File(file);
         if (! acceptable(f)) continue;
         files.add (f);
      }

      final Encryption e = new Encryption ("Pass Phrase");

      main (new GZIPOutputStream (new CipherOutputStream (new FileOutputStream (jarName), e.getCipher (true))), files);
   }


   public static void main (final OutputStream jar, final List<File> files) throws IOException {
      final JarOutputStream jos = new JarOutputStream (jar);
      jos.setMethod (JarEntry.STORED);

      final Manifest man = new Manifest ();

      // Now save all the list files in the JAR.
      for (File f: files) {
         final FileInfo fi = new FileInfo (f, true);
         man.add (fi);
         final JarEntry je =new JarEntry(fi.getSubPathNetwork());
         je.setSize (f.length());
         je.setCompressedSize (f.length());
         je.setCrc (computeCRC (new FileInputStream (f)));
         jos.putNextEntry (je);
         copyStream (new FileInputStream (f), jos);
         jos.closeEntry();
      }

      // Now add manifest
      final JarEntry je =new JarEntry(Manifest.FILENAME);
      final byte[] bytes = man.getAsByteArray ();
      je.setSize (bytes.length);
      final CRC32 crc = new CRC32();
      crc.update (bytes);
      je.setCrc (crc.getValue());

      jos.putNextEntry (je);
      jos.write (bytes);
      jos.closeEntry();

      jos.close();
   }

   private static final byte[] buffer = new byte[4096];
   private static void copyStream (InputStream in, OutputStream out) throws IOException {
      try {
         for (;;) {
            final int len = in.read(buffer);
            if (len < 0) break;
            out.write (buffer, 0, len);
         }
      } finally {
         in.close();
      }
   }

   private static long computeCRC (InputStream in) throws IOException {
      final CRC32 crc = new CRC32();
      for (;;) {
         final int len = in.read(buffer);
         if (len < 0) break;
         crc.update (buffer, 0, len);
      }
      return crc.getValue();
   }
         

   // Converts f's pathname to a form acceptable to ZIP files.
   // In particular, file separators are converted to forward slashes.
   private static String entryName (final File f) throws IOException {
      return f.getPath().replace(File.separatorChar, '/');
   }

   private static boolean acceptable (File f) {
      if (f.isAbsolute()) return false;
      if (f.getPath().contains ("..")) return false;
      return f.exists();
   }

   /*
   private static ArrayList<File> parentFiles (final File f) throws IOException {
      File x = f;
      ArrayList<File> p = new ArrayList<File>();
      for (;;) {
         x = x.getParentFile();
         if (x==null) return p;
         p.add(x);
      }
   }
   */

   private static String [] slice (String [] a, int offset, int length) {
      final String [] r = new String [length];
      System.arraycopy (a, offset, r, 0, length);
      return r;
   }

   private static String [] slice (String [] a, int offset) {
      return slice (a, offset, a.length-offset);
   }

   private static String [] slice (String [] a) {
      return slice (a, 1);
   }
      

}
