package submit.email;

import java.net.*;
import java.io.*;

public class Mail {

   final static int SMPT_Port = 25;
   
   public static void main (String args[]) {
      final String recipient = args[0];
      final String host = args.length>1?args[1]:"mailhost";
      final String from = args.length>2?args[2]:"Elvis Presly <Elvis.Presley@jailhouse.rock>";
      try {
	 final Mail em = new Mail (new Socket (host, SMPT_Port));
	 final String headers = "To: you\nSubject:  In the ghetto\n" + from;
	 final String body = "I'm alive. Help me!";
	 em.sendMail (recipient, host, headers, body);
	 em.close ();
      } catch (IOException e) {
	 e.printStackTrace();
      }
   }

   public static void main (String host, String recipient, String to, String subject, String from, String body) throws IOException {
      final Mail em = new Mail (new Socket (host, SMPT_Port));
      final String headers = "To:  " + to + " <" + recipient + ">\nSubject:  " + subject + "\nFrom:  " + from;
      em.sendMail (recipient, host, headers, body);
      em.close ();
   }

   public static void main (String host, String recipient, String subject, String from, String body) throws IOException {
      final Mail em = new Mail (new Socket (host, SMPT_Port));
      final String headers = "To:  " + recipient + "\nSubject:  " + subject + "\nFrom:  " + from;
      em.sendMail (recipient, host, headers, body);
      em.close ();
   }


   private final BufferedReader in;
   private final BufferedWriter out;
   
   Mail (Socket s) throws IOException {
      in = new BufferedReader (
	      new InputStreamReader(s.getInputStream()));
      out= new BufferedWriter (
	      new OutputStreamWriter(s.getOutputStream()));
   }

   public void close () throws IOException {
      out.close();
   }

   /*
   private static String local = "cs.fit.edu";
   static {
      try {
         final InetAddress addr = InetAddress.getLocalHost();
         final byte [] ipAddr = addr.getAddress();
         local = addr.getHostName();
      } catch (UnknownHostException ex) {
         ex.printStackTrace (System.err);
      }
   }
   */
   
   public void sendMail (String recipient, String host, String headers, String body) {
      helo (host);
      final String user  = System.getProperty ("user.name");

      //mail (user+"@"+host);
      //mail (user+"@"+local);
      mail ("submit@cs.fit.edu");
      rcpt (recipient);
      data (headers, body);
      quit ();
   }

   /*
     HELO <SP> <domain> <CRLF>
     In the HELO command the host sending the command identifies
     itself; the command may be interpreted as saying "Hello, I am
     <domain>".
   */
   public void helo (String host) { send ("HELO "+ host); }

   /*
     MAIL <SP> FROM:<reverse-path> <CRLF>
     This command tells the SMTP-receiver that a new mail transaction
     is starting and to reset all its state tables and buffers,
     including any recipients or mail data.  It gives the
     reverse-path which can be used to report errors.  If
     accepted, the receiver-SMTP returns a 250 OK reply.
   */
   public void mail (String path) { send ("MAIL FROM: <"+path+">"); }

   /*
     RCPT <SP> TO:<forward-path> <CRLF>
     This command is used to identify an individual recipient of
     the mail data; multiple recipients are specified by multiple
     use of this command.
   */
   public void rcpt (String recipient){send("RCPT TO: <"+recipient+">");}

   public void data (String headers, String body) {
      final String msg = headers + "\n" + body + "\n.\n";
      send ("DATA\n"+msg);
   }

   public void quit () { send ("QUIT"); }
   
   public void send (final String s) {
      try {
	 out.write (s + "\n");
	 out.flush ();
         //System.out.println (s);
	 final String resp = in.readLine ();  // discard response
      } catch (IOException e) {
	 e.printStackTrace();
      }
   }
}
