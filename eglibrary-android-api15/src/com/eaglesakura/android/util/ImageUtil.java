package com.eaglesakura.android.util;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.eaglesakura.io.IOUtil;

public class ImageUtil {

    /**
     * 画像からSHA1指紋を作成する。
     * @param bitmap
     * @return
     */
    public static String genSHA1(Bitmap bitmap) {
        final int[] pixels = new int[bitmap.getWidth()];
        final byte[] src = new byte[pixels.length * 4];
        final int height = bitmap.getHeight();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            for (int i = 0; i < height; ++i) {
                bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, i, bitmap.getWidth(), 1);
                md.update(IOUtil.toByteArray(pixels, src));
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
     * image bufferからデコードする
     * @param imageFile
     * @return
     */
    public static Bitmap decode(byte[] imageFile) {
        if (imageFile == null) {
            return null;
        }

        try {
            return BitmapFactory.decodeByteArray(imageFile, 0, imageFile.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * PNG画像にエンコードする
     * @param bitmap
     * @return
     */
    public static byte[] encodePng(Bitmap bitmap) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            bitmap.compress(CompressFormat.PNG, 100, os);
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
