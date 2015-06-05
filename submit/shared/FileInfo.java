package submit.shared;

import java.io.*;

import java.util.Date;
import java.util.regex.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/*
**  WARNING:  Changing this file will cause a version error in the protocol between
**  old clients and the new server.
**/
/*
**  This class needs to add the character encoding of the file.
**  cf .../guide/intl/encoding.doc.html
**     encoding == null                : not a text file
**     encoding.equals("ISO-8859-1")   : a text file encoded in the ISO 8859-1, Latin 1 character set
**     encoding.equals("windows-1252") : a text file encoded in Windows Latin-1
**
**/

public class FileInfo implements Serializable {

   static final long serialVersionUID = -2380758188027054225L;

   /*
     NB. The client and the server staill can have DIFFERENT values
     for "MAX_FILE_SIZE" as they run in separate JVMs.
   */
   private static final int DEFAULT_MAX_FILE_SIZE = 21000000; 
   public static final int MAX_FILE_SIZE =
      Integer.getInteger ("submit.filesize", DEFAULT_MAX_FILE_SIZE);

   private static final int DEFAULT_MAX_FILES = 100; 
   public static final int MAX_FILES =
      Integer.getInteger ("submit.maxfiles", DEFAULT_MAX_FILES);

   /*
     A permitted file name consists of digits, letters, period, underscore, hyphen
     and length 1 to 45 characters.
   */
   private static final int MAX_NAME_SIZE = 45;
   private static final Pattern pattern = Pattern.compile("[0-9a-zA-Z._-]{1,"+MAX_NAME_SIZE+"}");

   public static boolean checkFileName (String file_name) {
      return pattern.matcher(file_name).matches();
   }

   /*
   private static final String df = "EEE, dd MMM yyyy, 'at' hh:mm a z";
   */
   public static final DateFormat format = new SimpleDateFormat ("EEE, dd MMM yyyy 'at' hh:mm a z");
   public static final char networkSeparatorChar = '/';

   private final static String platformEncoding = System.getProperty ("file.encoding");

   /*
     The client and server may have different values for this static variable!
   */
   public final static char separatorChar = File.separatorChar;

   private transient File root;  // OS dependent
   public File getFileRoot () { return root; }

   public static String makePathNetwork (String directory, String name) {
      return String.format ("%s%c%s", directory, networkSeparatorChar, name);
   }
   public static String makePathNetwork (String dir1, String dir2, String name) {
      return String.format ("%1$s%2$c%3$s%2$c%4$s", dir1, networkSeparatorChar, dir2, name);
   }

   public static String makePathOS (String directory, String name) {
      return String.format ("%s%c%s", directory, separatorChar, name);
   }

   public String getSubPathOS () {
      if (parent==null) {
         return name;
      } else {
         return makePathOS (convertFromNetwork (parent), name);
      }
   }

   public String getSubPathNetwork () {
      if (parent==null) {
         return name;
      } else {
         return makePathNetwork (parent, name);
      }
   }

   public  final String parent;  // possibly null; uses network separator char
   public  final String name;
   public  final long size;
   private final long modified;
   public  final String encoding;

   public Date getModified () { return new Date(modified); }


   /*
     This class does not have a no-argument constructor.  But nonetheless
     seems to serialize OK.  How?

     The requirement is to have access to the no-arg constructor
     of the first nonserializable superclass.
   */

   // just file, no parent directory
   public FileInfo (String n) throws IOException {
      this (new File (n));
   }

   // just file, no parent directory
   public FileInfo (File f) throws IOException {
      this (f, null, f.getName(), f.length(), f.lastModified(), platformEncoding);
   }

   public FileInfo (File f, boolean relative) throws IOException {
      this (f, f.getParent(), f.getName(), f.length(), f.lastModified(), platformEncoding);
      if (f.getParentFile().isAbsolute()) throw new BadFileException ("File name '"+f+"' must not be absolute");
   }

   // file and parent directory
   public FileInfo (File f, String p) throws IOException {
      this (f, p, f.getName(), f.length(), f.lastModified(), platformEncoding);
   }

   private FileInfo (final File root, final String p, final String n, final long s, final long m, final String e) throws IOException {
      this.root = root.getCanonicalFile();
      // assuming parent path is still using OS spearatorChar (same as root)
      assert root.getPath().endsWith (p+separatorChar+n);

      if (p==null) {
         // No parent; same as "." is parent
         parent = null;
      } else {
         // Network separator must not appear in path
         if (networkSeparatorChar != separatorChar) {
            if (p.indexOf (networkSeparatorChar) != -1) throw new BadFileException ("path separation confusion in directory name '"+p+"'");
         }
         parent = convertToNetwork (p);
      }
      assert n!=null;

      // If the file name has the networkSeparatorChar in it; it will be confused.
      if (n.indexOf (networkSeparatorChar)!=-1){
         throw new BadFileException ("File name '"+n+"' uses network path separator");
      }
      if (! checkFileName (n)) {
         throw new BadFileException ("File name '"+n+"' does not match '"+pattern.pattern()+"'");
      }

      // What about c:\\... ?
      if (p!=null && (p.charAt(0)==networkSeparatorChar)) {
         throw new BadFileException ("Parent directory '"+p+"' must not be absolute");
      }
      name = n;

      if (s>=MAX_FILE_SIZE) {
         throw new BadFileException ("File '"+n+"' too big");
      }
      size = s;
      modified = m;
      encoding = e;
   }

   private static String convertToNetwork (String n) {
      if (networkSeparatorChar != separatorChar) {
	 return n.replace (separatorChar, networkSeparatorChar);
      } else {
	 return n;
      }
   }

   private static String convertFromNetwork (String n) {
      if (networkSeparatorChar != separatorChar) {
	 return n.replace (networkSeparatorChar, separatorChar);
      } else {
	 return n;
      }
   }

   @Override
   public boolean equals (Object x) {
      if (this == x) return true;
      if (! (x instanceof FileInfo)) return false;
      final FileInfo y = (FileInfo) x;
      if (parent==null && y.parent==null) return name.equals (y.name);
      else if (parent!=null && y.parent!=null) return parent.equals (y.parent) && name.equals (y.name);
      else return false;
   }

   @Override
   public int hashCode () {
      int hash = 7;
      hash = 31 * hash + (name==null?0:name.hashCode());
      hash = 31 * hash + (parent==null?0:parent.hashCode());
      return hash;
   }

   private static String formatAttr (String n, String p, long s, String m, String e) {
      return String.format ("file=%s, parent=%s, size=%d, modified=%s, encoding=%s", n, p, s, m, e);
   }

   private static String formatHTML (String n, String p, long s, String m, String e) {
      return String.format ("<td><tt>%s</tt></td><td><tt>%s</tt></td><td align=right>%,d</td><td>&nbsp;<tt>%s</tt/</td><td></tt>%s</tt></td>", n, p, s, m, e);
   }

   public String toHTML () {
      return formatHTML (name, (parent==null?".":convertFromNetwork (parent)), size, format.format (modified), encoding==null?"[not text]":encoding);
   }

   public String toString () {
      return formatAttr (name, (parent==null?".":convertFromNetwork (parent)), size, format.format (modified), encoding==null?"[not text]":encoding);
   }

   public static void main (String [] args) throws Exception {
      for (int i=0; i<args.length; i++) {
	 File f = new File (args[i]);
	 FileInfo fi = new FileInfo (f);

	 FileOutputStream  fos = new FileOutputStream("fi.ser");
	 ObjectOutputStream oo = new ObjectOutputStream (fos);
	 oo.writeObject (fi);
	 oo.flush();
	 oo.close();

	 FileInputStream  fis = new FileInputStream("fi.ser");
	 ObjectInputStream oi = new ObjectInputStream (fis);
	 fi = (FileInfo) oi.readObject();
	 oi.close();
	 System.out.println (fi);
      }
   }
}
