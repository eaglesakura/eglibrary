package com.eaglesakura.android.net.parser;

import android.graphics.Bitmap;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.util.ImageUtil;

import java.io.InputStream;

public class BitmapParser implements RequestParser<Bitmap> {
    int maxWidth = 0;
    int maxHeight = 0;

    public BitmapParser() {
    }

    public BitmapParser(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public Bitmap parse(Connection<Bitmap> sender, AsyncTaskResult<Bitmap> taskResult, InputStream data) throws Exception {
        Bitmap bitmap = ImageUtil.decode(data);
        if (maxWidth > 0 && maxHeight > 0) {
            Bitmap scaled = ImageUtil.toScaledImage(bitmap, maxWidth, maxHeight);
            if (bitmap != scaled) {
                bitmap.recycle();
            }
            return scaled;
        } else {
            return bitmap;
        }
    }
}
