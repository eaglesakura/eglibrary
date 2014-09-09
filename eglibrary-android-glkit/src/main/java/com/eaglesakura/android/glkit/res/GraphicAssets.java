package com.eaglesakura.android.glkit.res;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCMethod;

import java.io.InputStream;

/**
 *
 */
@JCClass(cppNamespace = "es.glkit")
public class GraphicAssets {

    /**
     * AssetManagerを取得する
     * @param context
     * @return
     */
    @JCMethod
    public static AssetManager getAssets(Context context) {
        return context.getAssets();
    }

    /**
     * Bitmapオブジェクトをデコードする
     *
     * @param bitmap
     * @param recycle
     * @return
     */
    @JCMethod
    public static DecodedImage decodeImage(Bitmap bitmap, boolean recycle) {
        return DecodedImage.decodeFromBitmap(bitmap, recycle);
    }

    /**
     * assetsから画像を読み出す
     *
     * @param context
     * @param path
     * @return
     */
    @JCMethod
    public static Bitmap loadImageFromAssets(Context context, String path) {
        InputStream is = null;
        try {
            is = context.getAssets().open(path);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {

                }
            }
        }
        return null;
    }
}
