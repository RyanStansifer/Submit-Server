package submit.gui;

import java.util.EventObject;

public class StatusChangeEvent extends EventObject {
   public final int state;
   public StatusChangeEvent (int s) {
      // source "Component" not important to us, but must not be null!
      super (new Object());  
      state = s;
   }
}
