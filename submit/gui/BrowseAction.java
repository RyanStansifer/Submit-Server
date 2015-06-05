package submit.gui;

import submit.client.Parameters;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;


public class BrowseAction extends AbstractAction  {

   private final String fn = Parameters.currentParameters().getFile().getName();

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

   private final JFileChooser saveChooser = new JFileChooser();
   private final JFileChooser openChooser = new JFileChooser();


   public BrowseAction () {
      super ("Browse", null);
      saveChooser.setMultiSelectionEnabled (false);
      saveChooser.setFileHidingEnabled(false);
      saveChooser.addChoosableFileFilter(new SubmitFileFilter());
      saveChooser.setSelectedFile(new File (fn));

      openChooser.setMultiSelectionEnabled (false);
      openChooser.setFileHidingEnabled(false);
      openChooser.addChoosableFileFilter(new SubmitFileFilter());
      openChooser.setSelectedFile(new File (fn));

   }


   public void actionPerformed (ActionEvent ev) {
      /*
      final Main m = Main.get (ev);
      int option = chooser.showDialog (m, "Browse");
      if (option == JFileChooser.APPROVE_OPTION) {
         final File file = chooser.getSelectedFile();
         try {
         } catch (IOException ex) {
         }
      } else {
         // Action canceled
      }
      */
         
   }


}
