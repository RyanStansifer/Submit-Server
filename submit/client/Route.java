package submit.client;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/*
  Control, Author, Class, Project
  Control, Author, Course,  Project
  Control, Author, Archive, Problem
  Control, Author, Contest, Problem
  =================================
  Control, Author, Context, Problem
 */

public class Route extends HashMap<String,String> {

   private static String squash (final String s) {
      return s.trim().toLowerCase().replaceAll ("\\s", "");
   }

   private void put (final String s, final HashMap<String,String> map) {
      if (map.containsKey (s)) {
         String v = map.get (s);
         put (s, squash (v));
      }
   }

   public Route (HashMap<String,String> map) {
      put ("control", map);
      put ("author", map);
      put ("archive", map);
      put ("contest", map);
      put ("course", map);
      put ("class", map);
      put ("project", map);
      put ("problem", map);
      put ("charset", map);
   } 

   public Route (String file_name) throws IOException {
      this (Detect.detect (file_name));
   }

   public Route (File file) throws IOException {
      this (Detect.detect (file));
   }

   public static void main (String[] args) throws IOException {
      for (int i=0; i<args.length; i++) {
	 System.out.println (args[i]+": "+new Route (args[i]));
      }
   } 
}

