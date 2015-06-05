package submit.gui;

import submit.client.Parameters;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

public class SaveAction extends AbstractAction  {

   private final String fn = Parameters.currentParameters().getFile().getName();

   class SubmitFileFilter extends javax.swing.filechooser.FileFilter {
      public boolean accept (File f) {
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

   private final JFileChooser chooser = new JFileChooser();

   public SaveAction () {
      super ("Write", null);
      chooser.setDialogTitle ("Write Parameters to File");
      chooser.setApproveButtonToolTipText ("write parameters to selected file");
      chooser.setApproveButtonMnemonic ('w');
      chooser.setApproveButtonText ("Write");
      chooser.setMultiSelectionEnabled (false);
      chooser.setFileHidingEnabled (false);
      chooser.addChoosableFileFilter (new SubmitFileFilter());
      chooser.setSelectedFile (new File (fn));
   }

   public void actionPerformed (ActionEvent ev) {
      final LogPane log = Main.getLogPane (ev);
      final Main m = Main.get (ev);
      int option = chooser.showSaveDialog (m);
      if (option == JFileChooser.APPROVE_OPTION) {
	 final Parameters p = Parameters.currentParameters();
         final File file = chooser.getSelectedFile();
	 Parameters.currentParameters().write(file);
	 log.append ("\nParameters written to the file:\n  ");
	 log.appendBold (file.toString());
	 log.append ("\n");
	 /*
         try {
         } catch (IOException ex) {
         }
	 */
      } else {
         // Action canceled
	 log.append ("\nAction canceled; parameters not written.\n");
      }
         
   }
}
