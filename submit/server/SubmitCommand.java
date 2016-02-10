package submit.server;

import submit.shared.Manifest;
import submit.shared.Encryption;
import submit.shared.Registration;
import submit.shared.Response;
import submit.shared.FileInfo;

import submit.server.Parameters;

import java.io.*;
import java.net.SocketTimeoutException;

import java.util.jar.*;

class SubmitCommand {

   public static void submit (final Parameters args, final Response resp,
      final ObjectInputStream ois, final ObjectOutputStream oos) throws ClassNotFoundException, IOException {

      resp.success = false;

      if (! args.containsKey("control")) {
         resp.add_line ("Error:  transfer rejected; no control code provided");
         return;
      }

      /*
        Authenticate user
      */
      final Registration r = SubmitServer.db.getRegistration (args.getControl());
      if (r==null) {
         if (SubmitServer.VERBOSE>5) {
            System.out.format ("No registration for control code '%s'%n", args.getControl());
         }
         resp.add_line ("Error:  control code not registered with the submit server; check for typos");
         return;
      }
      if (SubmitServer.VERBOSE>2) {
         System.out.format ("Submission by %s%n", r);
      }

      //  By definition the registration is now activated.
      if (!r.isActivated()) {
         System.out.println ("Tried to submit with unactivated registration.");
         resp.add_line ("Internal Error:  submission by an unactivated registration");
         return;
      }

      /*
        Establish disposition of submission.
      */
      Context context;
      try {
         context = Context.createContext (args);
      } catch (ContextException ex) {
         context = null;
         resp.add_line (ex.getMessage());
      }
      if (context==null) {
         return;
      }
      if (SubmitServer.VERBOSE>2) {
         System.out.println ("Submit context:  " + context);
      }


      /*
        At this point we are willing to accept the transfer of files.
        We add a message, so that a response is never null.
      */

      /*
        We start a new response object.  It is important not to reuse
        the old one because writeObject caches them and sends the old!
      */
      {
         final Response intermediateResponse = new Response ();
         intermediateResponse.success = true;
         intermediateResponse.add_line ("Internal protocol message: go ahead and send files");
         oos.writeObject (intermediateResponse);   // 2.  OK to transfer
      }

      final String course_name  = args.getCourseName ();
      final String project_name = args.getProjectName();

      /*
        SUBMIT_ROOT / *class* / *project* / *control number* / *time stamp* /
      */
      File submissionJarFile;
      try {
         submissionJarFile = context.submissionJarFile ();
      } catch (ContextException ex) {
         submissionJarFile = null;
         ex.printStackTrace (System.out);
         System.out.println ();
         resp.add_line ("Internal server error:  "+ ex.getMessage());
      }
      if (submissionJarFile==null) {
         return;
      }

      boolean delete = true; //  Delete if not a complete success

      JarOutputStream jos=null;
      try {
         jos = new JarOutputStream (new FileOutputStream (submissionJarFile));
         jos.setMethod (JarEntry.DEFLATED);

         int to_write=0, written=0, zeros=0;  // files written, files of size zero

         // Create a manifest for the submission with the same time stamp
         // as the protocol connection

         // args is an instance of type Parameters

         // For quizes (no files) the info may be in the submit parameters

         final Manifest man = new Manifest (r, args);

         while (true) {
            final Object o = ois.readObject();   // do we need to time out??
            if (o==null) break;                  // No more files
            final FileInfo fi;
            if (o instanceof FileInfo) {
               fi = (FileInfo) o;
            } else {
               System.err.printf ("Getting submission; expecting a 'FileInfo', but got class %s%n", o.getClass());
               resp.failBecause ("Error:  Internal protocol error.");
               throw new IOException ("Expecting object of class 'FileInfo'");
            }

            if (SubmitServer.VERBOSE>3) {
               System.out.println (fi);
            }
            boolean write = true;  // Assume this file is to be written into the jar file
            to_write++;

            /* A check to avoid an unreasonably large number of files. */
            if (written>FileInfo.MAX_FILES) {
               if (SubmitServer.VERBOSE >= 0 ) {
                  System.out.println ("Too many files!");
               }
               resp.add_line ("Too many files "+ fi.name);
               write=false;  // Do not write this file into the jar file
            }

            /* Has name been sent twice? */
            if (man.contains (fi)) {
               if (SubmitServer.VERBOSE >= 0 ) {
                  System.out.printf ("Filename '%s' appears twice in manifest!", fi.name);
               }
               resp.add_line (String.format ("Filename '%s' appears twice in manifest!", fi.name));
               write=false;  // Do not write this file into the jar file
            }

            if (write) {
               final JarEntry je =new JarEntry(fi.getSubPathNetwork());
               long real_size = 0;  // The size of current file meansured by the bytes written

               try { 
                  jos.putNextEntry (je);
   
                  while (true) {
                     // Occasionally get EOFException here
                     final byte [] ba = (byte []) ois.readObject();
                     if (ba==null) break;
                     real_size += ba.length;

                     assert real_size>= 0;
                     // Prevent large transfers and forged FileInfo
                     if (real_size > FileInfo.MAX_FILE_SIZE) {
                        System.out.println ("File too big!");
                        throw new IOException (String.format (
                           "File '%s' exceeds preset limit '%d' for the submit server",
                           fi.name, FileInfo.MAX_FILE_SIZE
                        ));
                     }
                     jos.write (ba);
                  }

               } finally {
                  // EOFException or IOException may have happened

                  jos.closeEntry();
                  written++; // keep track of the total number of files written.
                  if (real_size==0) zeros++;
                  man.add (fi);  // maintain list of files written
               }

               if (real_size != fi.size) {
                  System.out.printf ("File '%s' size mismatch!  Described size %d versus bytes written %d.%n", fi.name, fi.size, real_size);
                  resp.add_line ("Problem reading and writing files over the network. Resubmit.");
                  throw new RuntimeException ();
               }



            } else {
               // Some problem, consume file without writing it
               while (true) {
                  final byte [] ba = (byte []) ois.readObject();
                  if (ba==null) break;
               }
            }
         }

         final JarEntry je =new JarEntry(Manifest.FILENAME);
         final byte[] bytes = man.getAsByteArray ();
         jos.putNextEntry (je);
         jos.write (bytes);
         jos.closeEntry();
         jos.close (); // in finally clause?


         // Report to the client what happened
         if (to_write==0) {
            resp.add_line ("Zero files submitted.");
         } else if (to_write==1) {
            resp.add_line ("One file submitted successfully.");
         } else if (written==to_write) {
            resp.add_line ("All "+written+" files submitted successfully.");
         } else {
            resp.add_line (written+" of "+to_write+" files submitted.");
         }
         resp.add_line ("All previous submissions (if any) for project '"+project_name+"' will be ignored.");
         resp.add_line ("If not all files were submitted, then submit again with all the files.");
         if (zeros>0) {
            resp.add_line ("WARNING:  Did you mean to submit files of size zero?");            
         }
         resp.success = true;
         delete = false;

         // Find out where the report is going.
         final File real = CreateProject.getProjectDirectory (course_name, project_name).getCanonicalFile();
         final String url = Report.reportURL (real);
         try {
            Report.main (real);   // real class name??
            AllSubmissionsReport.main (real);   // real class name??
            resp.add_line ("A report on submissions is available on the WWW");
            resp.add_line ("  " + url);
         } catch (Exception ex) {
            //reportException (ex);
            ex.printStackTrace (System.out);
            System.out.println ("Couldn't write html report.  Continuing ...");
            System.out.println ();
            resp.add_line ("Server had trouble creating " + url);
         }

      } catch (SocketTimeoutException ex) {
         //reportException (ex);
         ex.printStackTrace (System.out);
         resp.add_line ("Timeout.  Slow network?");
      } catch (FileNotFoundException ex) {
         //reportException (ex);
         ex.printStackTrace (System.out);
         resp.add_line ("Error.  Server had trouble writing a file.");
      } catch (ClassNotFoundException ex) {
         //reportException (ex);
         ex.printStackTrace (System.out);
         resp.add_line ("Error.  Server had trouble writing a file.");
      } catch (EOFException ex) {
         //reportException (ex);
         ex.printStackTrace (System.out);
         resp.add_line ("Error.  Possible network error in transmitting the file.");
      } catch (IOException ex) {
         //reportException (ex);
         ex.printStackTrace (System.out);
         resp.add_line ("Error.  Server had trouble writing a file.");
      } finally {
         Connection.closeIgnoringException (jos);
         /*
           If not successful, delete submission.
         */
         if (SubmitServer.VERBOSE>0) {
               System.out.format ("The submission file='%s'%n", submissionJarFile);
         }
         if (delete) {
            if (SubmitServer.VERBOSE>0) {
               System.out.format ("It was necessary to delete the submission. File='%s'%n", submissionJarFile);
            }
            // Maybe we need to delete the directory, if this was the only submission.
            // or, maybe it just isn't worth it.
            try {
               java.nio.file.Files.delete (submissionJarFile.toPath());    // DELETE SUBMISSION!!!!!
            } catch (IOException ex) {
               ex.printStackTrace (System.out);
            } catch (SecurityException ex) {
               ex.printStackTrace (System.out);
            } catch (RuntimeException ex) {
               ex.printStackTrace (System.out);
            } finally {
            }
         }
      }
   }
}
