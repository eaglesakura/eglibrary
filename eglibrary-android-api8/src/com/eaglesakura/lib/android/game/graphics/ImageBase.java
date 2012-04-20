package com.eaglesakura.lib.android.game.graphics;

import java.security.MessageDigest;

import android.graphics.Bitmap;

import com.eaglesakura.lib.android.game.resource.GCResourceBase;
import com.eaglesakura.lib.android.game.resource.GarbageCollector;
import com.eaglesakura.lib.android.game.util.GameUtil;

/**
 * 画像を管理する。<BR>
 * 画像の管理方法についてはサブクラスに一任し、こちらでは関与しない。
 * 
 * @author TAKESHI YAMASHITA
 */
public abstract class ImageBase extends GCResourceBase {

    protected ImageBase(GarbageCollector garbageCollector) {
        super(garbageCollector);
    }

    /**
     * 画像幅を取得する
     * @return
     */
    public abstract int getWidth();

    /**
     * 画像高を取得する
     * @return
     */
    public abstract int getHeight();

    /**
     * 画像RGBA情報からSHA1指紋を作成する。
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
                md.update(GameUtil.toByteArray(pixels, src));
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

}
