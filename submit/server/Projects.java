package submit.server;

import java.io.File;
import java.io.PrintStream;
import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

/*
    Gives an indication of which projects are currently accepting submissions

    java Projects ~submit/submissions/current
 */

class Projects {

   public static Set<File> subdirectories (final File dir) {
      final String [] allf = dir.list();
      final TreeSet<File> dirs  = new TreeSet<File> ();
      if (allf!=null) {
	 for (int i=0; i<allf.length; i++) {
	    final File fd1 = new File (dir.getPath(), allf[i]);
	    if (fd1.isFile()) {
	    } else if (fd1.isDirectory()) {
	       dirs.add (fd1);
	    } else {
	    }
	 }
      }
      return dirs;
   }

   public static void printOnProjects (final File fd) {
      printOnProjects (fd, System.out);
   }

   public static void printOnProjects (final File fd, final PrintStream out) {
      for (Iterator i=subdirectories(fd).iterator(); i.hasNext(); ) {
	 final File dir = (File) i.next();
	 final boolean w = dir.canWrite ();
	 final String msg1 = (w?"   ":"?  ");
         for (File subdir: subdirectories(dir)) {
            final boolean on = Database.submissionsOn (subdir);
	    out.println (dir.getName()+msg1+ (on?"* ":"  ") + subdir.getName());
	 }
      }
   }

   public static Map<String,Set<String>> allProjects (final File fd) {
      final Map<String,Set<String>> projects = new HashMap<String,Set<String>> (20);
      for (File dir: subdirectories(fd)) {
	 final boolean w = dir.canWrite ();
         if (!w) continue;
         final String name = dir.getName();
         final Set<String> set = new TreeSet<String>();
         projects.put (name, set);
         for (File subdir: subdirectories(dir)) {
            final boolean on = Database.submissionsOn (subdir);
            if (!on) continue;
            set.add (subdir.getName());
	 }
      }
      return Collections.unmodifiableMap (projects);
   }

   public static List<String> onProjects (final File fd) {
      final List<String> l = new ArrayList<String> ();
      
      for (File dir: subdirectories(fd)) {
         for (File subdir: subdirectories(dir)) {
            final boolean on = Database.submissionsOn (subdir);
	    l.add (dir.getName()+",  "+ (on?"* ":"  ") + subdir.getName());
	 }
      }
      return l;
   }

   public static List<String> onProjectsByCourse (final File fd) {
      final List<String> l = new ArrayList<String> ();
      for (File subdir: subdirectories(fd)) {
         final boolean on = Database.submissionsOn (subdir);
         l.add ((on?"* ":"  ") + subdir.getName());
      }
      return l;
   }


   public static void main (String args[]) throws IOException {
      final String name = args.length>0?args[0]:".";
      final File fd = new File(name);
      printOnProjects (fd);
   }
}

