package submit.server;

import java.io.Serializable;

import java.util.Date;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/*
  This class gets serialized in a class Properties in submit.shared.Manfest
*/
final class Parameters extends Properties implements Serializable {

   // Use our new series of version id
   static final long serialVersionUID = 5L;

   Parameters (final String n) {
      this (n, null);
   }

   Parameters (final String n, final Properties args) {
      putTimeStamp ();
      putRemoteHost (n);
      if (args!=null) {
         add (args);
      } else {
         put ("command", "bad");
      }
   }

   /*
     Override keys with values found in args.
   */
   void add (final Properties args) {
      for (Map.Entry<Object,Object> e: args.entrySet()) {
         if (e==null) continue;  // Huh?
         final String key = (String) e.getKey();
         if (key==null) continue;  // Huh?
         final String value = (String) e.getValue();
         /*
           Values are not
             a)  empty
             b)  empty strings
             c)  extremely long
         */
         if (value==null || value.length()<1 || value.length()>100) continue;
         // 1. Remove everything after and including the ";"
         // 2. Delete all non-printable characters
         put (key, value.trim().replaceAll (";.*","").replaceAll ("[^\\p{Print}]",""));
      }
   }

   private void putRemoteHost (final String host) {
      put ("remote_host", host);
   }

   // Note:  All times in local time, eg., Tue, 21 Aug 2007 at 2:03 PM EST
   static final String df = "EEE, dd MMM yyyy 'at' hh:mm a z";
   static final SimpleDateFormat format = new SimpleDateFormat (df);
   static String format (long d) { return format (new Date (d)); }
   static String format (Date d) { return format.format (d); }

   // Avoid getting the same time stamp twice.
   private void putTimeStamp () {
      putTimeStamp (System.currentTimeMillis());
   }
   private void putTimeStamp (final long date) {
      put ("time_stamp", Long.toString (date));
   }

   public long getTimeStamp () {
      return Long.valueOf (getProperty("time_stamp"));
   }
   private String getTimeStampAsString () {
      return format.format (getTimeStamp());
   }

   String getControl ()     { return getClean ("control"); }
   /*
	   The following requires all directories representing classes to have no
	   white space and be in lower case.  This is the convention I use:
	   cse1002, cse2010, cse4250, etc.  Clients may call the classes by
	   other names, e.g., "CSE 1002", and still find the directory.
           
           Some other characters ought to be permitted, e.g, cse4510-02.

           Will '/' cause problems?  Or will subdirectories be created?

           A directory (class name) like 'cse4250/5250' does not seem like a good idea.
   */
   String getCourseName ()  { return getClean ("course"); }
   String getProjectName()  { return getClean ("project"); }

   private final static Pattern INITIAL_PAT = Pattern.compile ("[a-z0-9_-]+");

   private final static String clean (final String s) {
      return s.replaceAll ("\\s", "").toLowerCase();
   }

   private final static String veryClean (final String s) {
      final Matcher init = INITIAL_PAT.matcher (clean(s));
      if (init.lookingAt()) {
         return init.group(0);
      } else {
         // Should not happen
         return s;
      }
   }

   private String getClean (final String key) {
      final String value = getProperty (key);
      if (value==null) return null;
      return veryClean (value);
   }

   public String toString () {
      final Properties copy = new Properties ();
      copy.putAll (this);
      copy.remove ("command");
      copy.remove ("time_stamp");
      return String.format ("%s on %s. %s", getProperty ("command"), getTimeStampAsString(), copy.toString());
   }
}
