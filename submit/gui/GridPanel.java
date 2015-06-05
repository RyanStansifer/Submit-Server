package submit.gui;

import java.awt.*;
import javax.swing.*;


public class GridPanel extends JPanel {

   void add (int row, int col, int anchor, int fill, Component c) {
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = col;
      gbc.gridy = row;
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

   void add (int row, int col, JLabel l, Component c) {
      add (row, col, GridBagConstraints.EAST, GridBagConstraints.NONE, l);
      add (row, col+1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, c);
      l.setLabelFor (c);
   }

   public GridPanel () {
      setLayout (new GridBagLayout());
   }
}
