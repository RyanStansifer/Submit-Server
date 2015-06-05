package submit.server;

import submit.shared.FileInfo;

import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.io.*;

final class Latest {

   // A directory (java.io.File) for each submitter of the project
   private final Set<File> directories;
   public int size () { return directories.size(); }

   private Latest (final Set<File> d) { directories = d; }
   private Latest (final File project) { this (lsd(project)); }
   private Latest (final String class_name, String project_name) { 
      this (Course.getProjectDirectory (class_name, project_name));
   }

   /*
     java -Droot=../../submissions/current -cp ~ryan/public_html/java/new_submit/server.jar submit.server.Latest cseXXXX test
    */
   public static void main (final String[] args) throws IOException {
      main (args[0], args[1]);
   }

   public static void main (final String class_name, final String project_name) throws IOException {
      new Latest (class_name, project_name) . main ();
   }

   interface Format {
      public void begin1();
      public void end1();
      public void entry1 (String student, String date);
      public void begin2();
      public void end2();
      public void listing (File f);
   }

   static private final class DefaultListing implements Format {
      public void begin1(){};
      public void end1(){};
      public void entry1 (final String student, final String date) {
	 System.out.println ("Student="+student+";  Date/time of submission="+date);
      }
      public void begin2(){};
      public void end2(){};
      public void listing (File f) {
	 System.out.println ("  file=" + f.getName() + " size=" + f.length() + " mod=" + FileInfo.format.format (new Date(f.lastModified())));
      }
   }

   private File link (File student) throws IOException {
      return new File (student, "/./latest/").getCanonicalFile();
   }

   public void main () throws IOException {
      main (new DefaultListing ());
   }

   public void main (Format f) throws IOException {
      f.begin1();

      for (File student: directories) {
	 assert (student!=null);
	 assert (student.exists());
	 assert (student.isDirectory());
	 assert (student.canRead());

	 final File sub = link (student);
	 assert (sub!=null);
	 assert (sub.exists());
	 assert (sub.isDirectory());
	 assert (sub.canRead());

         for (File entry: ls(sub)) {
	    f.begin2();
	    f.listing (entry);
	    f.end2();
	 }
      }
      f.end1(); 
   }

   private static class DF implements FileFilter {
      public boolean accept (File path) {
	 return path.isDirectory();
      }
   }

   private static DF df = new DF ();

   private static class JarFiles implements FileFilter {
      public boolean accept (File path) {
         return path.getName().endsWith (".jar");
      }
   }

   private static JarFiles jf = new JarFiles ();

   /*
     Return the set of all individuals (directories) who have submitted anything
   */
   public static Set<File> lsd (File d) {
      assert (d!=null);
      assert (d.exists());
      assert (d.canRead());
      if (d.isDirectory()) {
	 return new TreeSet<File> (Arrays.asList (d.listFiles (df))); // list of directories
      } else {
         if (SubmitServer.VERBOSE>1) {
            System.out.println ("Latest.lsd() expecting '" + d +"' to be a directory");
         }
	 return new TreeSet<File> ();
      }
   }

   public static File latestSubmission (File d) {
      try {
         return new TreeSet<File> (Arrays.asList (d.listFiles (new JarFiles()))) . last () ;
      } catch (NullPointerException ex) {
         // listFiles() returns null meaning there are no files in the directory
         return null;
      } catch (NoSuchElementException ex) {
         return null;
      }
   }

   public static Set<File> ls (File d) {
      assert (d!=null);
      assert (d.exists());
      assert (d.canRead());
      if (d.isDirectory()) {
	 return new TreeSet<File> (Arrays.asList (d.listFiles ()));
      } else {
         if (SubmitServer.VERBOSE>1) {
            System.out.println ("Latest.ls() expecting '" + d +"' to be a directory");
         }
	 return new TreeSet<File> ();
      }
   }
}
