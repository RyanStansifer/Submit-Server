package submit.gui;

import submit.client.Submit;
import submit.client.Parameters;
import submit.client.ResponseObject;
import submit.gui.QuitAction;

import java.net.ConnectException;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

final class SubmitTab extends JPanel {

   final JButton submit = new JButton ("submit");

   final SubmitPanel sp = new SubmitPanel ();

   SubmitTab (final LogPane ta) {
      setBorder (BorderFactory.createEmptyBorder(5,5,5,5));

      final JPanel bp = new JPanel ();  // panel of button(s)
      bp.add (new JButton (new SubmitServerAction(sp)));
      bp.add (new JButton (new QuitAction()));

      setLayout (new BorderLayout ());
      add (sp, BorderLayout.CENTER);
      add (bp, BorderLayout.SOUTH);
   }
}
