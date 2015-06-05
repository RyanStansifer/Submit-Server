package submit.server;

import submit.shared.Manifest;
import submit.shared.Encryption;
import submit.shared.Response;

import static submit.shared.FileInfo.makePathNetwork;

import submit.server.Parameters;

import java.io.*;

import java.util.Date;
import java.util.Set;
import java.util.jar.*;

import javax.crypto.*;

@Deprecated
class ExtractJar {

   public static void extract (final Parameters args, final Response resp) throws ClassNotFoundException, IOException {
      final String course = args.getCourseName();   // cleaned-up a bit
      if (course==null) resp.failBecause ("Missing course information.");

      final String project = args.getProjectName(); // cleaned-up a bit
      if (project==null) resp.failBecause ("Missing project information.");

      if (course==null || project==null) return;

      final File course_directory = CreateProject.getCourseDirectory (course);
      if (!course_directory.isDirectory()) {
         resp.failBecause (String.format ("Course %s not found.", CreateProject.infoCourse (course, args)));
         return;
      }

      final boolean authorized = CreateProject.isAuthorized (args, course_directory);
      if (!authorized) {
         resp.failBecause ("Not authorized.");
         return;
      }

      final File project_directory = CreateProject.getProjectDirectory (course_directory, project);
      final String pp = "Pass Phrase";
      final String filename = "XXX.jar.crypt";
      final Encryption e = new Encryption (pp);
      final OutputStream out = new CipherOutputStream (new FileOutputStream (filename), e.getCipher (true));
      jar2jar (project_directory, out);
      resp.add_line ("Finished extraction");
   }

   private static final byte[] buffer = new byte[4096];
   private static void copyStream (InputStream in, OutputStream out) throws IOException {
      for (;;) {
         final int len = in.read(buffer);
         if (len < 0) break;
         out.write (buffer, 0, len);
      }
   }

   private static void copyStream (byte[] a, OutputStream out) throws IOException {
      copyStream (new ByteArrayInputStream (a), out);
   }

   private static void copyStream (String x, OutputStream out) throws IOException {
      copyStream (x.getBytes(), out);
   }

   public static void jar2jar (File dir, OutputStream out) throws ClassNotFoundException, IOException {
      final String top = "fall05_cseXXXX_test";
      final String info = String.format ("copied=%s, semester=fall05, course=%s, project=%s%n", new Date(), "cseXXXX", "test");

      final JarOutputStream jos = new JarOutputStream (out);
      jos.setMethod (JarEntry.DEFLATED);

      final JarEntry ije =new JarEntry(makePathNetwork (top,"INFO"));
      jos.putNextEntry (ije);
      copyStream (info, jos);
      jos.closeEntry();
      System.out.println (ije);

      final Set<File> students = Latest.lsd (dir);
      System.out.println (students);

      for (File student: students) {
         // The latest submission for this student
         final File jar = Latest.latestSubmission (student);
         // In the rare event student directory exists, but no submission
         if (jar==null) continue;
         final Manifest man = Manifest.loadFromJar (jar);
         final String directory = makePathNetwork (top,man.getNameAsDirectory(),"submission");

         final ByteArrayOutputStream x = new ByteArrayOutputStream ();
         man.print (new PrintStream (x));
         x.close();
         
         final JarEntry xje = new JarEntry(directory+"/INFO");
         jos.putNextEntry (xje);
         copyStream (new ByteArrayInputStream (x.toByteArray()), jos);
         jos.closeEntry();
         System.out.println (xje);

         final JarInputStream jis = new JarInputStream (new FileInputStream(jar));

         while (true) {
	    final JarEntry je = jis.getNextJarEntry();
            if (je==null) break;
            final JarEntry sje =new JarEntry(directory+"/"+je.getName());
            if (SubmitServer.VERBOSE>5) System.out.format ("Copying from '%s' to '%s'.%n", je, sje);
            jos.putNextEntry (sje);
            copyStream (jis, jos);
            jos.closeEntry();
         }
         jis.close();
      }
      jos.close();
   }


   public static void main (String[] args) throws Exception {
      final Response r = new Response();
      final Parameters p = new Parameters ("localhost");
      p.add (System.getProperties());
      System.out.println (p);
      final String command=p.getProperty ("command", "extract");
      if (command.equals ("extract")) {
        extract (p, r);
      } else if (command.equals ("create")) {
         CreateProject.create (p, r);
      } else if (command.equals ("turnon")) {
         CreateProject.turnon (p, r);
      } else if (command.equals ("turnoff")) {
         CreateProject.turnoff (p, r);
      } else if (command.equals ("report")) {
         CreateProject.report (p, r);
      }
      System.out.println (r);
   }

}
