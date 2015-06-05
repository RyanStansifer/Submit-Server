// under development
package submit.server;

import java.util.concurrent.*;

public class AsynchronousResponse {

   
   private static ExecutorService pool = Executors.newSingleThreadExecutor();

   public void shutdown () { pool.shutdown(); }

   public void submit (Runnable task) {
      final Future<?> future = pool.submit (task);
      // We do not need the future!
   }
   
}

