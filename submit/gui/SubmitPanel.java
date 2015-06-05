package submit.gui;

import submit.client.Defaults;
import submit.client.Parameters;

import java.io.File;

import java.util.Set;

import javax.swing.*;

import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;

import java.awt.event.*;

public class SubmitPanel extends JPanel {

   final JLabel c    = new JLabel ("Course", JLabel.RIGHT);
   final JLabel proj = new JLabel ("Project", JLabel.RIGHT);
   final JLabel cont = new JLabel ("Control", JLabel.RIGHT);
   final JLabel dir  = new JLabel ("Directory", JLabel.RIGHT);
   final JLabel files = new JLabel ("Files", JLabel.RIGHT);

   final JTextField tfC = new ParameterTextField ("course");
   final JTextField tfP = new ParameterTextField ("project");
   final JTextField tfL = new ParameterTextField ("control");
   final ParameterTextField tfD = new ParameterTextField ("current");

   final DefaultListModel<File> model = new DefaultListModel<>();
   final JList<File> l = new JList<> (model);

   public int getNumberOfFiles () {  return model.size(); }
   public File[] getFiles ()          {
      Object[] o = model.toArray();
      File[] s = new File[o.length];
      System.arraycopy(o,0,s,0,s.length);
      return s;
   }
   public void removeAll () {
      model.clear();
   }

   void add (int row, int col, int anchor, int fill, Component c) {
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = col;
      gbc.gridy = row;
      gbc.weightx = (col==1?20:0);
      gbc.weighty = (row==3?30:0);
      gbc.anchor = anchor;
      gbc.fill = fill;
      gbc.insets = new Insets (3,3,3,3);
      add (c, gbc);
   }

   void add (int row, int col, int anchor, Component c) {
      add (row, col, anchor, GridBagConstraints.HORIZONTAL, c);
   }

   void add (int row, int col, Component c) {
      add (row, col, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, c);
   }

   final JComboBox<String> course_list;
      
   public SubmitPanel () {
      setLayout (new GridBagLayout());
      l.setToolTipText ("<html>Click the button \"<b>Add File</b>\",<br>then choose files to submit.</html>");
      l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      add (0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, c);
      final Set<String> classes = Main.status.getAvailableClasses();
      if (Defaults.VERBOSE>8) {
         System.out.println (classes);
      }
      if (classes!=null && classes.size()>0) {
         final String[] a = classes.toArray (new String[]{});
         java.util.Arrays.sort (a);
	 course_list = new JComboBox<String> (a);
	 course_list.setEditable (false);
      } else {
         course_list = new JComboBox<String> ();
	 course_list.setEditable (true);
      }
      add (0, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, course_list);
      c.setLabelFor (course_list);  // sets accessibleName

      final Parameters p = Parameters.currentParameters();
      final String cc = p.getParameter ("course");
      if (cc==null || cc.length()<1) {
         //	    System.out.println ("User has no value for 'class' parameter.");
         course_list.setSelectedItem (null);  // Make sure that no item is selected
      } else {
         // does this add 'cc'?  If not editable, then no.
         course_list.setSelectedItem (cc);
      }

      final String text = (String) course_list.getSelectedItem();
      if (text!=null) {
         // This destroys the user's value for "course";
         p.setParameter ("course", text);
      }

      // Change the course to the one selected by the user in the combo box
      class TextAction implements ActionListener {
         public void actionPerformed (ActionEvent ae) {
            final String text = (String) course_list.getSelectedItem();
            if (text!=null && text.trim().length()!=0) {
               final Parameters p = Parameters.currentParameters();
               p.setParameter ("course", text);  // could raise an exception
            }
         }
      }
      course_list.addActionListener (new TextAction());

      add (1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, proj);
      add (1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, tfP);
      proj.setLabelFor (tfP);  // sets accessibleName 

      add (2, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, cont);
      add (2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, tfL);
      proj.setLabelFor (tfL);  // sets accessibleName 

      add (3, 1, new JLabel ("The parameter values above can be overriden by those in the submitted files."));
      //add (3, 1, new JLabel ("File headers are ignored."));

      add (4, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, dir);
      add (4, 1, tfD);
      tfD.setEditable (false);

      add (5, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, files);
      add (5, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new JScrollPane (l));

      final JPanel jp = new JPanel();
      jp.setLayout (new GridBagLayout());
      final GridBagConstraints gbc2 = new GridBagConstraints();
      gbc2.fill = GridBagConstraints.HORIZONTAL;
      gbc2.gridy = 0;
      gbc2.gridx = 0;

      final JButton a = new JButton (new AddFileAction (model, tfD));
      jp.add (a,gbc2);
      gbc2.gridy = 1;
      gbc2.gridx = 0;
      jp.add (new JButton (new RemoveAction (l)),gbc2);
      add (5, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, jp);

      final JCheckBox box = new JCheckBox("text files",true);
      box.setEnabled (false);
      add (2, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, box);
   }
}
