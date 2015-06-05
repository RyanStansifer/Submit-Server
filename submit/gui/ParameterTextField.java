package submit.gui;

import submit.client.Defaults;
import submit.client.Parameters;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

public class ParameterTextField extends JTextField {
   //public class ParameterTextField extends JFormattedTextField {

   private final String parameterName;  // initialized after super constructor called!

   private void setParameter (String text) {
      final Parameters p = Parameters.currentParameters();
      try {
         p.setParameter (parameterName, text);
      } catch (IllegalArgumentException ex) {
         // Do nothing
      }
      setText (p.getParameter (parameterName));  // possible null
   }

   // Editability of the text field depends on the state of the connection with the server
   private void setEnabled (final int state) {
      setEnabled (state, Main.status.getCurrentStatus());
   }

   private void setEnabled (final int state, final int s) {
      setEnabled (s==state);
      final boolean b = isEnabled();
      //System.out.println (parameterName + ", type="+state+", current state=" + s + ", enabled=" + b);
   }

   private class CheckState implements StatusChangeListener {
      private final int state;
      CheckState (int state) { this.state=state; }
      public void statusChanged (StatusChangeEvent event) { setEnabled (state, event.state); }
   }

   private class TextAction implements ActionListener {
      public void actionPerformed (ActionEvent ae) {
         setParameter (getText().trim());
      }
   }

   /*  If we share documents, then multiple TextFields can access the
       same data.
   */
   private static Document last_nameDoc  = new PlainDocument ();
   private static Document first_nameDoc = new PlainDocument ();
   private static Document idDoc         = new PlainDocument ();
   private static Document controlDoc    = new PlainDocument ();
   private static Document emailDoc      = new PlainDocument ();   // ????
   /*
   private static Document codeDoc = new PlainDocument ();
   private static Document classDoc = new PlainDocument ();
   private static Document projectDoc = new PlainDocument ();
   private static Document currentDoc = new PlainDocument ();
   */

   private static Document documentFor (String name) {
      if (name.equals ("last_name")) return last_nameDoc;
      else if (name.equals ("first_name")) return first_nameDoc;
      else if (name.equals ("id")) return idDoc;
      else if (name.equals ("control")) return controlDoc;
      else return new PlainDocument ();
   }

   private static String toolTipText (String name) {
      if (name.equals ("last_name")) return "last name, eg, 'von Neuman'";
      else if (name.equals ("first_name")) return "first name, eg, 'János'";
      else if (name.equals ("course")) return "class name, eg, 'cse1234'";
      else if (name.equals ("control")) return "control code, eg, 'a1bcde23fg'";
      else if (name.equals ("email")) return "e-mail address, eg, 'smith@univ.edu'";
      else if (name.equals ("server")) return "name of the computer running the submit server";
      else if (name.equals ("port")) return "port on which the submit server is listening";
      else return name;
   }

   public ParameterTextField (String n) {
      parameterName=n;
      //setFocusLostBehavior (REVERT);// JFormattedTextField only
      setDocument (documentFor (n));
      if (Defaults.VERBOSE > 10) {
	 System.out.println (n+":  "+Parameters.currentParameters().getParameter(n));
      }
      setText (Parameters.currentParameters().getParameter(n));
      setColumns (25);
      addActionListener (new TextAction());
      setToolTipText (toolTipText (n));
      if (n.equals ("port") || n.equals ("server")) {
	 setEnabled (0);
	 Main.status.addStatusChangeListener (new CheckState(0));
         /*
      } else if (n.endsWith ("name") || n.equals ("email")) {
	 setEnabled (1);
	 Main.status.addStatusChangeListener (new CheckState(1));
      } else if (n.endsWith ("control")) {
	 setEnabled (2);
	 Main.status.addStatusChangeListener (new CheckState(2));
         */
      } else {
	 setEnabled (true);
      }
   }

   protected void processFocusEvent (FocusEvent event) {
      super.processFocusEvent (event);

      if (event.isTemporary()) return;

      if (event.getID()==FocusEvent.FOCUS_LOST) {
	 // Hope user is done editing
         setParameter (getText().trim());
            /*;
	 final String text = getText().trim();
	 final Parameters p = Parameters.currentParameters();
	 p.setParameter (parameterName, text);
            */
      }
   }

}
