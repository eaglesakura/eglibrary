package com.eaglesakura.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 暗号化・指紋サポートを行う
 *
 */
public class EncodeUtil {

    /**
     * byte配列からMD5を求める
     * 
     * @param buffer
     * @return
     */
    public static String genMD5(byte[] buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * byte配列からMD5を求める
     * 
     * @param buffer
     * @return
     */
    public static String genMD5(InputStream is) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            {
                byte[] buffer = new byte[1024 * 8];
                int readed = 0;
                while ((readed = is.read(buffer)) > 0) {
                    md.update(buffer, 0, readed);
                }
            }

            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * byte配列からMD5を求める
     * 
     * @param buffer
     * @return
     */
    public static String genSHA1(byte[] buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(buffer);
            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
