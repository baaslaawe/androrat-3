package com.corelib;

import java.io.Serializable;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * Basic encryption class
 * Created by hkff on 4/29/15.
 */
public class Crypto {

    /**
     * Encrypted Packet
     */
    public static class EncryptedPacket implements Serializable {
        // Attributes
        private  static  final  long serialVersionUID =  1850642861348722735L;
        public byte[] data;

        public EncryptedPacket(){}

        public EncryptedPacket(byte[] data){
            this.data = data;
        }
    }


    /**
     * Encrypt a string
     * @param msg
     * @return
     */
    public static String encrypt(String msg) {
        return "";
    }

    /**
     * Decrypt a string
     * @param msg
     * @return
     */
    public static String decrypt(String msg) {
        return "";
    }


    /**
     * Encrypt a Packet object
     * @param packet
     * @return
     */
    public static EncryptedPacket encrypt(Packet packet, SecretKeySpec sks) {
        EncryptedPacket ep = null;

        // Encrypt
        byte[] encodedBytes = null;

        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(packet.build());
            ep = new EncryptedPacket(encodedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ep;
    }

    /**
     * Decrypt a packet
     * @param packet
     * @return
     */
    public static Packet decrypt(EncryptedPacket packet, SecretKeySpec sks) {
        Packet p2 = new Packet();

        // Decode the encoded data with AES
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, sks);
            decodedBytes = c.doFinal(packet.data);
            p2.parse(decodedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return p2;
    }



    /**
     * Generate a secret key
     * @param seed
     * @return
     */
    public static synchronized SecretKeySpec genAESkey(String seed) {
        SecretKeySpec sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("any data used as random seed".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sks;
    }

}
