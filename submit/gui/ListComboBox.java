package submit.gui;

import javax.swing.*;

public class ListComboBox extends JComboBox<Object> {
   public ListComboBox () {
      setEditable (true);
   }

   public ListComboBox (Object [] x) {
      super (x);
      setEditable (true);
   }
   
}
