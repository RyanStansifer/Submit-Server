package submit.server;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

// All known subclasses:  Course

abstract public class Context {

   // Note:  All times in UTC/"Zulu" military time, eg., 2007-08-21T14:03:01:002Z
   private static final String dtz = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
   private static final SimpleDateFormat formatz = new SimpleDateFormat (dtz);
   static {
      formatz.setTimeZone (TimeZone.getTimeZone("UTC"));
   }
   protected static String formatUTC (long d) { return formatUTC (new Date(d)); }
   protected static String formatUTC (Date d) { return formatz.format (d); }

   protected final String control;
   protected final long time_stamp;
   protected final File root;

   Context (File r, String c, long ts) throws ContextException {
      root=r;
      control=c;
      time_stamp=ts;
   }

   public static Context createContext (Parameters args) throws ContextException {
      if (args.containsKey ("control")) {
         if (args.containsKey ("course") && args.containsKey ("project")) {
            return new Course (args);
         } else if (args.containsKey ("course") && !args.containsKey ("project")) {
            throw new ContextException ("Missing 'project' information.");
         } else if (!args.containsKey ("course") && args.containsKey ("project")) {
            throw new ContextException ("Missing 'course' information.");
         } else {
            throw new ContextException ("The disposition of the submission could not be determined.");
         }
      } else {
         throw new ContextException ("The control code is required.");
      }
   }

   abstract public File submissionJarFile () throws ContextException ;
   public File createSubmissionFile () throws ContextException, IOException { return null; }
}
