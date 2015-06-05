package submit.gui;

import submit.client.Defaults;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.*;

public class LogPane extends JTextPane {

   LogPane () {
      setEditable (false);
      setMinimumSize(new Dimension(500, 200));
   }

   //  Force pane to scroll to end of text

   /*
     "it turns out that it has to do with the newline characters in
     Windows. (I printed out the ASCII codes in the text, and there's
     a 10,13,13 instead of just 10...) It looks like
     getText().length() includes those characters in its count, but
     setCaretPosition() doesn't (which doesn't make any sense to me)."
   */

   public void toEnd () {
      try {
	 setCaretPosition (getDocument().getLength());
      } catch (IllegalArgumentException ex) {
	 ex.printStackTrace (System.err);
      }
   }

   private SimpleAttributeSet color (Color c) {
      final SimpleAttributeSet sas = new SimpleAttributeSet ();
      StyleConstants.setForeground (sas, c);
      return sas;
   }

   private SimpleAttributeSet def = color (Color.black);
   private SimpleAttributeSet red = color (Color.red);

   private SimpleAttributeSet bold () {
      final SimpleAttributeSet sas = new SimpleAttributeSet();
      StyleConstants.setForeground (sas, Color.black);
      //StyleConstants.setFontFamily(attribute, fontName);
      StyleConstants.setBold (sas, true);
      StyleConstants.setItalic (sas, false);
      StyleConstants.setUnderline (sas, false);

      final int size = StyleConstants.getFontSize (sas);
      StyleConstants.setFontSize (sas, size+2);
      return sas;
   }

   private SimpleAttributeSet bold = bold();

   public void appendln () {
      final Document doc = this.getDocument();
      try {
	 doc.insertString (doc.getLength(), "\n", def);
      } catch (BadLocationException ex) {
	 ex.printStackTrace (System.err);
      }
   }

   public void append (String s, SimpleAttributeSet sas) {
      final Document doc = this.getDocument();
      try {
	 doc.insertString (doc.getLength(), s, sas);
      } catch (BadLocationException ex) {
	 ex.printStackTrace (System.err);
      }
   }
   public void append (String s) { append (s, def); }
   public void appendln (String s) { append (s+"\n", def); }
   public void appendRed (String s) { append (s, red); }
   public void appendBold (String s) { append (s, bold); }
   public void appendAction (String s) { appendRed (String.format ("\n%s at %s\n", s, Defaults.date())); }
}

