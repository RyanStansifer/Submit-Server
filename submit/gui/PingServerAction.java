package submit.gui;

import submit.client.Ping;
import submit.client.Parameters;
import submit.client.ResponseObject;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.AbstractAction;

public class PingServerAction extends ServerAction  {
   private final static String name = "Ping";
   private final static String tooltiptext = "connect to the submit server";
   private final static Icon icon = null;

   public PingServerAction () {
      super (name, icon);
      putValue (javax.swing.Action.SHORT_DESCRIPTION, tooltiptext);
   }

   public void actionPerformed (ActionEvent ae) {
      final JComponent source = (JComponent) ae.getSource();
      source.setEnabled (false);  // avoid concurrent actions
      setRootCursor (source);

      final LogPane log = Main.getLogPane (ae);

      // Verify parameters:  "server", "port"
      final String message = Ping.checkParameters ();
      if (message!=null) {
	 log.appendRed ("\n"+message);
      } else {
	 log.appendAction ("Ping");
	 final ResponseObject resp = Ping.analyzedPing();
	 Main.status.setResponse (resp);
	 log.append (resp.toString());
      }
      log.toEnd();
      resetRootCursor (source);
      source.setEnabled (true);
   }
}
