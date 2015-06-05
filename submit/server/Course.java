package submit.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

final public class Course extends Context {

   private final static File COURSE_CONTEXT_ROOT = SubmitServer.db.root;

   /*
     COURSE_CONTEXT_ROOT / *course* / *project* / *identity* / *time stamp* ".jar"
   */

   private final File course_directory;    // ... / *course* /
   private final File project_directory;   // ... / *course* / *project* /
   private final File identity_directory;  // ... / *course* / *project* / *identity* /
   private final java.nio.file.Path identity_directory_path;


   Course (final Parameters args) throws ContextException {
      super (COURSE_CONTEXT_ROOT, args.getControl(), args.getTimeStamp());
      assert COURSE_CONTEXT_ROOT.isDirectory();
      course_directory = setCourseDir (args);
      project_directory = setProjectDir (args);
      identity_directory =  new File (project_directory, identityDirectoryName(control));

      identity_directory_path = java.nio.file.FileSystems.getDefault().getPath(root.getName(),
         args.getCourseName(), args.getProjectName(), control);

      if (! accepting()) {
         final String project = args.getProjectName();
         final String course = args.getCourseName();
         final String p = args.getProperty("project");
         final String c = args.getProperty("course");
         /*var*/ String s;
         if (p.equals (project)) {
            s = "Project '"+p+"' for ";
         } else {
            s = "Project '"+p+"' ['"+project+"'] for ";
         }
         if (c.equals (course)) {
            s += "course '"+c+"' not accepting submissions at this time.";
         } else {
            s += "course '"+c+"' ['"+course+"'] not accepting submissions at this time.";
         }
         throw new ContextException (s);
      }
   }

   /*
     The "course_name" should be a directory, but it may be an alias.
   */

   private File setCourseDir (final Parameters args) throws ContextException {
      final String course = args.getCourseName();
      final File f = new File (root, course); // NB. root must be init first
      if (f.isDirectory()) {
         return f;
      } else {
         final String c = args.getProperty("course");
         final String s;
         if (c.equals (course)) {
            s = "Course '"+c+"' not found.";
         } else {
            s = "Course '"+c+"' ['"+course+"'] not found.";
         }
         throw new ContextException (s);
      }
   }

   private File setProjectDir (Parameters args) throws ContextException {
      final String project = args.getProjectName();
      final File f = new File (course_directory, project); // NB. 'course_directory' must be init first
      if (f.isDirectory()) {
         return f;   // Canonical path?
      } else {
         final String course = args.getCourseName();
         final String p = args.getProperty("project");
         final String s;
         if (p.equals (project)) {
            s = "Project '"+p+"' not found for course '"+course+"'.";
         } else {
            s = "Project '"+p+"' ['"+project+"'] not found for course '"+course+"'.";
         }
         throw new ContextException (s);
      }
   }

   /*
     For now we identify the submitter by their control code
   */
   private String identityDirectoryName (final String control) {
      return control;
   }

   private boolean accepting () {
      final File on = new File (project_directory, "ON");
      return on.exists();
   }

   public String password () {
      final File pf = new File (course_directory, "PASSWORD");
      if (pf.exists()) {
         return null;
      } else {
         return null;
      }
   }

   private int previousSubmissions () {
      if (identity_directory.isDirectory()) {
	 final File [] files = identity_directory.listFiles(new FilenameFilter() {
	       @Override
		  public boolean accept(File dir, String name) {
		  return name.endsWith(".jar");
	       }
	    });
	 return files.length;
      } else {
	 return 0;
      }
   }

   public final File submissionJarFile () throws ContextException {
      if (! identity_directory.isDirectory()) {
         // If we create it, we may have to delete it later or leave it empty, if the submission fails
         if (! identity_directory.mkdir()) throw new ContextException ("Couldn't make submission directory for "+control);
      }
      return new File (identity_directory, formatUTC (time_stamp) + ".jar");
   }

   public static File getProjectDirectory (String c, String proj) {
      return new File (new File (COURSE_CONTEXT_ROOT, c), proj);
   }

   public File getProjectDirectory () {
      return project_directory;
   }


   public String toString () {
      final String course_name = course_directory.getName();
      final String project_name = project_directory.getName();
      return String.format ("course=%s, project=%s, control=%s, time=%s, dir=%s, time stamp=%s",
         course_name, project_name, control, Parameters.format(time_stamp), identity_directory, Context.formatUTC(time_stamp));
   }
}
