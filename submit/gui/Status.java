package submit.gui;

import submit.client.ResponseObject;
import java.util.Set;
import javax.swing.event.*;

public class Status {

   private int currentStatus = -1;
   private ResponseObject lastResponse = null;

   public synchronized Set<String> getAvailableClasses () {
      if (lastResponse==null) {
	 return null;
      } else {
	 return lastResponse.getAvailableClasses();
      }
   }

   private void updateStatus () {
      final int newStatus;
      if (lastResponse==null) newStatus = -1;
      else if (lastResponse.r==null) newStatus = 0;      // not connected
      else if (false) newStatus = 1;
      else if (false) newStatus = 2;
      else newStatus = 3;
      if (newStatus != currentStatus) {
	 currentStatus = newStatus;
	 fireStatusChangeEvent (new StatusChangeEvent (newStatus));
      }
   }

   public synchronized void setResponse (ResponseObject r) {
      lastResponse = r;
      updateStatus ();
   }

   public synchronized ResponseObject getResponse () {
      return lastResponse;
   }

   public int getCurrentStatus () { return currentStatus; }

   private EventListenerList listenerList=new EventListenerList();

   public void addStatusChangeListener (StatusChangeListener listener) {
      listenerList.add (StatusChangeListener.class, listener);
   }

   public void removeStatusChangeListener (StatusChangeListener listener) {
      listenerList.remove (StatusChangeListener.class, listener);
   }

   // Notify all listeners that have registered interest

   private void fireStatusChangeEvent (StatusChangeEvent event) {
     final Object[] listeners = listenerList.getListenerList();
     // assert (listeners!=null);
     // Process the listeners last to first, notifying
     // those that are interested in this event
     for (int i=listeners.length-2; i>=0; i-=2) {
         if (listeners[i]==StatusChangeListener.class) {
             ((StatusChangeListener)listeners[i+1]).statusChanged(event);
         }
     }
   }

}
