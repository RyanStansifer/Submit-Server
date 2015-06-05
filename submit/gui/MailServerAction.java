package submit.gui;

import submit.gui.ServerAction;

import submit.client.Mail;
import submit.client.ResponseObject;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Icon;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class MailServerAction extends ServerAction  {
   private final static String name = "Mail";
   private final static String tooltiptext = "request control code to be sent by e-mail";
   private final static Icon icon = null;

   public MailServerAction () {
      super (name, icon);
      putValue (SHORT_DESCRIPTION, tooltiptext);
   }

   public void actionPerformed (ActionEvent ae) {
      final JComponent source = (JComponent) ae.getSource();
      source.setEnabled (false);  // avoid concurrent actions
      setRootCursor (source);

      final LogPane log = Main.getLogPane (ae);

      // Verify parameters:  "first_name", "last_name", "id"
      final String message = Mail.checkParameters ();
      if (message!=null) {
	 log.appendRed ("\n"+message);
      } else if (Main.status.getCurrentStatus()<2) {
	 log.appendRed ("\nIt is required to be in contact with the server\n");
	 log.appendRed ("and to have registration information confirmed before\n");
	 log.appendRed ("requesting password.\n");
      } else {
	 log.appendAction ("Ask for mail");
	 final ResponseObject resp = Mail.analyzedMail();
	 /*
	   Do not change status just because of a mail request.  A mail
	   request ought never to change the status.  But an unexpected
	   exception could be thrown changing the status and confusing
	   the user.
	 */
	 //  Main.status.setResponse (resp);
	 log.append (resp.toString());
      }
      log.toEnd();
      resetRootCursor (source);
      source.setEnabled (true);
   }
}
