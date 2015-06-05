package submit.gui;

import submit.client.Register;
import submit.client.ResponseObject;
import submit.client.Defaults;
import submit.client.Parameters;

import java.io.File;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.AbstractAction;

public class RegisterServerAction extends ServerAction  {
   private final static String name = "Register";
   private final static Icon icon = null;
   private final static String tooltiptext = "register with the submit server";

   public RegisterServerAction () {
      super (name, icon);
      putValue (SHORT_DESCRIPTION, tooltiptext);
   }

   public void actionPerformed (ActionEvent ae) {
      final JComponent source = (JComponent) ae.getSource();
      source.setEnabled (false);  // avoid concurrent actions
      setRootCursor (source);

      final Parameters p = Parameters.currentParameters();
      final LogPane log = Main.getLogPane (ae);
      ResponseObject resp = null;
      log.appendRed ("\nRegistering at " + Defaults.date() + "\n");

      // slow 
      resp = Register.analyzedRegister();
      Main.status.setResponse (resp);	 
      log.append (resp.toString());

      log.toEnd();
      resetRootCursor (source);
      source.setEnabled (true);

      // If all is well, save parameters?
   }
}

