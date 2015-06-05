package submit.email;

import java.io.*;

public final class Base64Buffer {

   public static final char BaseTable[] = { 
      'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
      'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
      'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
      'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'
   };

   public static char getByte (int i) { return BaseTable [i]; };

   public static void fill (char[] buffer, byte A, byte B, byte C) {
      assert buffer.length==4;
      buffer[0] = getByte ((( A & 0xFC) >> 2));
      buffer[1] = getByte ((((A & 0x03) << 4) | ((B & 0xF0) >> 4)));
      buffer[2] = getByte ((((B & 0x0F) << 2) | ((C & 0xC0) >> 6)));
      buffer[3] = getByte ((  C & 0x3F));
   }

   public static void fill (char[] buffer, byte A, byte B) {
      assert buffer.length==4;
      buffer[0] = getByte ((( A & 0xFC) >> 2));
      buffer[1] = getByte ((((A & 0x03) << 4) | ((B & 0xF0) >> 4)));
      buffer[2] = getByte ((((B & 0x0F) << 2)));
      buffer[3] = '=';
   }

   public static void fill (char[] buffer, byte A) {
      assert buffer.length==4;
      buffer[0] = getByte ((( A & 0xFC) >> 2));
      buffer[1] = getByte ((((A & 0x03) << 4)));
      buffer[2] = '=';
      buffer[3] = '=';
   }



   private final Writer out;

   public Base64Buffer () {
      this (System.out);
   }
   public Base64Buffer (PrintStream out) {
      this (new PrintWriter (out));
   }
   public Base64Buffer (Writer out) {
      this.out = out;
   }

   private final char[] chars = new char [4];
   private final byte[] bytes = new byte [3];
   private int rest=0;
   private int line=0;

   public void write (String file) throws IOException {
      final FileInputStream f = new FileInputStream (file);
      write (f);
      finish ();
      f.close();
   }

   public void write (InputStream in) throws IOException {
      for (;;) {
         final int b = in.read();
         if (b==-1) break;
         write ((byte)b);  // for b>=0 this gives 8 bits
      }
   }

   public void write (byte[] b) throws IOException {
      for (byte x: b) write (x);
   }

   public void write (byte b) throws IOException {
      bytes[rest++] = b;
      if (rest==3) {
         Base64Buffer.fill (chars, bytes[0], bytes[1], bytes[2]);
         out.write (chars);
         rest=0;
         line += 4;
         if (line>=76) {  // 4*19=76
            out.write ("\r\n");
            line = 0;
         }
      }
   }

   public void finish () throws IOException {
      assert 0<=rest && rest<3;
      if (rest==2) {
         Base64Buffer.fill (chars, bytes[0], bytes[1]);
      } else if (rest==1) {
         Base64Buffer.fill (chars, bytes[0]);
      }
      if (rest>0) out.write (chars);
      rest=0;
      out.write ("\r\n");
      line = 0;
      out.flush();
   }
   
   public static void main (String[] args) throws IOException {
      final Base64Buffer b = new Base64Buffer ();
      for (String fn: args) b.write (fn);
   }
}