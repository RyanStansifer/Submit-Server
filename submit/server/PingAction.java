package submit.server;

import submit.shared.Registration;
import submit.shared.Response;


final class PingAction {

   static void ping (final Parameters args, final Response resp) {
      final String ln = args.getProperty("last_name");
      final String fn = args.getProperty("first_name");
      final Registration r;
      if (args.getControl()==null) {
         if (ln!=null && fn!=null) {
            r = SubmitServer.db.isRegistered (fn, ln);
         } else {
            r = null;
         }
      } else {
         r = SubmitServer.db.getRegistration (args.getControl());
      }

      // If you guess someone's key, then you get their name.  You would anyway
      // if you submit something with their key.
      if (r!=null && r.isActivated()) {
         resp.registered = true;
         resp.add_line ("Hello, "+ r.fullName()+".");
      } else if (r!=null) {
         resp.registered = false;
         resp.add_line ("Hello, "+ r.fullName()+", registration not activated.");
      } else  {
         resp.registered = false;
      }

      resp.add_line (String.format ("The current date and time on the server is %s.", Connection.date()));
      resp.add_line (String.format ("Registration began on %s, currently %d registrations.", SubmitServer.db.date, SubmitServer.db.size()));
      resp.success = true;
   }

}
