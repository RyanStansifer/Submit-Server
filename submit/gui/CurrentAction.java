package submit.gui;

import submit.client.Defaults;
import submit.client.Parameters;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Icon;
import javax.swing.AbstractAction;

public class CurrentAction  extends AbstractAction  {
   private final static Icon icon = null;
   private final static String tooltiptext = "show current user parameters";
   
   public CurrentAction () {
      super ("Current", icon);
      putValue (SHORT_DESCRIPTION, tooltiptext);
   }

   public void actionPerformed (ActionEvent ev) {
      final LogPane log = Main.getLogPane (ev);

      log.appendRed ("\nFile info as of " + Defaults.date() + "\n");
      log.append (Parameters.currentParameters().fileInfo());
      log.append ("\n");

      log.appendRed ("\nParameters as of " + Defaults.date() + "\n");
      log.append (Parameters.currentParameters().toString());
      log.append ("\n");

      log.toEnd();
   }

}
