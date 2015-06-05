package submit.gui;

import submit.client.Ping;
import submit.client.Parameters;
import submit.client.ResponseObject;

import java.util.Set;

import java.io.File;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class RecoverAction extends ServerAction  {
   private final static String name = "Recover";
   private final static String tooltiptext = "write user parameters to a file";
   private final static Icon   icon = null;

   public RecoverAction () {
      super (name, icon);
      putValue (javax.swing.Action.SHORT_DESCRIPTION, tooltiptext);
   }

   final JTextArea dialogMessage = new JTextArea (5,40);

   public void actionPerformed (ActionEvent ae) {
      final JComponent source = (JComponent) ae.getSource();
      //source.setEnabled (false);  // avoid concurrent actions
      setEnabled (false);  // avoid concurrent actions
      setRootCursor (source);
      /*
      final JFrame frame = Main.getFrame (ae);
      frame.setCursor (Cursor.WAIT_CURSOR);
      */
      final LogPane log = Main.getLogPane (ae);

      final Parameters p = Parameters.currentParameters();

      // Verify parameters:  "first_name", "last_name", "id"
      String message = "";
      if (p.missing ("first_name")) message += "Missing first name\n";
      if (p.missing ("last_name" )) message += "Missing last name\n";

      if (message.length()>0) {
	 log.appendRed ("\nThere is not much point in writing user parameters when important parameters are lacking.\n");
	 log.appendRed (message);
      } else if (Main.status.getCurrentStatus()==0) {
	 log.appendRed ("\nServer is not accessible.\n");
      } else if (Main.status.getCurrentStatus()==3) {
	 log.appendRed ("\nThe old parameters are correct.  Why risk writing incorrect ones?\n");
      } else {

	 // Prompt for missing class name, if necessary
	 final String cc = p.getParameter ("class");
	 final Set<String> classes = Main.status.getAvailableClasses();
	 if (cc==null && classes!=null) {
	    final String s = (String) JOptionPane.showInputDialog(
               (Component)ae.getSource(),
               "Would you like to provide a default class name?",
	       "Choose Default Class",
	       JOptionPane.QUESTION_MESSAGE,
               null, classes.toArray(new String[]{}), null);

	    if (s!=null) {
	       p.setParameter("class",s);  // FIX!  Need to notify textfield/combox box
	    }
	 }

	 final File f = p.getFile();
	 dialogMessage.setText ("Do you want to write these parameters:\n"+
				p.toString()+ "\nto this file:\n"+ f.toString()+ "\n");
				      
	 final int n = JOptionPane.showConfirmDialog (
            (Component)ae.getSource(),
	    new JScrollPane(dialogMessage),
	    "Write parameters?", JOptionPane.OK_CANCEL_OPTION);

	 if (n == JOptionPane.OK_OPTION) {
	    p.write ();
	    log.append ("\nParameters written to the file:\n  ");
	    log.appendBold (f.toString());
	    log.append ("\n");
	 } else {
	    // Action canceled
	    log.append ("\nAction canceled; parameters not written.\n");
	 }

      }
      log.toEnd();

      log.appendAction ("Ping");
      final ResponseObject resp = Ping.analyzedPing();
      Main.status.setResponse (resp);
      log.append (resp.toString());
      log.toEnd();

      resetRootCursor (source);
      setEnabled (true);
   }
}
