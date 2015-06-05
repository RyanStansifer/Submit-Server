package submit.server;

import submit.shared.Response;

import submit.server.Parameters;

import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class CreateProject {

   private final static File COURSE_CONTEXT_ROOT = SubmitServer.db.root;
   private final static String PASSWORD_FILENAME = "PASSWORD";

   public static File getCourseDirectory (String course) {
      return new File (COURSE_CONTEXT_ROOT, course);
   }

   public static File getRealCourseDirectory (String course) {
      try {
         return new File (COURSE_CONTEXT_ROOT, course).getCanonicalFile();
      } catch (IOException ex) {
         return null;
      }
   }

   public static String getSemester (final File real_course_directory) {
      return real_course_directory.getParentFile().getName();
   }

   public static File getProjectDirectory (File course, String project) {
      return new File (course, project);
   }
      
   public static File getProjectDirectory (String c, String proj) {
      return new File (new File (COURSE_CONTEXT_ROOT, c), proj);
   }

   private static synchronized boolean turnSubmissionsOn (final File project_directory) {
      boolean response = false;
      try {
         final File on = new File (project_directory, "ON");
         response = on.createNewFile();
      } catch (IOException ex) {
         ex.printStackTrace (System.err);
      } 
      return response;
   }

   private static synchronized boolean turnSubmissionsOff (final File project_directory) {
      boolean response = false;
      try {
         final File on = new File (project_directory, "ON");
         if (on.exists()) {
            response = on.delete();
         } else {
            // nothing to do
            response = true;
         }
      } catch (Exception ex) {
         ex.printStackTrace (System.err);
      } 
      return response;
   }

   private static synchronized boolean accepting (final File project_directory) {
      final File on = new File (project_directory, "ON");
      return on.exists();
   }

   private static byte[] password (final File course_directory) throws IOException {
      final FileInputStream in = new FileInputStream (new File (course_directory, PASSWORD_FILENAME));
      final ByteArrayOutputStream out = new ByteArrayOutputStream ();
      for (;;) {
         final byte[] buffer = new byte[4096];
         final int len = in.read(buffer);
         if (len < 0) break;
         out.write (buffer, 0, len);
      }
      in.close();
      out.close();
      return out.toByteArray();
   }

   /* Can raise FileNotFoundException */
   public static String getPassword (final File course_directory) throws IOException {
      return new String (password (course_directory)); // using platform's default charset
   }

   private static boolean isPassword (final File course_directory, final String password) {
      boolean matches = false;
      try {
         final String p = getPassword (course_directory); 
         matches = p.equals (password);
         if (SubmitServer.VERBOSE>3 && !matches) {
            System.out.format ("%s!=%s%n", p, password);
         }
      } catch (FileNotFoundException ex) {
         /* If no password file, then no password will access the course. */
         matches = false;
      } catch (Exception ex) {
         System.out.format ("Unexpected exception while checking the password%n");
         ex.printStackTrace (System.out);
      }
      return matches;
   }

   static boolean isAuthorized (Parameters args, final File course_directory) {
      return isPassword (course_directory, args.getProperty("password"));
   }

   static String infoCourse (final String course, final Parameters args) {
      final String c = args.getProperty("course");
      if (c.equals (course)) {
         return String.format ("'%s'", c);
      } else {
         return String.format ("'%s' ['%s']", c, course);
      }
   }

   static String infoCourseProject (final String course, final String project, final Parameters args) {
      final String i = infoCourse (course, args);
      String p = args.getProperty("project");
      try {
         p = CreateProject.getProjectDirectory (course, project).getCanonicalFile().getName();
      } catch (IOException ex) {
         // No likely, or important
      }
      if (p.equals (project)) {
         return String.format ("project '%s' for course %s", p, i);
      } else {
         return String.format ("project '%s' ['%s'] for course %s", p, project, i);
      }
   }


   public static void create (final Parameters args, final Response resp) {
      final String course = args.getCourseName();   // cleaned-up a bit
      if (course==null) resp.failBecause ("Missing course information.");

      final String project = args.getProjectName(); // cleaned-up a bit
      if (project==null) resp.failBecause ("Missing project information.");

      if (course==null || project==null) return;

      final File course_directory = getCourseDirectory (course);
      if (!course_directory.isDirectory()) {
         if (SubmitServer.VERBOSE>4) {
            System.out.format ("File not found or not directory: '%s'%n", course_directory);
         }
         resp.failBecause (String.format ("Course %s not found.", infoCourse (course, args)));
         return;
      }

      // If the course directory exists, then we can check for a password
      final boolean authorized = isAuthorized (args, course_directory);
      if (!authorized) {
         resp.failBecause ("Not authorized.");
         return;
      }

      final File project_directory = getProjectDirectory (course_directory, project);
      if (project_directory.isDirectory()) {
         resp.success = false;
         resp.add_line (String.format ("Won't create a new project; a directory for project '%s' already exists.", project));
         return;
      }

      final boolean b1 = project_directory.mkdir();
      if (b1) {
         boolean b2 = turnSubmissionsOn (project_directory);
         if (b2) {
            resp.success = true;
            resp.add_line (String.format ("Project directory for '%s' created; submissions allowed.", project));
         } else {
            resp.success = false;
            resp.add_line (String.format ("Could not create 'ON' file for project '%s' already exists.", project));
         }
      } else {
         resp.success = false;
         resp.add_line (String.format ("Could not create directory for project '%s' already exists.", project));
      }
   }


   public static void turnon (final Parameters args, final Response resp) {
      final String course = args.getCourseName();   // cleaned-up a bit
      if (course==null) resp.failBecause ("Missing course information.");

      final String project = args.getProjectName(); // cleaned-up a bit
      if (project==null) resp.failBecause ("Missing project information.");

      if (course==null || project==null) return;

      final File course_directory = getCourseDirectory (course);
      if (!course_directory.isDirectory()) {
         resp.failBecause (String.format ("Course %s not found.", infoCourse (course, args)));
         return;
      }

      final boolean authorized = isAuthorized (args, course_directory);
      if (!authorized) {
         resp.failBecause ("Not authorized.");
         return;
      }

      final File project_directory = getProjectDirectory (course_directory, project);
      if (!project_directory.isDirectory()) {
         resp.failBecause (String.format ("The %s not found.", infoCourseProject (course, project, args)));
         return;
      }

      if (accepting (project_directory)) {
         resp.success = true;
         resp.add_line ("Submissions already turned on.");
      } else {
         boolean b = turnSubmissionsOn (project_directory);
         if (b) {
            resp.success = true;
            resp.add_line ("Submissions turned on.");
         } else {
            resp.failBecause ("Error. Unexpected trouble turning submissions turned on.");
         }
      }
   }


   public static void turnoff (final Parameters args, final Response resp) {
      final String course = args.getCourseName();   // cleaned-up a bit
      if (course==null) resp.failBecause ("Missing course information.");

      final String project = args.getProjectName(); // cleaned-up a bit
      if (project==null) resp.failBecause ("Missing project information.");

      if (course==null || project==null) return;

      final File course_directory = getCourseDirectory (course);
      if (!course_directory.isDirectory()) {
         resp.failBecause (String.format ("Course %s not found.", infoCourse (course, args)));
         return;
      }

      final boolean authorized = isAuthorized (args, course_directory);
      if (!authorized) {
         resp.failBecause ("Not authorized.");
         return;
      }

      final File project_directory = getProjectDirectory (course_directory, project);
      if (!project_directory.isDirectory()) {
         resp.failBecause (String.format ("The %s not found.", infoCourseProject (course, project, args)));
         return;
      }

      if (accepting (project_directory)) {
         boolean b = turnSubmissionsOff (project_directory);
         if (b) {
            resp.success = true;
            resp.add_line ("Submissions turned off.");
         } else {
            resp.failBecause ("Error. Unexpected trouble turning submissions off.");
         }
      } else {
         resp.success = true;
         resp.add_line ("Submissions already turned off.");
      }
   }

   public static void status (final Parameters args, final Response resp) {
      final String course = args.getCourseName();   // cleaned-up a bit
      if (course==null) {
         resp.failBecause ("Missing course information.");
         return;
      }

      final File course_directory = getCourseDirectory (course);
      if (!course_directory.isDirectory()) {
         resp.failBecause (String.format ("Course %s not found.", infoCourse (course, args)));
         return;
      }

      final boolean authorized = isAuthorized (args, course_directory);
      if (!authorized) {
         resp.failBecause ("Not authorized.");
         return;
      }

      resp.add_line ("Projects (*=ON):");
      final List<String> list = Projects.onProjectsByCourse (course_directory);
      for (String x: list) {
         resp.add_line (x);
      }

   }


   public static void report (final Parameters args, final Response resp) {
      final String course = args.getCourseName();   // cleaned-up a bit
      if (course==null) resp.failBecause ("Missing course information.");

      final String project = args.getProjectName(); // cleaned-up a bit
      if (project==null) resp.failBecause ("Missing project information.");

      if (course==null || project==null) return;

      final File course_directory = getCourseDirectory (course);
      if (!course_directory.isDirectory()) {
         resp.failBecause (String.format ("Course %s not found.", infoCourse (course, args)));
         return;
      }

      final boolean authorized = isAuthorized (args, course_directory);
      if (!authorized) {
         resp.failBecause ("Not authorized.");
         return;
      }

      final File project_directory = getProjectDirectory (course_directory, project);
      if (!project_directory.isDirectory()) {
         resp.failBecause (String.format ("The %s not found.", infoCourseProject (course, project, args)));
         return;
      }

      if (SubmitServer.VERBOSE>4) {
         System.out.format ("Wrote HTML report for %s.%n", infoCourseProject (course, project, args));
         System.out.format ("File is %s.%n", project_directory);
         try {
            System.out.format ("File is %s.%n", project_directory.getCanonicalPath());
         } catch (IOException ex) {
            ex.printStackTrace (System.out);
         }
      }

      try {
         // Write a report for latests submission, all submissions
         Report.main (project_directory);   // real class name??
         AllSubmissionsReport.main (project_directory);   // real class name??
         resp.success = true;
         resp.add_line (String.format ("Wrote HTML report for %s.", infoCourseProject (course, project, args)));
      } catch (final Exception ex) {
         ex.printStackTrace (System.err);
         System.err.println ("Couldn't write html report.");
         resp.failBecause ("Server had trouble creating html report");
      }

   }

   public static void main (String[] args) {
      final Response r = new Response();
      final Parameters p = new Parameters ("localhost");
      p.add (System.getProperties());
      System.out.println (p);
      final String command=p.getProperty ("command", "create");
      if (command.equals ("create")) {
         create (p, r);
      } else if (command.equals ("turnon")) {
         turnon (p, r);
      } else if (command.equals ("turnoff")) {
         turnoff (p, r);
      } else if (command.equals ("report")) {
         report (p, r);
      }
      System.out.println (r);
   }

}
