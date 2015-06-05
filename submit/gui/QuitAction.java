package submit.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Icon;
import javax.swing.AbstractAction;

public class QuitAction extends AbstractAction  {
   private final static Icon icon = null;
   private final static String tooltiptext = "exit the application";
   public QuitAction () {
      super ("Exit", icon);
      putValue (SHORT_DESCRIPTION, tooltiptext);
   }
   public void actionPerformed (ActionEvent ae) {
      System.exit(0);
      // Main.getFrame (ae).dispose();
   }
}
