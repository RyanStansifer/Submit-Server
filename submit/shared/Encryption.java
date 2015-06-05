package submit.shared;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import java.security.spec.KeySpec;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import java.io.IOException;

public class Encryption {
   final Cipher ecipher;
   final Cipher dcipher;

   public static javax.crypto.SealedObject sealObject (java.io.Serializable o) throws IOException {
      return sealObject (o, "Columba livia");
   }

   public static javax.crypto.SealedObject sealObject (java.io.Serializable o, String password) throws IOException {
      try {
         return new javax.crypto.SealedObject (o, new Encryption(password).getCipher(true));
      } catch (javax.crypto.IllegalBlockSizeException ex) {
         // Transform this exception
         throw new RuntimeException (ex);
      }
   }

   public static Object unsealObject (javax.crypto.SealedObject o) throws ClassNotFoundException, IOException {
      return unsealObject (o, "Columba livia");
   }

   public static Object unsealObject (javax.crypto.SealedObject o, String password) throws ClassNotFoundException, IOException {
      try {
         return o.getObject (new Encryption(password).getCipher(false));
      } catch (javax.crypto.IllegalBlockSizeException ex) {
         ex.printStackTrace (System.out);
         return null;
      } catch (javax.crypto.BadPaddingException ex) {
         ex.printStackTrace (System.out);
         return null;
      }
   }

   public Cipher getCipher (boolean encrypt) {
      if (encrypt) return ecipher; else return dcipher;
   }

   // 8-byte Salt
   private final byte[] salt = {
      (byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
      (byte)0x56, (byte)0x35, (byte)0xE3, (byte)0x03
   };
    
   private final static int iterationCount = 19;
    
   public Encryption (String passPhrase) {
      Cipher ecipher = null;
      Cipher dcipher = null;
      try {
         final KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);         // Create the key
         final SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
         ecipher = Cipher.getInstance(key.getAlgorithm());
         dcipher = Cipher.getInstance(key.getAlgorithm());
    
         // Prepare the parameter to the ciphers
         final AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
    
         // Create the ciphers
         ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
         dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
      } catch (java.security.InvalidAlgorithmParameterException ex) {
         ex.printStackTrace (System.out);
      } catch (java.security.spec.InvalidKeySpecException ex) {
         ex.printStackTrace (System.out);
      } catch (javax.crypto.NoSuchPaddingException ex) {
         ex.printStackTrace (System.out);
      } catch (java.security.NoSuchAlgorithmException ex) {
         ex.printStackTrace (System.out);
      } catch (java.security.InvalidKeyException ex) {
         ex.printStackTrace (System.out);
      } finally {
         this.ecipher = ecipher;
         this.dcipher = dcipher;
      }
   }
    
}
