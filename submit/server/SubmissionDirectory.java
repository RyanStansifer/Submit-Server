package submit.server;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Date;
import java.text.SimpleDateFormat;

/*
   Execute.exec UNIX dependent ln command
*/

@Deprecated
public class SubmissionDirectory {

   public static final SimpleDateFormat dt = new SimpleDateFormat ("yyyy-MM-dd.HH:mm:ss");

   public synchronized File createDirLink (File dir) throws IOException {
      if (! dir.isDirectory()) throw new IOException (dir + " not a directory");
      final File subdir = new File (dir, dt.format (date));
      if (subdir.exists()) throw new IOException ("file '"+subdir+"' already exists");
      if (! subdir.mkdir()) throw new IOException ("trouble creating " + subdir);
      final File link = new File (dir, "latest");
      final boolean res = link.delete();  // discard result
      final int ret = Execute.exec ("/bin/ln -s "+ subdir.getName() + " " + link.getCanonicalPath());
      if (ret != 0) throw new IOException ("error in creating link " + link);


      final File new_link = new File (dir, "/./latest/./");
      if (!new_link.exists()) {
	 throw new RuntimeException ("link doesn't exist: " + link);
      }
      if (!new_link.isDirectory()) {
	 throw new RuntimeException ("link not a directory: " + link);
      }
      if (!new_link.canRead()) {
	 throw new RuntimeException ("link not readable:    " + link);
      }
      
      /*
	Why does the symbolic link not get resolved?
      */

      final String n = new_link.getCanonicalFile().getName();
      if (n.startsWith ("20")) {
      } else {
	 System.out.println ("Bad symbolic link?");
	 System.out.println ("relative:  "+ new_link);
	 System.out.println ("absolute:  "+ new_link.getAbsoluteFile());
	 System.out.println ("canonical: "+ new_link.getCanonicalFile());
      }
      return subdir;
   }

   /*
     "date" is the official time stamp of the submission
   */

   public final Date date = new Date ();
   public final File directory;

   public SubmissionDirectory (File d) throws IOException {
      directory = createDirLink (d);
   }

   public synchronized void copy (InputStream is, String name) throws IOException {
      final File f = new File (directory, name);
      final OutputStream fos = new FileOutputStream (f);
      for (;;) {
	 int ch = is.read();
	 if (ch<0) break;
	 fos.write (ch);
      }
      fos.close();
   }

   public static void main (String[] args) throws Exception {
      final SubmissionDirectory sd = new SubmissionDirectory (new File ("subdir"));
      for (int i=0; i<args.length; i++) {
	 sd.copy (new FileInputStream (new File (args[i])), args[i]);
      }
   }
}
