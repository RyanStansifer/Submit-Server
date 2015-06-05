package submit.shared;

public class BadFileException extends IllegalArgumentException {
   static final long serialVersionUID = -8329907228668875662L;
   public BadFileException (String x) {
      super (x);
   }
}
