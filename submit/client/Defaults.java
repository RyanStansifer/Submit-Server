package submit.client;

import java.io.*;


import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.net.InetAddress;

/*
                                                                    saved \ / sent
public  static final int    VERBOSE     //  -Dverbose                     o o  chatter on System.out
public  static final String VERSION     //               client.version   o x  version of software
private static final String HOST        //               host             o x  accountability purposes
                                        //  -Duser.name  user.name        o x  accountability purposes

private static       File   directory   //  -Ddirectory                   o o
private static       File   file        //  -Dfile                        o o

public  static       int    port        //  -Dport       port             x o
public  static       String server      //  -Dserver     server           x o
public  static       String current     //  -Dcurrent    current          x o

                                        //  -Dcode       code             x x
                                        //  -Dclass      class            x x
                                        //  -Dproject    project          ? x

*/


public final class Defaults {

   /*
    */
   private static final ResourceBundle bundle;
   
   static {
      ResourceBundle b = null;
      try {
         b = ResourceBundle.getBundle ("resources/bundles/submit");
      } catch (MissingResourceException ex) {
         ex.printStackTrace (System.err);
	 System.err.println ("Continuing with null bundle.");
      } finally {
         bundle = b;
      }
   }

   public static String getString (final String name) throws NullPointerException, MissingResourceException {
      return bundle.getString (name);
   }

   public static String getLocalizedString (final String name, final String def) {
      if (bundle==null) return def;
      try {
         return bundle.getString (name);
      } catch (MissingResourceException ex) {
         // The defaults are really supposed to be in the bundle!
         return def;
      }
   }


   /*
     The formatting of dates is done uniformally accross the application in the
     manner provided here (for lack of a better place).

     Looking up the string had better not raise an exception!
   */
   private static final DateFormat format = new SimpleDateFormat (getLocalizedString ("submit.date.format", "EEE, dd MMM yyyy, 'at' hh:mm a z"));
   public static String formatDate (Date d) { return format.format (d); }
   public static String formatDate (long d) { return format.format (new Date(d)); }
   public static String date()              { return format.format (new Date()); }


   /*
     Client Version
   */
   public static final String VERSION = "Submit client 8.1 -- 6 Oct 2012";


   /*
     Parameter "VERBOSE" for controlling messages
   */
   public static final int VERBOSE;
   static {
      final String v = System.getProperty ("verbose");
      if (v==null) {
	 VERBOSE = 1;
      } else if (v.equals ("")) {
	 VERBOSE = 2;
      } else {
	 int x=3;
	 try {
	    x = Integer.parseInt (v);
	 } catch (NumberFormatException ex) {
	    x = 4;
	    ex.printStackTrace (System.err);
	    System.err.println ("Property \"verbose\" must be an integer; found -Dverbose=\""+v+"\"");
	    System.err.println ("Continuing as if -Dverbose=\""+x+"\"");
	 } finally {
	    VERBOSE = x;
	 }
      }
   }


   // Initialize parameter "HOST" for identifying the user
   final static String HOST;
   static {
      String x = null;
      try {
	 x = InetAddress.getLocalHost().toString();
      } catch (Exception e) {
	 x = "[unknown]";
      }
      HOST = x;
   }

   public static final String DEFAULT_SERVER = "submit.cs.fit.edu";
   static final String INITIAL_SERVER = System.getProperty ("server", DEFAULT_SERVER);

   public static final int DEFAULT_PORT = 13263;
   static final int INITIAL_PORT = Integer.getInteger ("port", DEFAULT_PORT);

   // Informational Banner
   public static final String line_separator = System.getProperty ("line.separator");
   public static final String banner;
   static {
      final StringBuffer b = new StringBuffer (400);
      b.append ("Submit client, "+VERSION+", by Ryan Stansifer;");
      b.append (" verbose="+VERBOSE+".");
      b.append (line_separator);

      if (VERBOSE>1) {
	 b.append ("Java version " + System.getProperty ("java.version") + " from " + System.getProperty ("java.vendor"));
	 b.append (" and API version " + System.getProperty ("java.class.version") + ".");
	 b.append (line_separator);
	 b.append ("The Java classpath is '" + System.getProperty("java.class.path") + "'");
	 b.append (line_separator);
	 b.append ("The host is '" + HOST +"'; the user is '" + System.getProperty ("user.name"));
	 b.append ("' under " + System.getProperty ("os.name") +
		   " " + System.getProperty ("os.version") + "/" + System.getProperty ("os.arch") + ".");
	 b.append (line_separator);
         /*
	 b.append ("The language is " + System.getProperty("user.language"));
	 b.append (line_separator);
	 b.append ("The country is " + System.getProperty("user.country"));
	 b.append (line_separator);
         */
	 b.append ("The locale is " + java.util.Locale.getDefault().getDisplayName());
	 b.append (", and the default file encoding is '" + System.getProperty ("file.encoding") +"'.");
	 b.append (line_separator);
	 b.append ("The current user directory ('user.dir') is \""+System.getProperty("user.dir")+"\".");
	 b.append (line_separator);
	 b.append ("The home directory ('user.home') is \""+System.getProperty("user.home")+"\".");
	 b.append (line_separator);
      }

      b.append ("The date and time at start-up is " + date() + ".");
      b.append (line_separator);

      banner = b.toString();
   }

   public static void printStars () {
      System.out.println ("****************************************************************");
   }

   static {
      if (VERBOSE>1) {
	 printStars();
	 System.out.print (banner);
	 printStars();
      }
   }


   private final static boolean windows = System.getProperty ("os.name").startsWith ("Window");


   /*
     The current directory is not the working directory in which the program is
     running, but rather a likely place in which to find files to submit.
     Should this be a function?  The string is only important at start-up.
   */
   private final static String DEFAULT_CURRENT_DIRECTORY;
   static {
      final String user_home = System.getProperty ("user.home");
      if (windows) {
         final File home = WindowsFileSystem.getHomeDirectory();
         if (home!=null && home.exists()) {
            DEFAULT_CURRENT_DIRECTORY = home.getAbsolutePath();
         } else {
            DEFAULT_CURRENT_DIRECTORY = System.getProperty ("user.home");
         }
      } else {
         DEFAULT_CURRENT_DIRECTORY = System.getProperty ("user.home");
      }
   }

   /*
     Figure out the name of the submit parameters file.  This string is important
     only at start-up.
    */
   private final static String WINDOWS_DEFAULT_FILE_NAME = "SUBMIT_PARAMETERS";
   private final static String UNIX_DEFAULT_FILE_NAME = ".submit";
   final static String file_name;
   static {
      if (windows) {
         file_name = System.getProperty ("file", WINDOWS_DEFAULT_FILE_NAME);
      } else {
         file_name = System.getProperty ("file", UNIX_DEFAULT_FILE_NAME);
      }
   }


   /*
     Figure out the directory of the submit parameters file.
    */
   final static String directory_name;


   private final static java.util.List<File> DEFAULT_SEARCH_DIRECTORY = new java.util.ArrayList<File>();
   private static void add (String directory) {
      if (directory!=null) {
         final File directoryAsFile = new File (directory);
         if (directoryAsFile.exists() && directoryAsFile.isDirectory()) {
            DEFAULT_SEARCH_DIRECTORY.add (directoryAsFile);
         }
      }
   }

   static {
      final String user_home = System.getProperty ("user.home");
      if (windows) {
         add (System.getenv ("HOMESHARE"));  // \\udrive.fit.edu\ryan
         add (System.getenv ("APPDATA"));     // C:Doc and Settings\ryan\App Data
         if (DEFAULT_SEARCH_DIRECTORY.size()==0) {
            add (user_home);
         }
      } else {
         add (user_home);
      }

      if (VERBOSE>2) {
         System.out.format ("Dirs to search for parameters: %s%n", DEFAULT_SEARCH_DIRECTORY);
      }

      File s = null;
      for (File f: DEFAULT_SEARCH_DIRECTORY) {
         final File x = new File (f, file_name);
         if (x.exists() && ! x.isDirectory() && x.canRead()) {
            s = x;
            if (VERBOSE>3) {
               System.out.format ("Found parameters file: %s%n", s);
            }
            break;
         }
      }

      if (s==null) s = new File (user_home, file_name);
      directory_name = s.getParentFile().getAbsolutePath();
   }


   static {

      //  Search root drives

      final File [] file_system_roots;

      if (windows) {
         // Should give preference to likely home directories


         // System.out.println (System.getenv());

	 //file_system_roots = WindowsFileSystem.getMyComputerPathList();
	 file_system_roots = new File [] { new File("C:\\"), new File("U:\\") };
      } else {
	 file_system_roots = null;
      }
      

      String a_root_directory = null;
      for (int j=0;file_system_roots!=null && j<file_system_roots.length;j++) {
	 final String path = file_system_roots[j].getAbsolutePath();

	 /*
	   Watch the abort/retry/continue pop-up on a: drive on some systems!!
	   Assume that its a floppy iff it starts with "a".
           But what about zip drives and other removable media?
	 */
	 if (path.toLowerCase().startsWith ("a")) continue;

	 if (new File (file_system_roots[j], file_name). exists()) {
	    a_root_directory = path;
	    break;  // end search
	 }
      }

      /*
        Even if we can't find an existing parameter files, then
        set some plausible place to write one out.
      */

      if (a_root_directory!=null) {
	 //directory_name = a_root_directory;
      } else if (windows) {
         final File home = WindowsFileSystem.getHomeDirectory();
         // parent(home) equal to user.home on all Windows OS?
         // convert to ..\Application Data\Submit Server\ ?
	 //directory_name = home.getAbsolutePath();
      } else {
	 //directory_name = System.getProperty("directory", System.getProperty("user.home"));
      }
   }


   static File makeDefaultFile ()  {
      return makeDefaultFile (directory_name, file_name);
   }

   static File makeDefaultFile (String dir, String name)  {
      final File f = new File (new File (dir), name);
      return f;
   }

   private static Properties fileProperties (File file) {
      final Properties file_props  = new Properties ();

      // Default values, allowed to be overriden.  "server" and "port" have been checked.
      // Will be overriden by values in file, and then by -D properties
      file_props.put ("server",  INITIAL_SERVER);
      file_props.put ("port",    Integer.toString(INITIAL_PORT));
      file_props.put ("current", DEFAULT_CURRENT_DIRECTORY);

      if (file.exists() && file.canRead() && !file.isDirectory()) {
	 try {
            FileInputStream fis=null;
            try {
               fis = new FileInputStream (file);
               file_props.load (fis);
            } finally {
               if (fis!=null) fis.close();
            }
	 } catch (IOException ex) {
	    ex.printStackTrace (System.err);
	    System.err.println ("Trouble reading parameter files; continuing ...");
	 }
      }
      return file_props;
   }


   /*
     NOFILES == true means that the submission of zero files is permitted (presumably
       the parameters are of interest)

     NOFILES == false means that the at least one file must be submitted

     Boolean.getBoolean(String name) returns true if and only if the system
     property named by the argument exists and is equal to the string "true".

     So, the default value of NOFILES is false;

   */
   public static final Boolean NOFILES = Boolean.getBoolean ("nofiles");

   // Read parameter "project" (especially from command line); but don't write it
   static String [] props = {
      "server","port","last_name","first_name","email","control","course","current","project","problem","archive","password","nopeek","nofiles","relative"
   };

   private static Properties overrideProperties (Properties def) {
      final Properties final_props = new Properties ();
      for (int i=0; i<props.length; i++) {
	 final String value = System.getProperty(props[i],def.getProperty(props[i]));
         // System.out.format ("override key=%s,value=%s;%s;%s%n", props[i], value, System.getProperty(props[i]), def.getProperty(props[i]));
	 if (value!=null) final_props.put (props[i], value);
      }
      return final_props;
   }

   static Properties makeProperties (File file)  {
      final Properties final_props  = overrideProperties (fileProperties (file));
      return final_props;
   }

   static Properties makeSimpleProperties (String command) {
      return makeSimpleProperties (INITIAL_SERVER, INITIAL_PORT, command);
   }

   static Properties makeSimpleProperties (String server, int port, String command) {
      final Properties p = new Properties ();
      p.put ("server", server);
      p.put ("port", Integer.toString(port));
      p.put ("command", command);
      return p;
   }

   public static void main (String [] args)  {
   }

}
