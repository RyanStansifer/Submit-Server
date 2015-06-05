package submit.gui;

import submit.client.Parameters;
import submit.client.Ping;
import submit.gui.QuitAction;

import java.net.ConnectException;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

final class PropertiesTab extends JPanel {

   final JButton current = new JButton (new CurrentAction());
   final JButton read = new JButton (new OpenAction());
   final JButton write = new JButton (new SaveAction());

   final JTextField directory = new JTextField (40);
   final JTextField file      = new JTextField (40);

   final JCheckBox
      de = new JCheckBox(),
      dr = new JCheckBox(),
      fe = new JCheckBox(),
      fr = new JCheckBox();

   private void setAllFields () { setAllFields (Parameters.currentParameters()); }
   private void setAllFields (Parameters p) {
      final File d = p.getDirectory();
      final File f = p.getFile();
      try {
	 directory.setText (d.getCanonicalPath());
      } catch (IOException ex) {
	 ex.printStackTrace (System.err);
      }
      file.setText (f.getName());
      de.setSelected (d.exists() && d.isDirectory());
      dr.setSelected (d.canWrite());
      fe.setSelected (f.exists());
      fr.setSelected (f.canWrite());
   }

   PropertiesTab (final LogPane ta) {
      final Border b = BorderFactory.createEmptyBorder(5,5,5,5);
      setBorder (BorderFactory.createCompoundBorder (
         BorderFactory.createTitledBorder ("Save or Open Parameters File"),b));

      final JPanel bp = new JPanel ();  // panel of button(s)
      bp.add (current);
      current.setToolTipText ("show current parameters");
      bp.add (read);
      bp.add (write);
      write.setToolTipText ("save current parameters to a file");
      //bp.add (browse);
      bp.add (new JButton (new QuitAction()));

      de.setEnabled (false);  de.setHorizontalAlignment (SwingConstants.CENTER);
      dr.setEnabled (false);  dr.setHorizontalAlignment (SwingConstants.CENTER);
      fe.setEnabled (false);  fe.setHorizontalAlignment (SwingConstants.CENTER);
      fr.setEnabled (false);  fr.setHorizontalAlignment (SwingConstants.CENTER);

      setAllFields ();

      setLayout (new BorderLayout ());
      add (new Form (), BorderLayout.CENTER);
      add (bp, BorderLayout.SOUTH);

      /*
      final JTextArea x = new JTextArea ();
      x.setEditable (false);
      x.setLineWrap (true);
      x.setText (Parameters.currentParameters().toString());
      x.setWrapStyleWord (true);
      x.setBorder(BorderFactory.createEmptyBorder(2,3,2,3));
      */
      // add (x, BorderLayout.NORTH);

   }

   class Form extends JPanel {
      Form () {
	 setMinimumSize(new Dimension(400, 200));
	 setLayout (new GridBagLayout());

	 directory.setToolTipText ("Directory where parameters file is found");
	 file.setEditable (false);
	 file.setToolTipText ("File name of parameters file");

	 final GridBagConstraints gbc = new GridBagConstraints();
	 gbc.anchor = GridBagConstraints.WEST;
	 gbc.insets = new Insets (3,3,3,3);
         gbc.fill = GridBagConstraints.HORIZONTAL;  // ??

	 gbc.gridy = 0;
	 gbc.gridx = 0;
	 add (new JPanel());
	 gbc.gridx = 1;
	 add (new JPanel());
	 gbc.gridx = 2;
	 gbc.anchor = GridBagConstraints.CENTER;
	 add (new JLabel ("exists?"));
	 gbc.gridx = 3;
	 add (new JLabel ("write?"));


	 JLabel l = new JLabel ("Directory", JLabel.RIGHT);
	 l.setLabelFor (directory);

	 // lay out label & field & checkmarks
	 gbc.gridy = 1;
	 gbc.gridx = 0;
	 gbc.weightx = 5;
	 gbc.anchor = GridBagConstraints.EAST;
	 add(l, gbc);
	 gbc.gridx = 1;
	 gbc.weightx = 85;
	 gbc.anchor = GridBagConstraints.WEST;
	 add(directory, gbc);
	 gbc.gridx = 2;
	 gbc.weightx = 5;
	 gbc.anchor = GridBagConstraints.CENTER;
	 add (de, gbc);
	 gbc.gridx = 3;
	 gbc.weightx = 5;
	 add (dr, gbc);


	 gbc.weightx = 0;
	 l = new JLabel ("File Name", JLabel.RIGHT);
	 l.setLabelFor (file);
	 
	 // lay out label & field
	 gbc.gridy = 2;
	 gbc.gridx = 0;
	 gbc.anchor = GridBagConstraints.EAST;
	 add(l, gbc);
	 gbc.gridx = 1;
	 gbc.anchor = GridBagConstraints.WEST;
	 add(file, gbc);
	 gbc.gridx = 2;
	 gbc.anchor = GridBagConstraints.CENTER;
	 add (fe, gbc);
	 gbc.gridx = 3;
	 add (fr, gbc);

      }

   }
}
