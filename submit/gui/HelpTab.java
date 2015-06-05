package submit.gui;

import submit.client.Parameters;
import submit.client.Ping;
import submit.gui.QuitAction;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

final class HelpTab extends JPanel {

   private final static String helpTitle       = "Do you want to ...";

   private static class GotoTab extends AbstractAction {
      private final String label;
      public GotoTab (final String label) {
	 super (label, null);
	 this.label = label;
	 putValue (SHORT_DESCRIPTION, "goto '"+label.toLowerCase()+"' tab");
      }
      public void actionPerformed (ActionEvent ae) {
	 Main.getTabs (ae).setTabTo(label);
      }
   }

   private final JButton ping = new JButton (new GotoTab ("Ping"));
   private final JButton register = new JButton (new GotoTab ("Register"));
   private final JButton submit = new JButton (new GotoTab ("Submit"));
   private final JButton parameters = new JButton (new GotoTab ("Parameters"));
   private final JButton recover = new JButton (new GotoTab ("Recover"));
   private final JButton quit = new JButton (new QuitAction ());


   private class Questions extends JPanel {
      Questions () {
	 final Border b = BorderFactory.createEmptyBorder(5,5,5,5);
	 setBorder(BorderFactory.createCompoundBorder (BorderFactory.createTitledBorder (helpTitle),b));

	 setLayout (new GridBagLayout());
	 final GridBagConstraints gbc = new GridBagConstraints();
	 gbc.anchor = GridBagConstraints.WEST;
	 gbc.insets = new Insets (2,2,2,2);
	 gbc.fill = GridBagConstraints.HORIZONTAL;

	 // lay out label & button
	 gbc.gridy = 0;// first row
	 gbc.gridx = 0;
	 add (new JLabel ("test the connectivity to the submit server?",JLabel.RIGHT), gbc);
	 gbc.gridx = 1;
	 add (ping, gbc);
	 ping.setToolTipText ("goto 'ping' tab");

	 gbc.gridy = 1;// second row
	 gbc.gridx = 0;
	 add (new JLabel ("register with the submit server?",JLabel.RIGHT), gbc);
	 gbc.gridx = 1;
	 add (register, gbc);
	 register.setToolTipText ("goto 'register' tab");

	 gbc.gridy = 2;// third row
	 gbc.gridx = 0;
	 add (new JLabel ("transfer files to the submit server?",JLabel.RIGHT), gbc);
	 gbc.gridx = 1;
	 add (submit, gbc);
	 submit.setToolTipText ("goto 'submit' tab");

	 gbc.gridy = 3;// fourth row
	 gbc.gridx = 0;
	 add (new JLabel ("find, or write, the local parameters file?",JLabel.RIGHT), gbc);
	 gbc.gridx = 1;
	 add (parameters, gbc);
	 parameters.setToolTipText ("goto 'parameters' tab");

	 gbc.gridy = 4;// fifth row
	 gbc.gridx = 0;
	 add (new JLabel ("obtain a control code again?",JLabel.RIGHT), gbc);
	 gbc.gridx = 1;
	 add (recover, gbc);
	 recover.setToolTipText ("goto 'recover' tab");

	 gbc.gridy = 6;// sixth row
	 gbc.gridx = 0;
	 add (new JLabel ("quit the program?",JLabel.RIGHT), gbc);
	 gbc.gridx = 1;
	 add (quit, gbc);
      }
   }

   HelpTab () {
      setLayout (new BoxLayout (this, BoxLayout.X_AXIS));
      final Situation s = new Situation ();
      Main.status.addStatusChangeListener (s);
      add (s);
      add (new Questions());
   }

}
