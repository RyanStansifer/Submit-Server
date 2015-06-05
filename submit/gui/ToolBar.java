package submit.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;


public final class ToolBar extends JToolBar {

   private void add (Action a, String tip, char mn) {
      final JButton button = new JButton (a);
      button.setToolTipText (tip);
      button.setMnemonic (mn);
      add (button);
   }

   /*
     Button must be visible for mnemonic to work.
   */

   private void add (String label, String tip, char mn) {
      final JButton button = new JButton (label);
      button.setToolTipText (tip);
      button.setMnemonic (mn);
      button.setMargin (new Insets(3,0,3,0)); // top, left, bottom, right
      add (button);

   }

   /*
   private final JButton exit = new JButton (Resources.toolbar_exit_label);
   private final JButton ping = new JButton (Resources.toolbar_ping_label);
   private final JButton register = new JButton (Resources.toolbar_register_label);
   private final JButton save = new JButton (Resources.toolbar_save_label);
   */

   public ToolBar () {
      setName ("ToolBar");

      add (Resources.toolbar_exit_label, Resources.toolbar_exit_ttt, 'x');
      add (Resources.toolbar_ping_label, Resources.toolbar_ping_ttt, 'p');
      add (Resources.toolbar_register_label, Resources.toolbar_register_ttt, 'r');
      add (Resources.toolbar_save_label, Resources.toolbar_save_ttt, 'p');

      putClientProperty ("JToolBar.isRollover", Boolean.TRUE);
   }


}
