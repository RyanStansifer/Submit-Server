package submit.gui;

import submit.client.Parameters;
import submit.shared.FileInfo;

import java.io.File;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class AddFileAction extends AbstractAction  {

   // kludge snarfed from 'net
   public void disableNewButtonFor (JFileChooser choose) {
      final Component[] arr1=choose.getComponents();
      for (int i = 0; i < arr1.length; i++) {
	 if (! (arr1[i] instanceof JPanel)) continue;
	 final Component[] arr2=((JPanel)arr1[i]).getComponents();
	 for (int j = 0; j < arr2.length; j++) {
	    if (! (arr2[j] instanceof JPanel)) continue;
	    final Component[] arr3=((JPanel)arr2[j]).getComponents();
	    for (int k = 0; k < arr3.length; k++) {
	       if (! (arr3[k] instanceof JButton)) continue;
	       // System.out.println (((JButton)arr3[k]).getToolTipText());
	       if(((JButton)arr3[k]).getToolTipText().equalsIgnoreCase("Create New Folder")) {
		  ((JButton)arr3[k]).setEnabled(false);
		  return;
	       }
	    }
	 }
      }
   }

   static class JavaFileFilter extends FileFilter {
      public boolean accept (File f) {
	 if (f.isDirectory()) {  // allow user to navigate directories
	    return true;
	 } else {
	    return (f.getName().endsWith (".java"));
	 }
      }
      public String getDescription () {
	 return "Java source files \"*.java\"";
      }
   }

   static class CppFileFilter extends FileFilter {
      public boolean accept (File f) {
	 if (f.isDirectory()) {  // allow user to navigate directories
	    return true;
	 } else {
	    return (f.getName().endsWith (".cpp"));
	 }
      }
      public String getDescription () {
	 return "C++ source files \"*.cpp\"";
      }
   }

   private final JFileChooser chooser = new JFileChooser();
   private final DefaultListModel<File> model;
   private final ParameterTextField current;

   private final static String name = "Add File";
   private final static String tooltiptext = "browse to find a file to add";
   private final static Icon icon = null;

   public AddFileAction (DefaultListModel<File> m, ParameterTextField c) {
      super (name, icon);
      putValue (SHORT_DESCRIPTION, tooltiptext);

      this.model = m;
      this.current = c;

      chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);  // FILES_ONLY is the default
      chooser.setMultiSelectionEnabled (true);
      chooser.setFileHidingEnabled(true);
      chooser.setAcceptAllFileFilterUsed(true);
      chooser.addChoosableFileFilter(new JavaFileFilter());
      chooser.addChoosableFileFilter(new CppFileFilter());
      chooser.setFileFilter (chooser.getAcceptAllFileFilter());// Make all files the default
      disableNewButtonFor (chooser);
   }

   public void actionPerformed (ActionEvent ev) {
      final LogPane log = Main.getLogPane (ev);
      final String cwd = Parameters.currentParameters().getParameter("current");
      if (cwd!=null && cwd.length()>0) {
	 chooser.setCurrentDirectory (new File(cwd));
      }

      final Main m = Main.get (ev);
      final int option = chooser.showOpenDialog (m);
      if (option == JFileChooser.APPROVE_OPTION) {
         final File[] files = chooser.getSelectedFiles();
	 for (int i=0; i<files.length; i++) {
	    if (model.contains (files[i])) {
	       // duplicate
	       log.appendRed ("Duplicate file name: " + files[i].toString() + "\n");
	    } else if (model.getSize()>25) {
	       log.appendRed ("Too many files!\n");
	       break;
	    } else if (!files[i].canRead()) {
	       log.appendRed ("Can't read file: " + files[i].toString() + "\n");
	    } else if (!FileInfo.checkFileName (files[i].getName())) {
	       log.appendRed ("File name: '" + files[i].toString() + "' contains illegal characters\n");
	    } else if (files[i].length()>1000000) {
	       log.appendRed ("File too big: " + files[i].toString() + "\n");
	    } else {
	       // OK, add to the list to transfer
	       model.addElement (files[i]);
	    }
	 }
	 if (files.length>0) {
	    final String parent = files[0].getParentFile().getAbsolutePath();
	    current.setText (parent);   	    // keep text field upto date
	    Parameters.currentParameters().setParameter("current", parent);
	    // now it would have to be written back to the the parameters file!
	 }
	 
      } else {
         // Action canceled
	 // log.append ("Add file action canceled\n");
      }
      log.toEnd();
   }
}
