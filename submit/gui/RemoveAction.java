package submit.gui;

import submit.client.Parameters;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;


/*
  Control - click deselects item in JList
*/

public class RemoveAction extends AbstractAction  {

   private final JList jl;

   /*
   private void setEnabled (DefaultListModel model) { setEnabled (model.getSize()>0);  }
   private void setEnabled (JList l) { setEnabled ((DefaultListModel)(l.getModel()));  }
   */

   class SL implements ListSelectionListener {
      public void valueChanged (ListSelectionEvent event) {
	 setEnabled (jl.getSelectedIndex()!=-1);
      }
   }

   public RemoveAction (JList jl) {
      super ("Remove", null);
      this.jl = jl;
      jl.addListSelectionListener (new SL());
      setEnabled (jl.getSelectedIndex()!=-1);
   }

   public void actionPerformed (ActionEvent ev) {
      final DefaultListModel model = (DefaultListModel) jl.getModel();
      final int [] selected = jl.getSelectedIndices();
      for (int j=0; j<selected.length; j++)  model.remove (selected[j]);
      setEnabled (jl.getSelectedIndex()!=-1);
   }
}
