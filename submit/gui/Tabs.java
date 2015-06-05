package submit.gui;

import submit.client.Defaults;
import submit.client.Parameters;

import java.awt.Container;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

class Tabs extends JTabbedPane  {

   private void setTabTo (int i)       { setSelectedIndex (i);   }
   public  void setTabTo (String label) { setTabTo (indexOfTab (label)); }

   public void setTabToPing()      { setTabTo ("Ping");   }
   public void setTabToRegister()  { setTabTo ("Register"); }
   public void setTabToSubmit()    { setTabTo ("Submit"); }
   public void setTabToAdvanced()  { setTabTo ("Advanced"); }
   public void setTabToParameters(){ setTabTo ("Parameters"); }
   public void setTabToRecover()    { setTabTo ("Recover"); }
   public void setTabToHelp()       { setTabTo ("Help"); }

   private final PingTab pingTab;
   private final RegisterTab registerTab;
   private final SubmitTab submitTab;
   //   private final JPanel advancedTab;

   private final boolean admin = false;

   private static JPanel create (String b, String [] l, String [] d, int [] w) {
      final JPanel bp = new JPanel ();
      bp.add (new JButton (b));
      final JPanel j = new JPanel ();
      j.setLayout (new BorderLayout ());
      j.add (new TextForm (l, d, w), BorderLayout.CENTER);
      j.add (bp, BorderLayout.SOUTH);
      return j;
   }

   Tabs (final LogPane ta) {
      this.addTab ("Ping", null, pingTab=new PingTab (ta), "Contact the remote server");
      this.addTab ("Register", null, registerTab=new RegisterTab (ta), "Provide identifying information");
      this.addTab ("Submit", null, submitTab=new SubmitTab (ta), "Transfer files");
      // this.addTab ("Advanced", null, advancedTab=new JPanel (), "Transfer files");
      this.addTab ("Recover", null, new RecoverTab (ta), "Obtain control code");
      this.addTab ("Parameters", null, new PropertiesTab (ta), "Locate parameters file");
      // this.addTab ("Script", null, new ScriptTab (), "Create a submit script");
      this.addTab ("Help", null, new HelpTab (), "Help using this application");

      // if (admin) this.addTab ("Admin", null, new JPanel (), "Admin utilities");

      final JTextArea about = new JTextArea ();
      final Border b = BorderFactory.createEmptyBorder(5,5,5,5);
      about.setBorder(BorderFactory.createCompoundBorder (
         BorderFactory.createTitledBorder("About this application"),b));

      about.append ("Submit Client, "+Defaults.VERSION+", by Ryan Stansifer.\n");
      about.append ("The purpose of this application is to transfer files to the submit server.\n");
      about.append ("Generally, this is required for student assignments in certain classes.\n");
      about.append ("Any files may be transfered.  Files may be submitted from any computer (running Java)\n");
      about.append ("as long as the submit server can be reached over the Internet.\n");
      this.addTab ("About", null, about, "Information about this application");

      // setSelectedIndex (3);
   }

   private void updateState () {
   }
}
