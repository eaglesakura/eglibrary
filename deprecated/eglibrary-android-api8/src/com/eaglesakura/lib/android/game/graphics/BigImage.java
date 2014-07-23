package com.eaglesakura.lib.android.game.graphics;

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.net.Uri;

import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.util.LogUtil;

public class BigImage {

    /**
     * フルHDのピクセル数
     */
    public static final int FULL_HD = 1920 * 1080;

    /**
     * 巨大な画像を前提として読み込みを行う。
     * @param resource
     * @param drawableId
     * @return
     */
    public static Bitmap loadBitmap(Resources resource, int drawableId, int maxPixels, Vector2 originSize) {
        Options option = new Options();
        option.inJustDecodeBounds = true;
        Bitmap result = BitmapFactory.decodeResource(resource, drawableId, option);

        int decodeWidth = option.outWidth;
        int decodeHeight = option.outHeight;
        originSize.set(decodeWidth, decodeHeight);
        int sample = 1;
        while ((decodeWidth * decodeHeight) > (maxPixels)) {
            ++sample;
            decodeWidth /= 2;
            decodeHeight /= 2;

            LogUtil.log("compact bitmap!! : " + decodeWidth + " x " + decodeHeight);
        }

        // サンプルサイズを指定して再度生成する
        option = new Options();
        option.inSampleSize = sample;
        result = BitmapFactory.decodeResource(resource, drawableId, option);
        return result;
    }

    /**
     * 巨大な画像を前提として読み込みを行う。
     * @param resource
     * @param drawableId
     * @return
     */
    public static Bitmap loadBitmap(ContentResolver resolver, Uri uri, int maxPixels, Vector2 originSize)
            throws IOException {
        Options option = new Options();
        option.inJustDecodeBounds = true;

        InputStream is = resolver.openInputStream(uri);
        Bitmap result = null;
        {
            result = BitmapFactory.decodeStream(is, new Rect(), option);
            is.close();
        }

        int decodeWidth = option.outWidth;
        int decodeHeight = option.outHeight;
        originSize.set(decodeWidth, decodeHeight);
        int sample = 1;
        while ((decodeWidth * decodeHeight) > (maxPixels)) {
            ++sample;
            decodeWidth /= 2;
            decodeHeight /= 2;

            LogUtil.log("compact bitmap!! : " + decodeWidth + " x " + decodeHeight);
        }

        // サンプルサイズを指定して再度生成する
        option = new Options();
        option.inSampleSize = sample;
        is = resolver.openInputStream(uri);
        result = BitmapFactory.decodeStream(is, new Rect(), option);
        is.close();
        return result;
    }
}
