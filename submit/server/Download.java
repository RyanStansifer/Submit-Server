package submit.server;

import submit.shared.Manifest;
import submit.shared.Registration;
import submit.shared.Encryption;
import submit.shared.Response;

import static submit.shared.FileInfo.makePathNetwork;
import static submit.shared.FileInfo.format;


import submit.server.Parameters;

import java.io.*;

import java.util.Date;
import java.util.Set;
import java.util.jar.*;

import javax.crypto.*;

class Download {

   private static String semesterx, coursex, projectx;

   public static void download (final Parameters args, Response resp,
      final ObjectInputStream ois, final ObjectOutputStream oos) throws ClassNotFoundException, IOException {

      final String course = args.getCourseName();   // cleaned-up a bit
      if (course==null) resp.failBecause ("Missing course information.");

      final String project = args.getProjectName(); // cleaned-up a bit
      if (project==null) resp.failBecause ("Missing project information.");

      if (course==null || project==null) return;

      final File course_directory = CreateProject.getRealCourseDirectory (course);
      if (!course_directory.isDirectory()) {
         resp.failBecause (String.format ("Course %s not found.", CreateProject.infoCourse (course, args)));
         return;
      }
      coursex=course;
      projectx=project;
      semesterx= CreateProject.getSemester (course_directory);
      

      final boolean authorized = CreateProject.isAuthorized (args, course_directory);
      if (!authorized) {
         resp.failBecause ("Not authorized.");
         return;
      }

      final File project_directory = CreateProject.getProjectDirectory (course_directory, project);

      resp.success = true;
      resp.add_line ("Internal protocol message: ready to send jar");
      oos.writeObject (resp);   // 2.  OK to transfer?
      oos.flush();
      ois.readObject(); // Protocol; discard input

      final Encryption e = new Encryption (CreateProject.getPassword (course_directory));
      
      if (SubmitServer.VERBOSE>4) System.out.format ("Download: encrypted using password='%s'%n", CreateProject.getPassword (course_directory));
      final OutputStream out = new CipherOutputStream (oos, e.getCipher (true));

      jar2jar (project_directory, out);
      oos.flush();
   }

   private static final byte[] buffer = new byte[8_000];
   private static void copyStream (InputStream in, OutputStream out) throws IOException {
      for (;;) {
         final int len = in.read(buffer);
         if (len < 0) break;
         out.write (buffer, 0, len);
      }
   }

   private static void copyStream (final byte[] a, OutputStream out) throws IOException {
      copyStream (new ByteArrayInputStream (a), out);
   }

   private static void copyStream (final String x, OutputStream out) throws IOException {
      copyStream (x.getBytes(), out);
   }


   public static void jar2jar (final File dir, OutputStream out) throws ClassNotFoundException, IOException {
      final String top = String.format ("%s_%s_%s", semesterx, coursex, projectx);
      if (SubmitServer.VERBOSE>4) System.out.format ("jar2jar: dir=%s; top=%s%n", dir, top);

      final JarOutputStream jos = new JarOutputStream (out);
      jos.setMethod (JarEntry.DEFLATED);

      /*
        Include in the jar from some general meta information about the download.
      */

      final JarEntry ije =new JarEntry(makePathNetwork (top,"INFO"));
      jos.putNextEntry (ije);
      final String info = String.format ("copied=%s, semester=%s, course=%s, project=%s%n", format.format(new Date()), semesterx, coursex, projectx);
      copyStream (info, jos);
      jos.closeEntry();

      final JarEntry ijej =new JarEntry(makePathNetwork (top,"info.json"));
      jos.putNextEntry (ijej);
      final String infoj = String.format ("{%n'copied'='%s', 'semester'='%s', 'course'='%s', 'project'='%s'%n}%n", format.format(new Date()), semesterx, coursex, projectx);
      copyStream (infoj, jos);
      jos.closeEntry();

      final Set<File> students = Latest.lsd (dir);

      for (File student: students) {
         // The latest submission for this student
         final File jar = Latest.latestSubmission (student);
         // In the rare event student directory exists, but no submission
         if (jar==null) continue;

         final Manifest man = Manifest.loadFromJar (jar);  // The submission manifest (not the Jar manifest)

         final String submitter_directory;
         if (man==null) {
            // This is not good.
            if (SubmitServer.VERBOSE>4) System.out.format ("The manifest for '%s' is missing.%n", student.toString());
            submitter_directory = makePathNetwork (top, student.getName());
         } else {
            submitter_directory = makePathNetwork (top, man.getNameAsDirectory());
         }

         if (man!=null) {
            final JarEntry xje = new JarEntry(makePathNetwork (submitter_directory, "INFO"));
            jos.putNextEntry (xje);
            final ByteArrayOutputStream x = new ByteArrayOutputStream ();
            man.print (new PrintStream (x));
            x.close();
            copyStream (new ByteArrayInputStream (x.toByteArray()), jos);
            jos.closeEntry();

            final JarEntry xjej =new JarEntry(makePathNetwork (submitter_directory, "info.json"));
            jos.putNextEntry (xjej);

            final String name    = man.getRegistration().fullName();
            final String control = man.getRegistration().getControl();
            final String email   = man.getRegistration().getEMail();
            final String time    = man.formatLocalTimeStamp();  // time of submission
            final String xinfoj   = String.format ("{%n'name'='%s', 'control'='%s', 'email'='%s', 'time'='%s'%n}%n", name, control, email, time);
            copyStream (xinfoj, jos);
            jos.closeEntry();
         }

         //final JarInputStream jis = new JarInputStream (new FileInputStream(jar));
         final String submissions_directory = makePathNetwork (submitter_directory, "submission");

         try (
              final JarInputStream jis = new JarInputStream (new FileInputStream(jar))
         ) {

               while (true) {
                  final JarEntry je = jis.getNextJarEntry();
                  if (je==null) break;
                  if (je.getName().equals (Manifest.FILENAME)) continue;  // cf INFO; plain text
                  final JarEntry sje =new JarEntry (makePathNetwork (submissions_directory,je.getName()));
                  if (SubmitServer.VERBOSE>5) System.out.format ("Copying from '%s' to '%s'.%n", je, sje);
                  jos.putNextEntry (sje);
                  copyStream (jis, jos);
                  jos.closeEntry();
               }
         }
         //jis.close();
      }
      jos.close();
   }
}
