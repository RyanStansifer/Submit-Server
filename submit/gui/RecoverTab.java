package submit.gui;

import submit.client.Parameters;
import submit.client.Mail;
import submit.client.ResponseObject;
import submit.gui.QuitAction;

import java.net.ConnectException;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

final class RecoverTab extends JPanel {

   private final static String text =
      "<html>Fill in your first name and last names exactly as when you registered.<br>The control code can be sent to the e-mail address already registered for you by pushing the &#147;mail&#148; button.</html>";

   private class InnerPanel extends GridPanel {
      InnerPanel () {
	 add (0,0, new JLabel ("First Name"), new ParameterTextField ("first_name"));
	 add (1,0, new JLabel ("Last Name"), new ParameterTextField ("last_name"));
      }
   }

   //final JButton recover = new JButton ("recover");
   final JButton mail = new JButton ("mail");


   RecoverTab (final LogPane ta) {
      final Border b = BorderFactory.createEmptyBorder(5,5,5,5);
      setBorder(b);

      final JPanel bp = new JPanel ();  // panel of button(s)
      //      bp.add (new JButton (new RecoverAction()));
      bp.add (new JButton (new MailServerAction()));
      bp.add (new JButton (new QuitAction()));

      setLayout (new BorderLayout ());
      //add (tf, BorderLayout.CENTER);
      add (new InnerPanel(), BorderLayout.CENTER);
      add (bp, BorderLayout.SOUTH);
      final JLabel l = new JLabel (text);
      add (l, BorderLayout.NORTH);
   }
}
