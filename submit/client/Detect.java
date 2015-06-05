package submit.client;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 

import java.nio.CharBuffer; 
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.channels.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Detect {

   private final static int MAX = 1000;  // Consider up to these many bytes.

   private final static Charset CS = Charset.forName ("8859_1");

   /*
     Key/value pairs are separated by ":", terminated by ";" or by the end
     of the line, the key and value must each begin with a letter.
    */

   private final static int FLAGS = Pattern.CASE_INSENSITIVE;
   private final static Pattern PAIR_PAT =
      Pattern.compile ("\\W*(\\w+):[ \t]*(\\w[^;\n\r]*)(?:;.*)?", FLAGS);

   /*
     Consider the more liberal:
      Pattern.compile ("\\W*(\\w+)[ \t]*:[ \t]*(\\w[^;\n\r]*)(?:;.*)?", FLAGS);
     [Whitespace before colon ...]
   */

   /*
     Multiple values for same key, e.g., "author"?
     The *first* one encountered prevails!
   */

   public static HashMap<String,String> detect (Matcher m) {
      final HashMap<String,String> map = new HashMap<String,String> ();
      while (m.find()) {
	 final String key = m.group(1).toLowerCase();
	 if (!map.containsKey(key)) map.put (key, m.group(2).trim());
      }
      return map;
   }


   public static HashMap<String,String> detect (CharBuffer cb) {
      cb.rewind();
      return detect (PAIR_PAT.matcher (cb));
   }

   public static HashMap<String,String> detect (MappedByteBuffer bb) throws java.nio.charset.CharacterCodingException {
      return detect (CS.newDecoder().decode (bb));
   }

   public static HashMap<String,String> detect (FileChannel fc) throws IOException {
      final int prolog = Math.min (MAX, (int)fc.size());
      final MappedByteBuffer bb = fc.map (FileChannel.MapMode.READ_ONLY, 0, prolog);
      return detect (bb);
   }

   public static HashMap<String,String> detect (FileInputStream fis) throws IOException {
      final FileChannel fc = fis.getChannel(); 
      final HashMap<String,String> map = detect (fc);
      fc.close();
      fis.close();
      return (map);
   }

   public static HashMap<String,String> detect (String file_name) throws IOException {
      return detect (new FileInputStream (file_name));
   }

   public static HashMap<String,String> detect (final File f) throws IOException {
      return detect (new FileInputStream (f));
   }

   public static void main (String[] args) throws IOException {
      for (int i=0; i<args.length; i++) {
	 System.out.println (args[i]+": "+detect (args[i]));
      }
   } 
}

