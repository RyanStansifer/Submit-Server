package submit.eval;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.*;
import java.io.File;

public class Names {

   static class Entry {
      final Pattern parent;
      final Pattern file;
      final int lb, ub;
      final String [] types;
      Entry (String p, String f, int lb, int ub, String x) {
         parent = Pattern.compile(p);
         file   = Pattern.compile(f);
         this.lb = lb;
         this.ub = ub;
         types = x.split("\\s*,\\s*");
      }
      public String toString () {
         return String.format ("%s %s %d %d %s", parent, file, lb, ub, Arrays.toString(types));
      }
      
   }
   
   static String filename = "EXPECTING.txt";

   public static void main (String[] args) throws java.io.FileNotFoundException {
      Scanner scan = new Scanner (new File (filename));
      while (scan.hasNext()) {
         Entry e = new Entry (scan.next(),scan.next(),scan.nextInt(),scan.nextInt(),scan.nextLine().trim());
         System.out.println (e);
      }
   }
}
