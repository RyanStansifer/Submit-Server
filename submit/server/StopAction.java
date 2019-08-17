package submit.server;

import submit.shared.Response;

@Deprecated
final class StopAction {

   static void stop (final Parameters args, final Response resp) {
      if (args.authenticateAdministrator()) {
	 SubmitServer.haltServer();
	 resp.add_line ("Kill command received");
      } else {
	 resp.add_line ("Not authorized");
      }
   }

}
