package submit.gui;

import submit.gui.ServerAction;

import submit.client.Submit;
import submit.client.ResponseObject;
import submit.client.Defaults;
import submit.client.Parameters;

import java.awt.*;
import java.awt.event.*;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.AbstractAction;

import javax.swing.*;

public class SubmitServerAction extends ServerAction  {
   private final static String name = "Submit";
   private final static String tooltiptext = "transfer files to the submit server";
   private final static Icon icon = null;

   private final SubmitPanel sp;

   public SubmitServerAction (SubmitPanel sp) {
      super (name, icon);
      putValue (SHORT_DESCRIPTION, tooltiptext);
      this.sp=sp;
   }

   /*
   private static String makeQuestion (Parameters p, int n) {
      return
         "My name is " + p.getDisplayName() +
         ".\nThe class is '" + p.getParameter("class") + "'; the project is '"+p.getParameter("project")+
         "'.\nThe complete submission is "+n +" files.";
   }
   */

   public void actionPerformed (ActionEvent ae) {
      final JComponent source = (JComponent) ae.getSource();
      source.setEnabled (false);  // avoid concurrent actions
      setRootCursor (source);

      final LogPane log = Main.getLogPane (ae);
      final Parameters p = Parameters.currentParameters();

      final java.io.File[] files = sp.getFiles();

      // Make sure that the data actually being used is the data seen by user

      final String message = Submit.checkParameters (p);
      if (Main.status.getCurrentStatus()==0) {
	 log.appendRed ("\nServer is apparently not accessible at the moment.\n");
      } else if (message!=null) {
	 log.appendRed ("\n"+message);
      } else if (sp.getNumberOfFiles()<1) {
	 log.appendRed ("\nNo files to submit.\n");
      } else {
         /*
	 final String title = "Confirm Submission";
	 final String question = makeQuestion (p, sp.getNumberOfFiles());
	 final int n = JOptionPane.showConfirmDialog (null, question, title, JOptionPane.OK_CANCEL_OPTION);

	 if (n == JOptionPane.OK_OPTION) {
         */

         if (Defaults.VERBOSE>1) {
            final boolean peek = p.getPeek();
            final String control = p.getControl ();
            final String course = p.getCourse ();
            final String project = p.getProject ();
            log.append ("\nSubmitting ...\n");
            if (peek) {
               log.append ("Parameters may be overriden by values in submitted file or files.");
            } else {
               log.append ("The submitted files are not examined for parameter values.");
            }
            log.appendln ();
            final String m = String.format ("control='%s', course='%s', project='%s'\n", control, course, project);
            log.append (m);
         }

         log.appendRed ("\nSubmiting at " + Defaults.date() + "\n");
         try {
            final ResponseObject resp = Submit.analyzedSubmit (files, p);
            log.append (resp.toString());
            if (resp.getSuccess()) {
               // Remove all files from JList to prevent resubmission
               sp.removeAll();
            }
         } catch (Exception ex) {
            ex.printStackTrace();
            log.appendRed ("\nInternal error while submitting files\n");
         }
      }
      log.toEnd();
      resetRootCursor (source);
      source.setEnabled (true);
   }
}
