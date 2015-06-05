package submit.gui;

import submit.client.Defaults;
import submit.client.Parameters;
import submit.client.Ping;

import submit.gui.Tabs;

import java.util.EventObject;
import java.text.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.BorderLayout;

import javax.swing.*;

public class Main extends JSplitPane {

   // Static access to widely-used operations

   private final static String name = "Main";

   public static Main get (Component source) {
      return (Main) SwingUtilities.getAncestorNamed (name, source);
   }
   public static Main get (EventObject e) {
      return get ((Component) e.getSource());
   }
   public static JFrame getFrame (EventObject e) {
      return (JFrame) SwingUtilities.getAncestorOfClass (JFrame.class, (Component) e.getSource());
   }

   public final static Status status = new Status();

   private final Tabs top;
   private final LogPane log = new LogPane ();

   public static Tabs getTabs (EventObject e) {
      return get(e).top;
   }
      
   public static LogPane getLogPane (EventObject e) {
      return get(e).log;
   }

   public static void appendResponse (EventObject e) {
      getLogPane(e).append (status.getResponse().toString());
   }

   static class StartPing extends Thread {
      public void run () {
	 status.setResponse (Ping.analyzedPing());
	 // if not defaults; ping again with defaults!?
      }
   }

   public Main () {
      super (JSplitPane.VERTICAL_SPLIT);
      setName (name);
      setOneTouchExpandable (true);

      new StartPing (). start();

      try {
         Thread.sleep (1000);
      } catch (Exception ex) {
      }

      // initialize "Tabs" after ping establishes state.
      top = new Tabs (log);
      setLeftComponent (top);
      final JScrollPane jsp = new JScrollPane (log);
      jsp.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      setRightComponent (jsp);

      log.append (Defaults.banner);
      if (Defaults.VERBOSE>1) {
         log.appendln ();
	 log.append ("Look-and-feel is " + UIManager.getLookAndFeel());
         log.appendln ();
	 log.append ("The locale is " + java.util.Locale.getDefault().getDisplayName() +
		     ", and the resource bundle is: " +
		     Resources.getResource("submit.resource.name") + ".\n");
      }

      log.appendln ();
      log.appendRed ("Start-up ping at " + Defaults.date() + "\n");
      if (status.getResponse()==null) {
	 log.appendln ("No response yet.");
      } else {
	 log.append (status.getResponse().toString());
      }
      // log.toEnd();  // does not always work?


      switch (status.getCurrentStatus()) {
      case -1: top.setTabToHelp(); break;
      case 0:  top.setTabToPing(); break;
      case 1:  top.setTabToRegister(); break;
      case 2:  top.setTabToHelp(); break;
      case 3:  top.setTabToSubmit(); break;
      default:  /*assert (false);*/
      }
   }


   public static void main (String[] args) {
      try {
	 if (System.getProperty ("os.name").startsWith ("Window")) {
	    final String nativeLF = UIManager.getSystemLookAndFeelClassName();
	    UIManager.setLookAndFeel (nativeLF);
	 } else {
	    final String javaLF = UIManager.getCrossPlatformLookAndFeelClassName();
	    UIManager.setLookAndFeel (javaLF);
	 }
      } catch (InstantiationException e) {
      } catch (ClassNotFoundException e) {
      } catch (UnsupportedLookAndFeelException e) {
      } catch (IllegalAccessException e) {
      }

      JFrame.setDefaultLookAndFeelDecorated (true);
      JDialog.setDefaultLookAndFeelDecorated (true);

      final JFrame frame = new JFrame (Resources.getResource("submit.name"));
      frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE); // Requires Java 1.3

      final Container c = frame.getContentPane();
      c.setLayout (new BorderLayout());
      final Main m = new Main();
      c.add (m, BorderLayout.CENTER);
      frame.pack();
      m.log.toEnd();
      frame.setVisible(true);
   }

}
