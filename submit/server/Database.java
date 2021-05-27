package submit.server;

import submit.shared.Registration;

import submit.email.Mail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Serializable;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;

public final class Database {

   private final static String DEFAULT_ROOT   = "submissions";
   public final File root = new File (System.getProperty ("root", DEFAULT_ROOT));

   private final static String DEFAULT_DB   = "parameters";
   public final File directory = new File (System.getProperty ("database", DEFAULT_DB));

   private final static String DEFAULT_MAIL   = "mailhost.fit.edu";
   public final String mailhost = System.getProperty ("mailhost", DEFAULT_MAIL);

   public    Date    date;  //  Used as an application-level version number
   public String date () { return Parameters.format.format (date); }

   private   HashMap<String,Registration> registered;
   public int size() {
      if (registered==null) return -1;
      else return this.registered.size();
   }
   
   private Database () { this(new Date()); }
   private Database (final Date d) { date=d; }
   private Database (final Date d, final HashMap<String,Registration> data) { date=d; registered=data; }

   public static Database init () throws IOException, RuntimeException {

      final Database db = new Database ();

      final File t = new File (db.directory, "info.tsp");
      final File r = new File (db.directory, "info.reg");

      if (t.exists() && r.exists()) {
	 final FileInputStream  fis = new FileInputStream(t);
	 final ObjectInputStream oo = new ObjectInputStream (fis);
	 try {
            // Keep the date from the tsp file
            db.date = (Date) oo.readObject();
	 } catch (ClassNotFoundException e) {
	    e.printStackTrace (System.err);
            db.date = new Date ();
	 }
         // Read in all the registrations from the text .reg file, even
         // the inactive registrations.  They could be removed by editing
         // by hand.
	 db.registered = Registration.create (r);

      } else {
         // Either the "info.tsp" or the "info.reg" file does not exist.
	 db.date = new Date ();           // Now
	 db.registered = new HashMap<String,Registration> ();
      }

      if (SubmitServer.VERBOSE>1) {
	 System.out.println ("*********  SubmitServer's Database ************");
         // Java can *not* currently change the working directory.  Changing user.dir doesn't do it.
	 System.out.println ("The working directory of the server is:");
         System.out.println (System.getProperty ("user.dir"));
	 System.out.println ("The mail host is: "+ db.mailhost);
	 System.out.println ("Database of registration information is kept in file with stem\n (-Ddatabase="+DEFAULT_DB+"):");
	 System.out.println (db.directory.getCanonicalPath());
	 System.out.println ("  DB exists?    " + db.directory.exists());
	 System.out.println ("  DB is dir?    " + db.directory.isDirectory());
	 System.out.println ("  DB writable?  " + db.directory.canWrite());
	 System.out.println ("Database version as of "+db.date);
	 System.out.printf  ("Current number of registrations in the database is %d%n", db.registered.size());
	 System.out.println ("Root of submission tree (-Droot="+DEFAULT_ROOT+"):");
	 System.out.println (db.root.getCanonicalPath());
	 System.out.println ("  Root exists?    " + db.root.exists());
	 System.out.println ("  Root is dir?    " + db.root.isDirectory());
	 System.out.println ("  Root writable?  " + db.root.canWrite());
	 db.print (System.out);
	 SubmitServer.printStars ();
      }
      if (!db.directory.canWrite()) throw new RuntimeException ("can't find or write on dir: " + db.directory.getCanonicalPath());
      if (!db.root.canWrite()) throw new RuntimeException ("can't find or write on dir: " + db.root.getCanonicalPath());
      return db;
   }


   public synchronized void save () throws IOException {
      // This deletes all the old files!
      final FileOutputStream  fos = new FileOutputStream(new File (directory,"info.tsp"));
      final ObjectOutputStream oo = new ObjectOutputStream (fos);
      oo.writeObject (date);
      oo.close();

      Registration.save (new File (directory, "info.reg"), registered);
   }

   /*
     Arbitrary control codes are used as keys in hash table.  Can't allow two registrations
     with same control code.  Also if names are too similar, this is not only suspicious,
     but we need unique directory names for each submitter.
   */
   public String register (final String last_name, final String first_name, final String email) throws IOException {
      final Registration r = new Registration (last_name, first_name, email);
      return register (r.getKey(), r);
   }

   private synchronized String register (final String control, final Registration o) throws IOException {
      // o is a newly created registration structure and control is its key

      if (isRegistered (control)) {
         // Control number has been given to someone already in the database ...
         // Could this happen by chance?;
	 throw new RuntimeException ("key already in use?!  [Try again]");
      } else if (o == null) {
         // Impossible!?
	 throw new RuntimeException ("null registration?!");
      }

      Registration r = o.close (registered);
      boolean reregistration = false;
      if (r!=null) {
         // The propsed registration 'o' is not close to existing registration
         if (r.isActivated ()) {
            if (SubmitServer.VERBOSE>2) {
               System.out.println ("Attempted registration: " + o);
               System.out.println ("Existing registration:  " + r);
            }
            throw new RuntimeException (o.toString (true) + " resembles an existing registration");
         }
         /*
             Removing an unactivated registration.  Allows a
             registrant to modify incorrect information (like bad
             e-mail address) without help from the administrator.

             We could be removing something close, but not identical
             to the new registration.
         */
         reregistration = true;
         if (SubmitServer.VERBOSE>2) {
            System.out.println ("Removing an inactive registration: " + r);
            System.out.println ("Registering                      : " + o);
         }
         registered.remove (r.getKey());  // DANGER.  Removing a registration

         // Check again
         r = o.close (registered);
         if (r!=null) {
            if (SubmitServer.VERBOSE>2) {
               System.out.println ("Attempted registration: " + o);
               System.out.println ("Existing registration:  " + r);
            }
            throw new RuntimeException (o.toString (true) + " resembles an existing registration");
         }
      }

      boolean delete = true;
      assert (!isRegistered (control));
      try {
         registered.put (control, o);
         save ();
         delete = false;
      } finally {
         if (delete) registered.remove (control);
      }
      if (! isRegistered (control)) {
	 throw new RuntimeException ("registration failed; couldn't save server's registration file");
      }

      delete = true;
      try {
         if (reregistration) {
            /* WHAT?? */
            // Mail.main (mailhost, o.getEMail(), o.fullName(), "submit server registration", "The submit server <submit@cs.fit.edu>", o.getReregisterMessage());
            Connection.mail (o, "submit server registration", o.getReregisterMessage());
         } else {
            //Mail.main (mailhost, o.getEMail(), o.fullName(), "submit server registration", "The submit server <submit@cs.fit.edu>", o.getHelpMessage());
            Connection.mail (o,"submit server registration", o.getHelpMessage());
         }
         delete = false;
      } catch (Exception ex) {
         ex.printStackTrace();
         delete = true;
      }

      if (delete) {
         registered.remove (control);
	 throw new RuntimeException ("registration failed; couldn't confirm registration by email");
      }

      // Consider returning
      //   0 failure (impossible)
      //   1 first registration
      //   2 reregistration
      final int ret;
      if (delete) {
         ret = 0;
      } else if (reregistration) {
         ret = 2;
      } else {
         ret = 1;
      }
      return (o.getControl());
   }

   public Registration isRegistered (final String first_name, final String last_name) {
      return Registration.isRegistered (registered, first_name, last_name);
   }

   public boolean isRegistered (final String control) {
      return control!=null && registered.containsKey (control);
   }

   public Registration getRegistration (final String entry) {
      return registered.get (entry);
   }

   public void activateRegistration (final String control) throws IOException {
      // It is possible that control==null!
      if (control==null) return;
      final Registration r = getRegistration (control);
      if (r==null) return;
      if (r.isActivated()) return;   // already activated
      r.setActivated (true);
      save (); // commit registration -- write files
   }


   public static boolean submissionsOn (File dir) {
      final File b = new File (dir, "ON");
      return (b.exists());
   }

   // Print course projects
   public void printProjects (final PrintStream out) {
      Projects.printOnProjects (root, out);
   }

   public List<String> onProjectsByCourse (final String course) {
      return Projects.onProjectsByCourse (root);
   }


   public boolean isClass (final String class_name) {
      final String [] classes = getClasses ();
      return java.util.Arrays.binarySearch (classes, class_name) >= 0;
   }

   // Why am I getting non-directories in this list!?

   String [] getClasses () {
      final String [] list = root.list (new FilenameFilter() {
         public boolean accept (File f, String s) {
            return f.isDirectory();
         }
      });
      Arrays.sort (list);
      return (list);
   }
      

   public void print (PrintStream out) {
      out.println ();
      printRegistrations (out);
      out.println ("\nCourses:");
      out.println (Arrays.toString (getClasses()));
      out.println ("\nProjects (*=ON):");
      printProjects (out);
      out.println ();
   }

   public void printRegistrations (PrintStream out) {
      out.println ("Registrations " + registered.size());
      for (Registration r: registered.values()) {
         out.println (r);
      }
   }

   public static void main (String[] args) throws Exception {
      Database db = init();
      db.print (System.out);
      db.save();
   }

}
