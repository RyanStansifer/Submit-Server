package submit.shared;

import java.io.Serializable;
import java.util.Set;
import java.util.Map;

final public class Response implements Serializable {

   static final long serialVersionUID = -3172040117083824413L;

   public boolean success = false;
   public boolean registered = false;
   public boolean authorized = false;

   public Map<String,Set<String>> projects = null;
   public boolean projectsAvailable () { return projects!=null; }
   public Set<String> getAvailableClasses ()  { return projects.keySet(); }
   public Set<String> getAvailableProjects (final String key) {
      return projects.get(key);
   }

   private String message = null;
   public void add_line (String s) {
      if (message==null) {
	 message = s + '\n';
      } else {
	 message += (s+'\n');
      }
   }
   public void failBecause (String line) {
      success=false;
      add_line (line);
   }
   public String   toString ()             { return message; }

}
