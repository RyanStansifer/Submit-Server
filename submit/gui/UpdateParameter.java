package submit.gui;

import submit.client.Parameters;

import javax.swing.JTextField;
import java.awt.event.*;

class UpdateParameter implements ActionListener  {
   final private String name;
   public UpdateParameter (String name) { this.name = name; }
   public void actionPerformed (ActionEvent ae) {
      final JTextField source = (JTextField) ae.getSource ();
      final String text = source.getText();
      System.out.println ("UpdateParameter.actionPerformed()");
      System.out.println ("commit text:" + text);
      final Parameters p = Parameters.currentParameters();
      p.setParameter (name, text);

      //source.setText(p.getParameter(name));
   }
}
