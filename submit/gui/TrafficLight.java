package submit.gui;

import submit.client.Parameters;

import java.io.*;
import java.awt.*;
import javax.swing.*;


class TrafficLight extends JLabel {
   
   public TrafficLight (final String label) {
      super (label, null, /*makes no difference*/ SwingConstants.LEFT);
      setAlignmentX (1.0f);
      setHorizontalTextPosition (SwingConstants.LEFT);
   }
   
}
