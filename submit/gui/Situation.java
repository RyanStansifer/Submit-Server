package submit.gui;

import java.net.URL;
import java.awt.Image;
import javax.swing.*;
import javax.swing.border.*;

/*
  Alternatively could use CardLayout and have an informative JTextArea
  explain the current situtation in words.
*/

class Situation extends JPanel implements StatusChangeListener {

   private final static String situtationTitle = "Current Situation";

   private static ImageIcon loadSmall (String file, String description) {
      final java.net.URL url = TrafficLight.class.getResource(file);
      if (url==null) {
         System.err.format ("Missing image file \"%s\" for \"%s\".  Continuing ...%n", file, description);
         return new ImageIcon ();
      }
      assert url!=null;
      final ImageIcon icon  = new ImageIcon (url, description);
      assert icon!=null;
      final Image image = icon.getImage();
      // reduce by 50%
      icon.setImage (image.getScaledInstance(image.getWidth(null)/2, image.getHeight(null)/2, Image.SCALE_SMOOTH));
      return icon;
   }

   final static ImageIcon offImage = loadSmall ("/resources/lights/off.png", "off light");
   final static ImageIcon redImage = loadSmall ("/resources/lights/red.png", "red light");
   /*
   final static ImageIcon orangeImage = loadSmall ("/resources/lights/orange.png", "orange light");
   final static ImageIcon yellowImage = loadSmall ("/resources/lights/yellow.png", "yellow light");
   */
   final static ImageIcon greenImage  = loadSmall ("/resources/lights/green.png", "green light");

   /*
   final TrafficLight offlineLight = new TrafficLight ("off-line");
   final TrafficLight onlineLight = new TrafficLight ("on-line");
   final TrafficLight recognizedLight = new TrafficLight ("recognized");
   final TrafficLight authorizedLight = new TrafficLight ("authorized");
   */
   final TrafficLight connectedLight = new TrafficLight ("contacted");


   public void setUncertain () {
      connectedLight.setIcon (offImage);
   }

   public void setOffline () {
      /*
      offlineLight.setIcon (redImage);
      onlineLight.setIcon (offImage);
      recognizedLight.setIcon (offImage);
      authorizedLight.setIcon (offImage);
      */
      connectedLight.setIcon (redImage);
   }

   public void setOnline () {
      /*
      offlineLight.setIcon (offImage);
      onlineLight.setIcon (orangeImage);
      recognizedLight.setIcon (offImage);
      authorizedLight.setIcon (offImage);
      */
      connectedLight.setIcon (greenImage);
   }

   public void setRecognized () {
      /*
      offlineLight.setIcon (offImage);
      onlineLight.setIcon (yellowImage);
      recognizedLight.setIcon (yellowImage);
      authorizedLight.setIcon (offImage);
      */
   }
      
   public void setAuthorized () {
      /*
      offlineLight.setIcon (offImage);
      onlineLight.setIcon (greenImage);
      recognizedLight.setIcon (greenImage);
      authorizedLight.setIcon (greenImage);
      */
   }

   public void statusChanged (StatusChangeEvent event) { statusChanged (event.state); }
   public void statusChanged (int state) {
      switch (state) {
      case -1:  setUncertain();break;
      case 0: setOffline();    break;
      case 1: setOnline();     break;
      case 2: setRecognized(); break;
      case 3: setOnline();     break;
      default:  /*assert (false)*/;
      }
   }

   public Situation () {
      final Border b = BorderFactory.createEmptyBorder(5,5,5,5);
      setBorder(BorderFactory.createCompoundBorder (
          BorderFactory.createTitledBorder (situtationTitle),b));

      // Initial status
      statusChanged (Main.status.getCurrentStatus());

      setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
      add (Box.createVerticalGlue());
      add (connectedLight);
      add (Box.createVerticalGlue());
      /*
      add (Box.createVerticalGlue());
      add (offlineLight);
      add (Box.createVerticalGlue());
      add (onlineLight);
      add (Box.createVerticalGlue());
      add (recognizedLight);
      add (Box.createVerticalGlue());
      add (authorizedLight);
      add (Box.createVerticalGlue());
      */
   }
}


