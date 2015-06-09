package submit.server;

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

public class Report {

   private final static String WWW_HOST = "cs.fit.edu";
   private final static String DEFAULT_REPORT_URL =
      String.format ("http://%s/%s", WWW_HOST, "~"+System.getProperty ("user.name"));
   public final static String reportURL = System.getProperty ("report.URL", DEFAULT_REPORT_URL);

   private final static String DEFAULT_REPORT_DIR =
      String.format ("%s%spublic_html", System.getProperty ("user.home"), File.separator);
   public final static String reportDIR = System.getProperty ("report.DIR", DEFAULT_REPORT_DIR);

   static {
      final File d = new File(reportDIR);
      if (!d.exists())  throw new RuntimeException ("directory " + d.toString() + " doesn't exist");
      if (!d.isDirectory()) throw new RuntimeException ("directory " + d.toString() + " not a directory");
      if (!d.canWrite()) throw new RuntimeException ("directory " + d.toString() + " not writeable");
   }

   public final static File   html_home = new File (reportDIR);
   public final static String extension = ".html";

   public static String reportDir (String class_name) {
      return "submissions"+File.separator+"current"+File.separator+class_name;
   }

   private static String reportName (String class_name, String project_name) {
      return reportDir(class_name)+File.separator+project_name+extension;
   }

   /*
     $HOME/public_html/submissions/current/
     The current term's place for putting public, feedback, html reports.
     It should already exist and be publically searchable/executable.
   */

   public static File reportDirFile (String class_name) {
      return new File (html_home, reportDir (class_name));
   }

   public static File  reportNameFile (String class_name, String project_name) {
      return new File (html_home, reportName(class_name,project_name));
   }

   public static String reportURL (String class_name, String project_name) {
      return reportURL+'/'+reportName(class_name,project_name);
   }

   public static String reportURL (File project_directory) {
      return reportURL+'/'+reportName(project_directory.getParentFile().getName(),project_directory.getName());
   }

   private final String class_name;
   private final String project;

   private final PrintWriter out;

   public Report (String c, String p) throws IOException {
      assert (c!=null);      assert (p!=null);
      class_name = c;        project = p;

      final File directory = reportDirFile (class_name);
      if (SubmitServer.VERBOSE > 4) {
	 System.out.println ("HTML home '" +html_home+"'");
	 System.out.println ("HTML directory for project '"+p+"' and class '"+c+"':");
         System.out.print ("  ");
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

      // Make class directory readable and executable/searchable (if possible)
      // final boolean b = directory.setReadable(true) && directory.setExecuable(true);

      if (SubmitServer.VERBOSE > 4) {
	 System.out.println ("HTML directory for project: " + reportNameFile(class_name,project).getAbsolutePath());
	 System.out.println (Course.getProjectDirectory (class_name, project));
      }

      final File project_file = reportNameFile (class_name, project);

      out = new PrintWriter (new FileWriter (project_file));

      // List of all directories
      directories = Latest.lsd (Course.getProjectDirectory (class_name, project));
   }

   private final Set<File> directories;

   private String title () {
      return String.format ("Submissions for '%s'", project);
   }

   private void prelude () throws IOException {
      out.println ("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
      out.println ("<html>\n<head>\n  <title>Florida Tech, "+class_name+":  "+title()+"</title>");
      out.println ("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">");
      out.println ("  <meta http-equiv=\"expires\" content=\"0\">");
      out.println ("  <meta http-equiv=\"Cache-Control\" content=\"no-cache\">");
      out.println ("  <meta http-equiv=\"Pragma\" content=\"no-cache\">");
      out.println ("  <meta name=\"robots\" content=\"noindex,nofollow\">");
      out.println ("  <style type=\"text/css\">");
      out.println ("    table.xhs { border-spacing: 15px 0px; }  /* horizontal and vertical spacing */");
      out.println ("    tr.blue   { background-color: #006699; color: white} /* table header */");
      out.println ("    tr.even   { background-color: white; }");
      out.println ("    tr.odd    { background-color: #eff7ff; } /* light blue for alternation */");
      out.println ("  </style>");
      out.println ("</head>\n<body style=\"font-family: sans-serif\">");
      out.println ("<h1>"+title()+"</h1>");

      // out.println ("<!--  submissions directory:  "+directory.getCanonicalPath()+"  -->");

      out.println ("<p>");
      out.println ("Here are the submissions sucessfully received by the ");
      out.println ("<a href=\"/~ryan/submit.html\">submission server</a> ");
      out.println ("for project \""+project+"\"");
      out.println ("and class \""+class_name+"\".");

      out.println ("This report was generated on " + FileInfo.format.format (new Date())+".");
      out.println (submissions (directories.size()));

      out.println ("If your browser has cached this URL, you may have to \"refresh\" or \"reload\"");
      out.println ("this document to view the most recent submissions.");
      out.println ("This report is usually generated only when a new submission has been recevied.");
      out.println ("</p>");
   }

   private String submissions (final int n) {
      final MessageFormat mf = new MessageFormat ("At that time, {0} had been received.");
      final double[] limits = {0,1,2,3};
      final String [] m = {"no submissions", "submissions from one person", "submissions from two persons", "submissions from {1} persons"};
      final ChoiceFormat cf = new ChoiceFormat (limits, m);
      final Format [] formats = {cf, NumberFormat.getInstance()};
      mf.setFormats (formats);
      final Object[] arguments = {n, n};
      return mf.format (arguments);
   }

   void listings () throws IOException {
      out.println ("<h2>Summary of Submissions</h2>");

      if (SubmitServer.VERBOSE > 13) {
         System.out.println ("Project directory: " + Course.getProjectDirectory (class_name, project));
         System.out.println (directories.size() + " directories to list:");
         System.out.println (directories);
      }

      final TreeSet<Manifest> submissions = new TreeSet<Manifest> ();

      for (File student: directories) {

	 if (student==null || !student.exists() || !student.isDirectory() || !student.canRead()) {
	    System.out.println ("Report.listings() has problem with student directory: " + student);
	    continue;
	 }

         class JarFiles implements FileFilter {
            public boolean accept (File path) {
               return path.getName().endsWith (".jar");
            }
         }

         try {
            // What if no files (because submission deleted due to network error)??!
            // listFiles() returns null meaning there are no files
            // What does asList(null) do?

            // The lastest submission for this student
            final File sub = new TreeSet<File> (Arrays.asList (student.listFiles (new JarFiles()))) . last () ;


            final Manifest man = Manifest.loadFromJar (sub);
            if (man==null) {
               System.out.printf ("No manifest for %s%n", sub);
            } else {
               submissions.add (man);
            }
         } catch (java.util.NoSuchElementException ex) {
            // java.util.TreeSet.last raised an exception because there were no entries
            // This can happen if the submission fails to run to completion.
            if (SubmitServer.VERBOSE > 0 ) {
               System.out.printf ("Making HTML report on submissions for project '%s' and course '%s'.%n", project, class_name);
               System.out.printf ("Examining entry for student '%s'.%n", student);
               System.out.println (ex);
               System.out.printf ("We have a directory for the student but no submissions (jar file).%n");
               System.out.printf ("Continuing to write the rest of the report...%n");
            }
            continue;  // continue report with next entry
         } catch (FileNotFoundException ex) {
            System.out.printf ("Making HTML report on submissions for project '%s' and course '%s'.%n", project, class_name);
            System.out.printf ("Examining entry for student '%s'.%n", student);
	    System.out.println (ex);
	    System.out.println ("Expecting to find the file '"+Manifest.FILENAME+"'.  Continuing ...");
            continue;  // continue report with next entry
         } catch (InvalidClassException ex) {
            System.out.println ("Making HTML report on submissions for project '"+project+"' and course '"+class_name);
            System.out.printf ("Examining entry for student '%s'.%n", student);
	    System.out.println (ex);
	    System.out.println ("A manifest file written with inconsistent class version has been found.  Continuing ...");
            continue;  // continue report with next entry
         } catch (Exception ex) {
            System.out.println ("Making HTML report on submissions for project '"+project+"' and course '"+class_name);
            System.out.printf ("Examining entry for student '%s'.%n", student);
            ex.printStackTrace (System.out); // We are logging on std out not std err
            System.out.printf ("Continuing to write the rest of the report...%n");
            continue;  // continue report with next entry
         }
      }

      out.println ("<p align=center>");
      out.println ("<table cellpadding=2><tr><th>name<th>date/time of latest submission</tr>");
      for (Manifest man: submissions) {
         out.println ("<tr>");
         out.format ("  <td>%s</td><td>&nbsp;<tt>%s</tt></td>%n",
                     man.getSubmittersName(),
                     man.formatLocalTimeStamp()
                     );
         out.println ("</tr>");
      }
      out.println ("</table>");
      out.println ("</p>");

      out.println ("<h2>Files Submitted Per Student</h2>");
      for (Manifest man: submissions) {
         out.format ("  <h3>%s</h3>%n<p>The last submission was %s.%n",
                     man.getSubmittersName(),
                     man.formatLocalTimeStamp()
                     );
	 final int n = man.size();
	 if (n==0) {
	    out.println ("No files were");
	 } else if (n==1) {
	    out.println ("One file was");
	 } else if (n==2) {
	    out.println ("Two files were");
	 } else if (n>=3) {
	    out.println (n + " files were");
	 }
	 out.println (" submitted at that time.</p>");
	 if (n > 0) {
	    out.println ("<p align=left>");
	    out.println ("<table cellpadding=2><tr><th width=100px>file name<th width=80px>directory<th width=70px>size<th>last modified<th>encoding</tr>");
            for (FileInfo entry: man) {
               out.println ("  <tr>"+entry.toHTML()+"</tr>");
            }
	    out.println ("</table>");
	    out.println ("</p>");

         }
      }
   }

   void postlude () throws IOException {
      out.println ("</body>\n</html>");
   }

   /*
     Create an HTML report of latestest submissions.  This task is
     similar to the task performed by the class AllSubmissionsReport.
   */
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

   public static void main (final String c, final String p) throws IOException {
      assert (c!=null);
      assert (p!=null);

      final Report r = new Report (c, p);
      r.write ();  // create the whole report

      // insure pages are accessible
      // With Java 1.6, one can use setReadable(true)!
      final boolean success = reportDirFile(c).setReadable(true);
      if (success) {
      } else {
	  throw new RuntimeException ("Unable to make html file readable: " + reportDirFile(c));
      }
      assert reportNameFile(c,p).exists();
   }

   /*
     Example usage:
     cd ~submit/server/fall03

     /software/solaris/compilers/java/j2sdk1.4.2/j2se/bin/java
        -Droot=../../submissions/current -cp ~ryan/public_html/java/new_submit/server.jar
        submit.server.Report cseXXXX test
   */

   public static void main (String [] args) throws IOException {
      main (args[0], args[1]);
   }

}
