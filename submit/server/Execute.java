package submit.server;

import java.io.IOException;

public class Execute {

   public static void main (String[] args) {
      if (args.length>0) exec (args[0]);
   }

   public static int exec (String command) {
      int ret = -1000;
      try {
	 final Process p = Runtime.getRuntime().exec (command);
	 ret = p.waitFor();  // ret<0 means command failed
	 if (ret!=0 || SubmitServer.VERBOSE>3) {
	    System.out.print ("Executed system command " + command);
	    System.out.println ("; ret = "+ ret);
	 }
      } catch (IOException ex) {
         ex.printStackTrace (System.err);
      } catch (InterruptedException ex) {
         ex.printStackTrace (System.err);
      } catch (Exception ex) {
         ex.printStackTrace (System.err);
      }
      return ret;
      
   }

}
