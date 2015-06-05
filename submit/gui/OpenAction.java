package submit.gui;

import submit.gui.ServerAction;

import submit.client.Ping;
import submit.client.Defaults;
import submit.client.Parameters;
import submit.client.ResponseObject;

import java.io.File;
import java.io.IOException;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class OpenAction extends ServerAction  {
   private final static String name = "Open";
   private final static String tooltiptext = "read a parameters from a file";
   private final static Icon   icon = null;

   // Does this get called again with parameters change?!
   private final String fn = Parameters.currentParameters().getFile().getName();

   private final JFileChooser chooser = new JFileChooser();

   public OpenAction () {
      super (name, icon);
      putValue (javax.swing.Action.SHORT_DESCRIPTION, tooltiptext);

      chooser.setMultiSelectionEnabled (false);
      chooser.setFileHidingEnabled(false);
      chooser.addChoosableFileFilter(new SubmitFileFilter());
      chooser.setSelectedFile(new File (fn));
   }

   class SubmitFileFilter extends javax.swing.filechooser.FileFilter {
      public boolean accept(File f) {
	 if (f.isDirectory()) {  // allow user to navigate directories
	    return true;
	 } else {
	    return (f.getName().equals (fn));
	 }
      }
      public String getDescription () {
	 return "submit files named '"+ fn + "'" ;
      }
   }

   public void actionPerformed (ActionEvent ae) {
      final JComponent source = (JComponent) ae.getSource();
      setEnabled (false);
      setRootCursor (source);

      final Main m = Main.get (ae);
      final LogPane log = Main.getLogPane (ae);

      int option = chooser.showOpenDialog (m);
      if (option == JFileChooser.APPROVE_OPTION) {
         final File file = chooser.getSelectedFile();

         try {
	    Parameters.rereadParameters (file);
	    log.appendRed ("\nParameters as of " + Defaults.date() + "\n");
	    log.append (Parameters.currentParameters().toString());
	    log.append ("\n");
	    log.toEnd();

	    chooser.setSelectedFile (file); // does this happen automatically?

	    /* Now we need to ping to update the status! */

	    log.appendAction ("Ping");
	    final ResponseObject resp = Ping.analyzedPing();
	    Main.status.setResponse (resp);
	    log.append (resp.toString());
	    log.toEnd();

	    // Did the status improve?

         } catch (IOException ex) {
	    log.appendRed ("\nTrouble reading parameters\n");
         }

      } else {
         // Action canceled
	 log.append ("\nAction canceled");
      }

      resetRootCursor (source);
      setEnabled (true);
   }
}
