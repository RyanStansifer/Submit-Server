package submit.gui;

import submit.shared.Response;
import submit.client.Ping;

final class InitialStatus {

   public static final Object response;

   static {
      Object r = null;
      try {
	 r = Ping.ping ();
      } catch (Exception ex) {
	 r = ex;
      }
      response = r;

   }

   public static String analyzeResponse (Object response) {
      String message;
      if (response instanceof Exception) {
	 final Exception ex = (Exception) response;
	 message = "Unable to connect with the submit server.\n";
	 if (response instanceof ClassNotFoundException) {
	 } else {
	    message += ex.getMessage();
	 }   
      } else if (response instanceof Response) {
	 final Response r = (Response) response;
	 message = "Connected with submit server.\n";
	 
      } else {
	 message = "Internal error:  unexpected response.\n";
      }
      return message;
   }

   public static String getMessage () {
      return analyzeResponse (response);
   }

   public static boolean hasConnected () {
      return (response instanceof Response);
   }

   /* Don't use */
   private static boolean hasRegistered () {
	 return false;
   }
}
