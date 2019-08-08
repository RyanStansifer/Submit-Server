/*
  Registration.java 

    Used in the server's database, but also in the submission's
    manifest and so by the client as well.  Registrations are
    serialized and written to a file by the server to keep the
    information about all registrants in persistent storage.

    So, modifying this file while during operation can lead to trouble.

 */
package submit.shared;

import java.util.Date;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.math.BigInteger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.Collator;
import java.text.Normalizer;  // Normalizer.normalize(string, Normalizer.Form.NFD);

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;

/*
  This class implements comparable, do not subclass it.
*/

public final class Registration implements Comparable <Registration>, Serializable {

   /*
      On 29 Jan 2013:
      > serialver submit.shared.Registration
   */
   static final long serialVersionUID = -3237593357848649027L;

   private final String control, last_name, first_name, email;
   private final Date timestamp;  // Creation date

   /*
     control number, last name, first name, e-mail, time stamp
   */
   private Registration (final String control, final String last_name, final String first_name, final String email, final Date d, final boolean a) {
      if (control==null)    throw new IllegalArgumentException ("null control info");
      if (d==null)          throw new IllegalArgumentException ("null time stamp");

      checkLastName (last_name);  // May raise IllegalArgumentException
      checkLastName (first_name); // May raise IllegalArgumentException
      checkEMail(email);         // May raise IllegalArgumentException

      this.control = control;        // lower case
      this.last_name = last_name;
      this.first_name = first_name;
      this.email = email;
      this.timestamp = d;
      this.activated = a;
   }

   private Registration (String control, String last_name, String first_name, final String email, String date, String activated) throws ParseException {
      this (control, last_name, first_name, email, dt.parse (date), Boolean.valueOf (activated));
   }

   private final static int BASE = 35;  // upto to 36 soemwaht arbitary to get letters and digits

   // Database.java creates new Registration
   public Registration (String last_name, String first_name, final String email) {
      this (createReadableControl().toString(BASE), last_name, first_name, email, new Date(), false);
   }

   /*
      Begin with (upper or lower case) letter, followed by letter,
      space, apostrophe, period, dash, and ends with (upper or lower
      case) letter.  So, length 2 or more characters.
   */
   private final static Pattern name_pattern =
      Pattern.compile ("[\\p{Lu}\\p{Ll}][\\p{Lu}\\p{Ll} '.-]*[\\p{Lu}\\p{Ll}]");

   private static boolean verifyName (final String name) {
      return (name_pattern.matcher (name).matches());
   }

   // Should be something like:  [a-z0-9._-]+[@]([a-z0-9-]+[.])+(com|net|edu|us|gov)
   // Or how about:  ^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$
   // The purpose is to prevent "bad" characters messing up the display.
   private final static Pattern address_pattern =
      Pattern.compile ("[\\p{Alnum}_\\.+-]+@[\\p{Alnum}_\\.+-]+");

   private boolean verifyEMail (final String email) {
      return address_pattern.matcher (email).matches();
   }

   private void checkEMail (final String email) {
      if (email==null) throw new IllegalArgumentException ("null email address");
      if (!verifyEMail (email)) {
         throw new IllegalArgumentException ("unacceptable email address");
      }
   }

   private void checkLastName (final String last_name) {
      if (last_name==null)  throw new IllegalArgumentException ("null last name");
      if (!verifyName (last_name)) {
         throw new IllegalArgumentException ("unacceptable last name");
      }
   }

   private void checkFirstName (final String first_name) {
      if (last_name==null)  throw new IllegalArgumentException ("null first name");
      // Possibly accept empty string as a valid first name??
      if (!verifyName (first_name)) {
         throw new IllegalArgumentException ("unacceptable first name");
      }
   }

   // Order registrations by natural language order.
   private static Collator collate = Collator.getInstance();

   public int compareTo (final Registration r) {
      if (this == r) return 0;
      if (collate.compare (last_name, r.last_name)<0) return -1;
      if (collate.compare (last_name, r.last_name)>0) return +1;
      if (collate.compare (first_name, r.first_name)<0) return -1;
      if (collate.compare (first_name, r.first_name)>0) return +1;
      if (control.compareTo(r.control)<0) return -1;
      if (control.compareTo(r.control)>0) return +1;
      if (email.compareTo(r.email)<0) return -1;
      if (email.compareTo(r.email)>0) return +1;
      assert false;
      return 0;
   }

   /*
      A registration is only created once! 
   */

   @Override
   public boolean equals (Object that) {
      if (this == that) return true;
      if (! (that instanceof Registration)) return false;
      final Registration r = (Registration) that;
      assert ! email.equals (r.email);
      assert ! control.equals (r.control);
      return false;
   }

   // A class that overrides equals most also override hashCode
   @Override
   public int hashCode () { return control.hashCode(); }
   
   /* Set the Most Significant Bit; a new series of control codes can be created
      by increasing the length by 1. */

   private static final int MSB = 41; // Changed 8 Aug 2019

   private static BigInteger createControl () {
      return new BigInteger (MSB, new Random ()).setBit(MSB);
   }

   private static boolean ambiguous (final String x) {
      // exclude 'O' (the letter O)
      return x.indexOf('O')!=-1 || x.indexOf('L')!=-1;
   }
   private static boolean ambiguous (final BigInteger b) {
      return ambiguous (b.toString(BASE).toUpperCase());
   }

   private static BigInteger createReadableControl () {
      for (;;) {
         final BigInteger b = createControl();
         // How do we know we didn't randomly create a control code already in use?
         // ambiguous() does not check; left to DataBase.register() to deal with
         if (!ambiguous (b)) return b;
      }
   }

   public String getKey ()      { return control; }
   public String getControl ()  { return control; }
   public String getEMail () { return email; }
   public String getHelpMessage () {
      return
         "Please keep this message, it contains your control code."+
	 "\nfirst_name="+first_name+
	 "\nlast_name="+last_name+
	 "\ncontrol="+getControl()+"\n"+
         "\nPlease observe the difference between the letter 'l' and the digit '1'."+
         "\nThe date and time of this registration is "+dt.format (timestamp)+"\n";
   }

   public String getReregisterMessage () {
      return
         "Please keep this message, it contains your control code."+
         "\nIgnore any previous control codes."+
	 "\nfirst_name="+first_name+
	 "\nlast_name="+last_name+
	 "\ncontrol="+getControl()+"\n"+
         "\nPlease observe the difference between the letter 'l' and the digit '1'."+
         "\nThe date and time of this registration is "+dt.format (timestamp)+"\n";
   }

   // The characters " ", ".", "-", "'" might legitimately appear in names, but we may not
   // want them in, say, a directory name. 
   private static String squash (final String s) {
      assert s.indexOf ('/')==-1;
      return
         Normalizer.normalize(s, Normalizer.Form.NFD). // separate diacritics
            replaceAll ("[\\p{InCombiningDiacriticalMarks}]+", "").   // remove diacritics
               replaceAll ("[ .\\-']+", "").   // remove troublesome ASCII characters
                  toLowerCase().                  // covert to lower case
                     replaceAll("[^\\p{ASCII}]", "Q");   // flatten to ASCII
   }

   private static String substring (final String s, int length) {
      return s.substring (0, Math.min (length, s.length()));
   }

   private String getSquashedName () throws PatternSyntaxException {
      return substring (substring (squash(last_name),8)+"_"+squash(first_name), 12);
   }

   /*
      2- 8 no more than 8 non-space characters of last_name
      3- 9 "_"
      5-12 no more than 12 non-space characters of last_name_first_name
      6-13 "_"
   */
   public String getNameAsDirectory () {
      return getSquashedName();
   }

   private boolean close (final Registration r) {
      if (control.equals (r.control)) return true;  // prevents randomly creating same key
      if (getSquashedName().equals (r.getSquashedName())) return true;
      if (getEMail().toLowerCase().equals (r.getEMail().toLowerCase())) return true;
      return false;
   }

   /*
     Is this proposed entry close to anything already registered?
     (checked in Database.java)
   */
   public Registration close (final Map<String,Registration> h) {
      for (Registration r: h.values()) {
	 if (close(r)) return r;
      }
      return null;  // nothing close
   }

   /*
     Can't have two registrations that differ only by the case of the names.
   */
   public boolean sameName (final String first_name, final String last_name) {
      if (! this.first_name.equalsIgnoreCase (first_name)) return false;
      if (! this.last_name.equalsIgnoreCase (last_name)) return false;
      return true;
   }

   public static Registration isRegistered (final Map<String,Registration> h, String first_name, String last_name) {
      for (Registration r: h.values()) {
	 if (r.sameName (first_name, last_name)) return r;
      }
      return null;
   }

   public String fullName () {
      return (first_name+" "+last_name);
   }

   private boolean consistentWithName () {
      return false;
   }

   private /* nonfinal */ boolean activated = false;

   public boolean isActivated ()   { return activated; }
   public void setActivated (boolean a) { activated=a; }

   public String toString () {
      return (last_name+", "+first_name+" ("+control+"), email="+email+", activated="+activated);
   }

   public String toString (boolean b) {
      if (b) {
	 return (last_name+", "+first_name+" <"+email+">");
      } else {
	 return (last_name+", "+first_name);
      }
   }

   public String toString (int i) {
      if (i==0) {
         return (last_name+", "+first_name+" ("+control+"), email="+email);
      } else {
	 return (last_name+", "+first_name);
      }
   }

   public static void main (final String [] args) throws IOException, ParseException {
      final BufferedReader br = new BufferedReader (new InputStreamReader (System.in));
      for (;;) {
	 final String line = br.readLine();
	 if (line==null) break;
	 final StringTokenizer st = new StringTokenizer(line, ";");
	 Registration r = new Registration (st.nextToken(),st.nextToken(),st.nextToken());
	 final String s = r.format();
	 r = Registration.parse (s);
	 System.out.println (s);
      }
   }

   private static final DateFormat dt =  new SimpleDateFormat ("EEE, dd MMM yyyy, hh:mm a z");

   public String format () {
      final StringBuffer s = new StringBuffer ();
      s.append (control);
      s.append (';');
      s.append (last_name);
      s.append (';');
      s.append (first_name);
      s.append (';');
      s.append (email);
      s.append (';');
      s.append (dt.format (timestamp));
      s.append (';');
      s.append (activated);
      return (s.toString());
   }
   
   public static Registration parse (String line) throws IllegalArgumentException, ParseException {
      final StringTokenizer st = new StringTokenizer(line, ";");
      //                       control,         last_name,     first,name,    e-mail,        time stamp,  activated
      return new Registration (st.nextToken(),st.nextToken(),st.nextToken(),st.nextToken(),st.nextToken(),st.nextToken());
   }
      
   public static HashMap<String,Registration> create (InputStreamReader reader) throws IOException {
      final BufferedReader br = new BufferedReader (reader);
      final HashMap<String,Registration> h = new HashMap<String,Registration> ();
      for (;;) {
	 final String line = br.readLine();
	 if (line==null) break;
	 try {
	    final Registration r = Registration.parse (line);
	    h.put (r.getKey(), r);
	 } catch (ParseException e) {
	    System.err.println ("Badly formed registration line.");
	    System.err.println ("Ignoring: '"+line+"'");
	 } catch (IllegalArgumentException e) {
	    System.err.println ("Badly formed registration line.");
	    e.printStackTrace (System.err);
	    System.err.println ("Ignoring: '"+line+"'");
	 } catch (NoSuchElementException e) {
	    System.err.println ("Badly formed registration line.");
	    System.err.println ("Ignoring: '"+line+"'");
	 }
      }
      return h;
   }

   private static final String ENC = "UTF-8";

   public static HashMap<String,Registration> create (final File f) throws IOException {
      return create (new InputStreamReader (new FileInputStream(f) , ENC));
   }

   public static void save (final PrintWriter out, final HashMap<String,Registration> h) throws IOException {
      for (final Registration r: h.values()) {
	 out.println (r.format());
      }
      out.close();
   }

   public static void save (final File f, final HashMap<String,Registration> h) throws IOException {
      save (new PrintWriter (f, ENC), h);
   }
}
