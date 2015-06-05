package submit.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public abstract class ServerAction extends AbstractAction  {

   private final static Cursor WAIT_CURSOR    = Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR);
   private final static Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR);
   private       static Cursor previousCursor;

   protected ServerAction (String name, Icon icon) {
      super (name, icon);
   }

   protected static void setRootCursor (final JComponent component) {
      final RootPaneContainer root = (RootPaneContainer)component.getTopLevelAncestor();
      root.getGlassPane().getCursor();
      root.getGlassPane().setCursor(WAIT_CURSOR);
      root.getGlassPane().setVisible(true);
   }

   protected static void resetRootCursor (final JComponent component) {
      final RootPaneContainer root = (RootPaneContainer)component.getTopLevelAncestor();
      root.getGlassPane().setCursor(DEFAULT_CURSOR);
      root.getGlassPane().setVisible(false);
   }

}
