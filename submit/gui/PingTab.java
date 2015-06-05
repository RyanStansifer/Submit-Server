package submit.gui;

import submit.client.Parameters;
import submit.client.Ping;
import submit.gui.QuitAction;

import java.net.ConnectException;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.BorderFactory;

final class PingTab extends JPanel {

   /*
   final String[] labels = { "Server Name", "Port Number" };
   final String[] descrs = { "Server name", "Port number" };
   final int[]    widths = { 15, 15 };

   final TextForm tf = new TextForm (labels, descrs, widths);
   */

   private class InnerPanel extends GridPanel {
      InnerPanel () {
	 add (0,0, new JLabel ("Server Name"), new ParameterTextField ("server"));
	 add (1,0, new JLabel ("Port Number"), new ParameterTextField ("port"));
      }
   }


   private final Border b = BorderFactory.createEmptyBorder(8,8,8,8);

   /*
   public void updateState () {
      // editable if we have not connected
      final boolean e1 = Main.status.getCurrentStatus()==0;
      tf.tf[0].setEditable (e1);
      tf.tf[1].setEditable (e1);
   }
   */

   PingTab (final LogPane ta) {
      final Border b = BorderFactory.createEmptyBorder(8,8,8,8);
      setBorder (BorderFactory.createCompoundBorder (
         BorderFactory.createTitledBorder ("Connectivity to the submit server"),b));


      final JPanel bp = new JPanel ();  // panel of button(s)
      bp.add (new JButton (new PingServerAction()));
      bp.add (new JButton (new QuitAction()));

      setLayout (new BorderLayout ());
      add (new InnerPanel (), BorderLayout.CENTER);
      add (bp, BorderLayout.SOUTH);

   }
}
