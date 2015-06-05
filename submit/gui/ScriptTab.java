package submit.gui;

import submit.client.Parameters;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

final class ScriptTab extends JPanel {

   final JButton uscript = new JButton ("UNIX/sh");
   final JTextArea area = new JTextArea ();

   public ScriptTab () {
      final Border b = BorderFactory.createEmptyBorder(5,5,5,5);
      setBorder(BorderFactory.createCompoundBorder (
         BorderFactory.createTitledBorder("Command line script"),b));
      
      final JPanel bp = new JPanel ();  // panel of button(s)
      bp.add (uscript);

      class WriteUnixScript implements ActionListener  {
	 public void actionPerformed (ActionEvent ae) {
	    area.setText (WriteScripts.unixShScript(Parameters.currentParameters()));
	 }
      }
      uscript.addActionListener (new WriteUnixScript());
      setLayout (new BorderLayout ());
      add (new JScrollPane (area), BorderLayout.CENTER);
      add (bp, BorderLayout.SOUTH);
      
   }
}
