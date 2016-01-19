package com.eaglesakura.android.net.parser;

import android.content.Context;
import android.graphics.Bitmap;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.util.ImageUtil;

import java.io.InputStream;

public class AlphaBitmapImageParser implements RequestParser<Bitmap> {
    int maxWidth;
    int maxHeight;
    int alphaImageDrawable;
    final Context context;

    public AlphaBitmapImageParser(final Context context, int maxWidth, int maxHeight, int alphaImageDrawable) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.context = context.getApplicationContext();
        this.alphaImageDrawable = alphaImageDrawable;
    }

    @Override
    public Bitmap parse(Connection<Bitmap> sender, AsyncTaskResult<Bitmap> taskResult, InputStream data) throws Exception {
        Bitmap bitmap = ImageUtil.decode(data);
        Bitmap scaled = ImageUtil.toScaledImage(bitmap, maxWidth, maxHeight);
        if (bitmap != scaled) {
            bitmap.recycle();
        }

        Bitmap alpha = ImageUtil.decode(context, alphaImageDrawable);
        Bitmap blend = ImageUtil.blendAlpha(scaled, alpha);
        scaled.recycle();
        alpha.recycle();

        return blend;
    }
}
