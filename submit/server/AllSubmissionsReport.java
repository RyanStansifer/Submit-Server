package submit.server;

/* A change in the version of the Manifest class invalidates
   all archived submission!
*/
import submit.shared.Manifest;
import submit.shared.FileInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InvalidClassException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;

import java.util.*;

import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;

import java.text.ChoiceFormat;


public final class AllSubmissionsReport {

   public static String reportName (String class_name, String project_name) {
      return Report.reportDir(class_name)+File.separator+project_name+"-all"+Report.extension;
   }

   private static File htmlFile (String class_name, String project_name) {
      return new File (Report.html_home, reportName(class_name,project_name));
   }

   private final String class_name;
   private final String project;

   private final PrintWriter out;

   public AllSubmissionsReport (String c, String p) throws IOException {
      assert (c!=null);
      assert (p!=null);

      class_name = c;
      project = p;

      final File directory = Report.reportDirFile (class_name);

      if (SubmitServer.VERBOSE > 4) {
	 System.out.println ("html home '" +Report.html_home+"'");
	 System.out.println ("directory for project '"+p+"' and class '"+c+"':");
	 System.out.println (directory.getAbsolutePath());
      }

      /*
	If a class has aliases formed by linking directories in
	the submissions directory, then we may be making the wrong
	directory here.
      */
      if (! directory.exists()) {
	 final boolean b = directory.mkdir ();  // use mkdirs() to create any non-existant parents
	 if (! b) throw new RuntimeException ("directory " + directory.toString() + " can't be created");
      }

      if (SubmitServer.VERBOSE > 3) {
	 System.out.println (htmlFile(class_name,project).getAbsolutePath());
	 System.out.println (Course.getProjectDirectory (class_name, project));
      }

      out = new PrintWriter (new FileWriter (htmlFile (class_name, project)));

      class JarFiles implements FileFilter {
            public boolean accept (File path) {
               return path.getName().endsWith (".jar");
            }
         }

      // List of all jar files
      final Set<File> student_directories = Latest.lsd (Course.getProjectDirectory (class_name, project));

      directories = new TreeSet<File> (new FileComparator());
      for (File student: student_directories) {
         directories.addAll (Arrays.asList (student.listFiles (new JarFiles())));
      }
   }

   private final SortedSet<File> directories;

   
   private String title () {
      return "All Submissions for " + project;
   }

   private void prelude () throws IOException {
      out.println ("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
      out.println ("<html>\n<head>\n  <title>Florida Tech, "+class_name+":  "+title()+"</title>");
      out.println ("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">");
      out.println ("  <meta http-equiv=\"expires\" content=\"0\">");
      out.println ("  <meta http-equiv=\"Cache-Control\" content=\"no-cache\">");
      out.println ("  <meta http-equiv=\"Pragma\" content=\"no-cache\">");
      out.println ("</head>\n<body style=\"font-family: sans-serif\">");
      out.println ("<h1>"+title()+"</h1>");

      // out.println ("<!--  submissions directory:  "+directory.getCanonicalPath()+"  -->");

      out.println ("Here are all the submissions (not just the lastest ones) sucessfully received by the ");
      out.println ("<a href=\"/~ryan/submit.html\">submission server</a> ");
      out.println ("for project \""+project+"\"");
      out.println ("and class \""+class_name+"\".");

      out.println ("This report was generated on " + FileInfo.format.format (new Date())+".");
      out.println (submissions (directories.size()));
      
      out.println ("If your browser has cached this URL, you may have to \"refresh\" or \"reload\"");
      out.println ("this document to view the most recent submissions.");
      out.println ("<p>");
   }

   private String submissions (int n) {
      final MessageFormat mf = new MessageFormat ("At that time, {0} had been received.");
      final double[] limits = {0,1,2};
      final String [] m = {"no submissions", "one submission", "{1} total submissions"};
      final ChoiceFormat cf = new ChoiceFormat (limits, m);
      final Format [] formats = {cf, NumberFormat.getInstance()};
      mf.setFormats (formats);
      final Object[] arguments = {n, n};
      return mf.format (arguments);
   }

   void listings () throws IOException {
      out.println ("<h2>List of Submissions</h2>");
      
      if (SubmitServer.VERBOSE > 13) {
         System.out.println ("Project directory: " + Course.getProjectDirectory (class_name, project));
         System.out.println (directories.size() + " all submissions (Jar files):");
         System.out.println (directories);
      }

      out.println ("<p align=center>");
      out.println ("<table cellpadding=2><tr><th>name<th>date/time of submission</tr>");

      for (File submission: directories) {
         try {
            final Manifest man = Manifest.loadFromJar (submission);
            out.println ("<tr>");
            if (man==null) {
               out.println ("  <td colspan=2><em>No manifest for</em> "+ submission.getName() +"</td>");
            } else {
               out.println ("  <td>" + man.getSubmittersName() + "</td><td>&nbsp;<tt>" + man.formatLocalTimeStamp() + "</tt></td>");
            }
            out.println ("</tr>");
         } catch (Exception ex) {
            System.out.println ("Making HTML report on submissions for project '"+project+"' and course '"+class_name);
            System.out.format ("Submissions manifest from jar file '%s' for %s %n", submission, submission.getName());
            ex.printStackTrace (System.out);  // (Not to standard error)
            continue;  // continue report with next entry
         }
      }

      out.println ("</table>");
      out.println ("</p>");
   }


   void postlude () throws IOException {
      out.println ("</body>\n</html>");
   }

   void write () throws IOException {
      prelude();
      listings();
      postlude();
      out.close();
   }

   public static void main (File project_directory) throws IOException {
      // Watch for links in courses!
      final File real = project_directory.getCanonicalFile();
      main (real.getParentFile().getName(), real.getName());
   }

   public static void main (String c, String p) throws IOException {
      assert (c!=null);
      assert (p!=null);

      final AllSubmissionsReport r = new AllSubmissionsReport (c, p);
      r.write ();
      // insure pages are accessible

      final File url = htmlFile (c, p);
      if (url.exists()) {
      } else {
	 System.err.println ("Report html file doesn't exists!  " + url.getCanonicalPath());
	 throw new RuntimeException ("failed to create html file: " + url.getCanonicalPath());
      }
   }

   // File sorts on the path; this sorts on the name
   public static class FileComparator implements Comparator <File> {
      public int compare (File f1, File f2) {
         final String name1 = f1.getName();
         final String name2 = f2.getName();
         final int x = name1.compareTo (name2);
         if (x==0) {
            // Files of same name better not compare the same
            // or they may get lost in the set.
            return f1.compareTo (f2);
         } else {
            return x;
         }
      }
   }

   /*
     Example usage:
     cd ~submit/server/fall03

     /software/solaris/compilers/java/j2sdk1.4.2/j2se/bin/java
        -Droot=../../submissions -cp ~ryan/submit/server.jar
        submit.server.AllSubmissionsReport cseXXXX test
   */

   public static void main (String [] args) throws IOException {
      main (args[0], args[1]);
   }

}
