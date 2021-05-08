package submit.client;

import java.io.*;
import java.util.Map;
import java.util.ArrayList;
import java.util.Properties;

public final class Parameters {

   private final Properties properties;
   private final File properties_file;   // the changing parameters are pinned to a file
   
   private Parameters (Properties p, File f)  {
      properties = p;
      properties_file = f;
      currentParameters = this;
   }

   private Parameters ()  {
      this (Defaults.makeDefaultFile ());
   }

   private Parameters (String d, String n) {
      this (Defaults.makeDefaultFile (d, n));
   }

   Parameters (File file)  {
      this (Defaults.makeProperties (file), file);
   }

   private Parameters (Properties p)  {
      properties = p;
      properties_file = null;
   }

   public Parameters (String command) {
      this (Defaults.INITIAL_SERVER, Defaults.INITIAL_PORT, command);
   } 

   public Parameters (String server, int port, String command) {
      this (Defaults.makeSimpleProperties (server, port, command), null);
   }

   public Properties getProperties() { return properties; }
   public Properties getProperties (Map<String,String> add) {
      final Properties p = new Properties ();
      p.putAll (properties);
      p.putAll (add);
      return p;
   }
   
   
   public String toString () { return properties.toString(); }

   public File getFile () { return properties_file; }
   public File getDirectory () { return getFile().getParentFile(); }

   public String fileInfo () {
      final File file = properties_file;
      final File directory = properties_file.getParentFile();

      final StringBuffer b = new StringBuffer (500);
      b.append ("The parameters file stores individual user information");
      b.append (Defaults.line_separator);
      b.append ("allowing you to use the submit server conveniently.");
      b.append (Defaults.line_separator);
      b.append ("By default the parameters file is called '"+Defaults.file_name+"'.");
      b.append (Defaults.line_separator);
      b.append ("The location and name of this file can be overriden by using Java properties as follows:");
      b.append (Defaults.line_separator);
      b.append ("   the parameters directory \"-Ddirectory="+Defaults.directory_name+"\"");
      b.append (Defaults.line_separator);
      b.append ("   the parameters file name \"-Dfile="+Defaults.file_name+"\"");
      b.append (Defaults.line_separator);
      b.append ("The current values of these variables are as follows:");
      b.append (Defaults.line_separator);
      b.append ("directory="+directory.getAbsolutePath());
      b.append (Defaults.line_separator);
      b.append ("   Does it exists?    " + directory.exists());
      b.append (Defaults.line_separator);
      b.append ("   Is it a directory? " + directory.isDirectory());
      b.append (Defaults.line_separator);
      b.append ("   Is it writable?    " + directory.canWrite());
      b.append (Defaults.line_separator);
      b.append ("file="+file.getName());
      b.append (Defaults.line_separator);
      b.append ("   Does it exists?    " + file.exists());
      b.append (Defaults.line_separator);
      b.append ("   Is it a file?      " + !file.isDirectory());
      b.append (Defaults.line_separator);
      b.append ("   Is it writable?    " + file.canWrite());
      b.append (Defaults.line_separator);
      if (file.exists()) {
	 b.append ("  Size of file:      " + file.length() + " bytes");
	 b.append (Defaults.line_separator);
	 b.append ("  Last modified:     " + Defaults.formatDate(file.lastModified()));
	 b.append (Defaults.line_separator);
      }
      return b.toString();
   }

   //  Do system parameters get reconsulted?
   public static Parameters rereadParameters (String d, String n) throws IOException {
      return rereadParameters (Defaults.makeDefaultFile (d, n));
   }

   //  Do system parameters get reconsulted?
   public static Parameters rereadParameters (File n) throws IOException {
      // Check if file exists?
      if (!n.exists()) throw new FileNotFoundException ();
      currentParameters = new Parameters (n);
      return currentParameters;
   }
  

   public int  getPort ()      {
      try {
	 return Integer.parseInt (properties.getProperty ("port"));
      } catch (NumberFormatException ex) {
         // Only legal integer values should ever be put into properties
         ex.printStackTrace (System.err);
	 return 0;
      }
   }
   public void setPort (final int p) {
      if (p!=getPort()) {
	 properties.put ("port", Integer.toString(p));
	 //dirty = true;
      }
   }

   public String getParameter (final String name)             { return properties.getProperty (name); }
   public void   setParameter (final String name, String s)   {
      assert (name!=null && name.length()>0);
      assert (s!=null);
      s = s.trim();       //  NB.  trim()!!!!!
      if (s.length()==0) throw new IllegalArgumentException ("empty parameter="+name);
      properties.put (name, s);
   }
   public void cleanParameter (final String name) {
      final String value = properties.getProperty (name);
      if (value!=null) {
         
      }
   }

   public Object remove (String name) {
      assert (name!=null && name.length()>0);
      return properties.remove (name);
   }
      
   public boolean containsKey (String name) {
      assert (name!=null && name.length()>0);
      return properties.containsKey (name);
   }

   public boolean missing (String name) {
      assert (name!=null && name.length()>0);
      return properties.getProperty (name)==null;
   }


   public String  getServer ()         { return properties.getProperty ("server"); }
   public void    setServer (String s) { properties.put ("server", s); }
   public boolean isDefaultServer () { return getServer().equals (Defaults.DEFAULT_SERVER); }
   public void    setServerToDefault () { setServer (Defaults.DEFAULT_SERVER); }

   public String getFirstName ()       { return properties.getProperty ("first_name"); }
   public void   setFirstName (String s) { properties.put ("first_name", s); }
   public String getLastName ()        { return properties.getProperty ("last_name"); }
   public void   setLastName (String s) { properties.put ("last_name", s); }
   public String getControl ()               { return properties.getProperty ("control"); }
   public void   setControl (String s)       { properties.put ("control", s); }
   private String getEMail ()           { return properties.getProperty ("email"); }
   private void   setEmail (String s)   { properties.put ("email", s); }
   public String getCourse ()             { return properties.getProperty ("course"); }
   public void   setCourse (String s)   { properties.put ("course", s); }
   public String getProject ()             { return properties.getProperty ("project"); }
   private void   setProject (String s)   { properties.put ("project", s); }

   // Peeking is no longer the default.
   public boolean getPeek () {
      final String y = properties.getProperty ("peek");
      final String x = properties.getProperty ("nopeek");
      if (x!=null) return false;
      if (y!=null) return true;
      return false;
   }

   public boolean fileOnly () {
      if (Defaults.VERBOSE>3) {
         System.out.println ("FILEONLY");
         System.out.println (properties);
      }
      final String x = properties.getProperty ("relative");
      if (x!=null) return false;
      return true;
   }

   public String getDisplayName () {
      return properties.get ("first_name") + "  " +
	 properties.get("last_name") +
	 " (" + properties.get("control")+")";
   }

   private static void copySystemProperties (Properties ps) {
      for (java.util.Enumeration<?> en = System.getProperties().propertyNames(); en.hasMoreElements();) {
         final String key = (String) en.nextElement();
         if (key.startsWith ("submit")) ps.put (key, System.getProperty(key));
      }
   }

   private static void copySystemProperty (Properties ps, String p) {
      if (System.getProperty (p)!=null) ps.put (p,System.getProperty(p));
   }


   public void writeObject (final String command, final ObjectOutputStream oos) throws IOException {
      writeObject (command, null, oos);
   }

   public void writeObject (final String command, final Map<String,String> m, final ObjectOutputStream oos) throws IOException {
      final Properties p = (Properties) properties.clone();
      if (Defaults.VERBOSE>5) {
         System.out.println ("Parameters to begin with:  ");
         System.out.println (p);
      }
      p.remove ("server");      // No longer needed
      p.remove ("port");        // No longer needed
      p.remove ("directory");   // Convenient for client only
      p.remove ("current");     // Convenient for client only
      p.remove ("relative");    // Convenient for client only
      p.remove ("nopeek");      // Convenient for client only
      p.remove ("file");        // Convenient for client only
      p.remove ("nofiles");     // Convenient for client only
      if (m!=null) p.putAll (m);
      p.put ("nopeek", String.format("%B", !getPeek()));
      p.put ("command", command);
      p.put ("host", Defaults.HOST);
      p.put ("user.name",     System.getProperty ("user.name"));
      p.put ("file.encoding", System.getProperty ("file.encoding"));
      p.put ("client.version", Defaults.VERSION);

      copySystemProperty (p, "http.remote.addr");
      copySystemProperty (p, "http.remote.host");
      copySystemProperty (p, "http.referer");
      copySystemProperties (p);  // that begin with "submit"

      if (Defaults.VERBOSE>4 && m!=null) {
         System.out.println ("Parameters in file:  ");
         System.out.println (m);
      }
      if (Defaults.VERBOSE>4) {
         System.out.println ("Parameters to write:  ");
         System.out.println (p);
      }

      javax.crypto.SealedObject o = submit.shared.Encryption.sealObject ((java.io.Serializable)p);
      oos.writeObject (o);
   }

   public static void main (String [] args) throws IOException {
      System.out.println ();
      Defaults.printStars();
      System.out.println ("***   Unit test class 'Parameters'   ***\n current value of parameters:");
      System.out.println (currentParameters);
      System.out.println ("\n all system properties:");
      System.getProperties().list(System.out);
      System.out.println ("\n all environment variables:");
      System.out.println (System.getenv());
   }

   private static Parameters currentParameters;

   static {
      /*
        We don't really want to do this if there is a possibility of a file
        containing the submit parameters.  This might occur when a JSP client
        is executing this code, for example.
       */
      currentParameters = new Parameters ();
      if (Defaults.VERBOSE>2) {
         System.out.print (currentParameters.fileInfo ());
         Defaults.printStars();
      }
      if (Defaults.VERBOSE>3) {
         System.out.println ("The values of the parameters at start-up:");
         System.out.println (currentParameters);
         Defaults.printStars();
      }
   }

   public static Parameters currentParameters () { return currentParameters; }

   
}
