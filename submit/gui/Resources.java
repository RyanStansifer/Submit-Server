package submit.gui;

import java.util.Properties;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

public final class Resources {

   private Resources () {}

   private static ResourceBundle resources = null;

   static {
      try {
	 resources = ResourceBundle.getBundle ("resources/bundles/submit"); // may raise MRE
      } catch (MissingResourceException ex) {
	 ex.printStackTrace(System.err);
         System.exit(1);
      } catch (Exception ex) {
	 ex.printStackTrace(System.err);
      }
   }

   public final static String getResource (String key) {
      return resources.getString (key);
   }

   private final static String getResource (String key, String def) {
      if (resources==null) {
	 return def;
      } else {
	 String x;
	 try {
	    x = resources.getString (key);
	 } catch (MissingResourceException ex) {
	    x = def;
	 }
	 if (x==null) {
	    return def;
	 } else {
	    return x;
	 }
      }
   }

   /*
     Going through System properties gives the user the change to customize
     the resources via -Dtoolbar.button.exit.ttt=Text; but this seems futile.
    */
   private final static Properties system_properties = System.getProperties ();

   private final static String getProperty (String key, String def) {
      return (system_properties.getProperty (key, getResource (key, def)));
   }

   public final static String toolbar_exit_label  = getProperty ("toolbar.button.exit", "Exit");
   public final static String toolbar_ping_label  = getProperty ("toolbar.button.ping", "Ping");
   public final static String toolbar_register_label  = getProperty ("toolbar.button.Register", "Register");
   public final static String toolbar_save_label  = getProperty ("toolbar.button.save", "Save");

   public final static String toolbar_exit_ttt = getProperty ("toolbar.button.exit.ttt", "Quit application");
   public final static String toolbar_ping_ttt = getProperty ("toolbar.button.ping.ttt", "Check on server");
   public final static String toolbar_register_ttt  = getProperty ("toolbar.button.Register", "Register");
   public final static String toolbar_save_ttt = getProperty ("toolbar.button.save.ttt", "Not needed");

}

