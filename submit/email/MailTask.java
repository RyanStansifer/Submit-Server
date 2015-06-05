package submit.email;

import submit.email.Base64Buffer;
import java.net.Socket;
import java.util.ArrayList;
import java.io.*;
import java.text.*;

public final class MailTask implements Runnable {

   public final static int SMPT_Port = 25;

   private final String host;
   private final int    port;
   private final String recipient;
   private final String headers;
   private final String body;

   private static final SimpleDateFormat sdf =
      new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss Z(z)");
   private final String date = sdf.format (new java.util.Date());

   private String makeSimpleHeaders
      (final String to, final String recipient, final String subject, final String from) {
      return String.format ("To:  %s <%s>\r\nSubject:  %s\r\nFrom:  %s\r\nDate: %s\r\nMIME-Version: 1.0", to, recipient, subject, from, date);
   }

   // This string must not appear in any of the parts
   // It must be unique if multipart messages are to be nested
   private final String boundary = "MIME multipart boundary";

   private final ArrayList<String> filenames = new ArrayList<String>();
   private final ArrayList<InputStream> contents = new ArrayList<InputStream>();
   private final ArrayList<String> contentType = new ArrayList<String>();

   private boolean hasAttachments () {
      assert filenames.size()==contents.size();
      assert contents.size()==contentType.size();
      return (filenames.size()>0 && contents.size()>0 && contentType.size()>0);
   }

   public void addAttachment (String f) {
      try {
         final InputStream is = new FileInputStream (f);
         final String y;
         if (f.endsWith (".gif")) y = "image/gif";
         else if (f.endsWith (".jpg")) y = "image/jpeg";
         else if (f.endsWith (".txt")) y = "text/plain";
         else y = "application/octet-stream";
         addAttachment (f, is, y);
      } catch (Exception ex) {
         ex.printStackTrace (System.err);
      }
   }

   public void addAttachment (String f, InputStream i, String y) {
      assert filenames.size()==contents.size();
      assert contents.size()==contentType.size();
      filenames.add (f);
      contents.add (i);
      contentType.add (y);
   }
         
      

   public MailTask (String host, String to, String recipient, String subject, String from, String body) {
      this (host, SMPT_Port, to, recipient, subject, from, body);
   }

   public MailTask (String host, int port, String to, String recipient, String subject, String from, String body) {
      this.host = host;
      this.port = port;
      this.recipient = recipient;
      this.headers = makeSimpleHeaders (to, recipient, subject, from);
      this.body = body;
   }

   private BufferedReader in;
   private BufferedWriter out;

   public void run () {
      try {
         final Socket s = new Socket (host, port);
         in = new BufferedReader (new InputStreamReader(s.getInputStream(), "US-ASCII"));
         out = new BufferedWriter (new OutputStreamWriter(s.getOutputStream(), "US-ASCII"));
         helo (host);
         final String user  = System.getProperty ("user.name");
         mail (user+"@"+host);
         rcpt (recipient);
         if (hasAttachments()) {
            multipartData ();
         } else {
            data ();
         }
         quit ();
         s.close();
      } catch (Exception ex) {
	 ex.printStackTrace (System.err);
      }
   }

   /*
     HELO <SP> <domain> <CRLF>
     In the HELO command the host sending the command identifies
     itself; the command may be interpreted as saying "Hello, I am
     <domain>".
   */
   public void helo (String host) throws IOException { send ("HELO "+ host); }
   /*
     MAIL <SP> FROM:<reverse-path> <CRLF>
     This command tells the SMTP-receiver that a new mail transaction
     is starting and to reset all its state tables and buffers,
     including any recipients or mail data.  It gives the
     reverse-path which can be used to report errors.  If
     accepted, the receiver-SMTP returns a 250 OK reply.
   */
   public void mail (String path) throws IOException { send ("MAIL FROM: <"+path+">"); }
   /*
     RCPT <SP> TO:<forward-path> <CRLF>
     This command is used to identify an individual recipient of
     the mail data; multiple recipients are specified by multiple
     use of this command.
   */
   public void rcpt (String recipient) throws IOException {send("RCPT TO: <"+recipient+">");}

   public void data () throws IOException {
      send ("DATA");
      out.write (headers);
      out.write (CRLF);
      out.write ("Content-Type: text/plain; charset=US-ASCII");
      out.write (CRLF);
      out.write (body);
      out.write (CRLF);
      send (".");
   }

   public void multipartData () throws IOException {
      send ("DATA");
      out.write (headers);
      out.write (CRLF);
      out.write (String.format ("Content-Type: multipart/mixed; boundary=\"%s\"", boundary));
      out.write (CRLF);
      out.write ("This is the preamble of a multiple part message in MIME format.\n");
      out.write ("This part should be ignored by MIME compliant mail readers.\n");
      out.write (String.format ("--%s", boundary));
      out.write (CRLF);
      out.write ("Content-Type: text/plain; charset=US-ASCII");
      out.write (CRLF);
      out.write (body);
      out.write (CRLF);
      for (int i=0; i<filenames.size(); i++) part (filenames.get(i), contents.get(i), contentType.get(i));
      out.write (String.format ("--%s--", boundary));  // Last boundary
      out.write (CRLF);
      send (".");
   }

   private void part (String fn, InputStream in, String y) throws IOException {
      out.write (String.format ("--%s", boundary));
      out.write (CRLF);
      out.write (String.format ("Content-Type: %s; name=%s", y, fn));
      out.write (CRLF);
      out.write ("Content-transfer-encoding: base64");
      out.write (CRLF);
      out.write (String.format ("Content-Dispostion: attachment; filename=%s", fn));
      out.write (CRLF);
      out.write (CRLF);
      final Base64Buffer bb = new Base64Buffer (out);
      bb.write (in);
      out.write (CRLF);
   }

   public void quit () throws IOException { send ("QUIT"); }
   public void send (final String s) throws IOException {
      // System.out.printf ("client: %s%n", s);
      out.write (s);
      out.write (CRLF);
      out.flush ();
      final String reply = in.readLine ();  // discard response
      if (reply.startsWith ("2")) {
         // ok
      } else if (reply.startsWith ("5")) {
         // trouble
         System.err.printf ("MailTask: 'send' received a negative reply '%s'.%n", reply);
         System.err.printf ("It is ignored and we hope for the best.%n");
      }
   }

   private static String CRLF = "\r\n";

   public static void main (String[] args) {
      // args[0] recipient
      // args[1] subject
      // args[2] body
      final MailTask mt = new MailTask ("mailhost.fit.edu", "you", args[0], args[1], "me", args[2]);
      for (int i=3; i<args.length; i++) mt.addAttachment (args[i]);
      new Thread (mt).start();
   }

}
