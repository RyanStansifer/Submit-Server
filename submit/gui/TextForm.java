package submit.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Container;

import java.awt.event.*;

public class TextForm extends JPanel {

   public final JTextField[] tf;

   // Create a form with the given labels, tooltips, and sizes
   public TextForm (String[] labels, String[] tips, int[] widths) {

      tf = new JTextField[labels.length];

      setLayout (new GridBagLayout());
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.WEST;
      gbc.insets = new Insets (3,3,3,3);

      // Add labels and fields as specified
      for (int i=0; i<labels.length; i++) {
	 final JLabel l = new JLabel (labels[i]);

	 // Create an accessibility-friendly field
	 tf[i] = new JTextField (widths[i]);
	 tf[i].setToolTipText (tips[i]); // sets accessible desc too!
	 l.setLabelFor (tf[i]);          // sets accessibleName for tf[i]!

	 // lay out label & field
	 gbc.gridy = i;
	 gbc.gridx = 0;
	 add(l, gbc);
	 gbc.gridx = 1;
	 add(tf[i], gbc);
    }
  }

   // Get the contents of one of the TFs.
   public String getEnteredText(int index) {
      return tf[index].getText();
   }

   // Set the contents of one of the TFs.
   public void setEnteredText(int index, String text) {
      tf[index].setText(text);
   }

   // A simple example program
   public static void main(String[] args) {
      final String[] labels = { "First Name", "Middle Initial", "Last Name", "Age" };
      final String[] descs =  { "First Name", "Middle Initial", "Last Name", "Age" };

      final int[] widths = { 15, 1, 15, 3 };

      final TextForm form = new TextForm(labels, descs, widths);

      // A button that dumps the field contents
      final JButton dump = new JButton("Dump");
      class DumpListener implements ActionListener {
	 public void actionPerformed(ActionEvent ev) {
	    System.out.println(form.getEnteredText(0));
	    System.out.println(form.getEnteredText(1));
	    System.out.println(form.getEnteredText(2));
	    System.out.println(form.getEnteredText(3));
	 }
      }
      dump.addActionListener (new DumpListener());

      final JFrame frame = new JFrame("Text Form");
      frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE); // Requires Java 1.3
      final Container c = frame.getContentPane();
      c.setLayout (new BorderLayout());
      c.add(form, BorderLayout.CENTER);
      c.add(dump, BorderLayout.SOUTH);
      frame.pack();
      frame.setVisible(true);
  }
}
