package submit.gui;

import java.util.EventListener;

public interface StatusChangeListener extends EventListener {
   public void statusChanged (StatusChangeEvent event);
}

