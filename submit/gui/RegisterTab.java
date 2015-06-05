package submit.gui;

import submit.client.Parameters;
import submit.client.Register;
import submit.client.ResponseObject;
import submit.gui.QuitAction;

import java.net.ConnectException;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

final class RegisterTab extends JPanel {


   private static final String info = 
      "<html>Do <font color=red>not</font> register more than once with the submit server.<br>"+
      "When you register, make sure the information is accurate;<br>it cannot be changed during the course of the year.<br>"+
      "A control code will be sent to your e-mail address.<br></html>\n";

   private static class InnerPanel extends GridPanel {
      InnerPanel () {
	 add (0,0, new JLabel ("First Name"), new ParameterTextField ("first_name"));
	 add (1,0, new JLabel ("Last Name"), new ParameterTextField ("last_name"));
	 add (3,0, new JLabel ("E-mail Address"), new ParameterTextField ("email"));
      }
   }
   final JButton register = new JButton ("register");

   private final Border b = BorderFactory.createEmptyBorder(5,5,5,5);

   RegisterTab (final LogPane ta) {
      setBorder(b);

      final JPanel bp = new JPanel ();  // panel of button(s)
      bp.add (new JButton (new RegisterServerAction ()));
      bp.add (new JButton (new QuitAction()));

      setLayout (new BorderLayout ());
      add (new InnerPanel (), BorderLayout.CENTER);
      add (bp, BorderLayout.SOUTH);
      final JLabel l = new JLabel (info);
      add (l, BorderLayout.NORTH);
   }
}
