package submit.gui;

import submit.client.ResponseObject;

public final class State {
   public static ResponseObject lastPing = null;

   public static void setResponse (ResponseObject r) {
      lastPing = r;
   }

   public static ResponseObject getResponse () {
      return lastPing;
   }

   public static boolean hasConnected () {
      return
	 lastPing!=null &&
	 lastPing.r!=null;
   }
   /*
   public static boolean isRegistered () {
      return
	 lastPing!=null &&
	 lastPing.r!=null &&
	 lastPing.r.registered;
   }

   public static boolean isAuthorized () {
      return
	 lastPing!=null &&
	 lastPing.r!=null &&
	 lastPing.r.authorized;
   }
   */
}
