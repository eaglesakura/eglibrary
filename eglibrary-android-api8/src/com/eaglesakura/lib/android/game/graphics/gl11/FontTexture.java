package com.eaglesakura.lib.android.game.graphics.gl11;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.eaglesakura.lib.android.game.graphics.Color;
import com.eaglesakura.lib.android.game.graphics.canvas.Graphics;

public class FontTexture extends BitmapTextureImage {
    protected Paint paint = new Paint();
    protected String text = null;
    protected int fontSize = 0;
    /**
     * テキストのバウンズエリア
     */
    protected Rect bounds = new Rect();

    /**
     * 
     * @param text
     * @param fontSize
     * @param glManager
     */
    public FontTexture(String text, int fontSize, OpenGLManager glManager) {
        super(glManager);
        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        createFont(text, Typeface.DEFAULT, fontSize);
    }

    /**
     * 
     * @param text
     * @param fontSize
     * @param typeface
     * @param glManager
     */
    public FontTexture(String text, int fontSize, Typeface typeface, OpenGLManager glManager) {
        super(glManager);
        paint.setStyle(Style.STROKE);
        paint.setAntiAlias(true);
        createFont(text, typeface, fontSize);
    }

    protected Bitmap createFontImage(String text, Typeface typeface, int fontSize) {

        paint.setTextSize(fontSize);
        paint.setTypeface(typeface);
        paint.getTextBounds(text, 0, text.length(), bounds);

        FontMetrics fontMetrics = paint.getFontMetrics();

        final int IMAGE_WIDTH = Math.max(1, bounds.width());
        final int IMAGE_HEIGHT = (int) Math.max(//
                Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent),
                //
                (Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom)));
        Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Graphics graphics = new Graphics();
        graphics.setCanvas(canvas);
        paint.setColor(0xffffffff);
        canvas.drawText(text, -bounds.left, -fontMetrics.top, paint);

        return bitmap;
    }

    /**
     * テクスチャのソースエリアを取得する。
     * @param start
     * @param end
     * @return
     */
    public Rect getSrcArea(int start, int end) {
        Rect result = new Rect();
        paint.getTextBounds(text, 0, start, result);
        final int headerWidth = result.width();
        paint.getTextBounds(text, end, text.length(), result);
        final int fooderWidth = result.width();

        result.left = headerWidth;
        result.right = getWidth() - fooderWidth;
        result.top = 0;
        result.bottom = getHeight();

        return result;
    }

    /**
     * フォントを作成する。古いフォントは削除される。
     * @param text
     * @param fontSize
     */
    public void createFont(String text, Typeface typeface, int fontSize) {
        this.text = text;
        this.fontSize = fontSize;
        Bitmap bitmap = createFontImage(text, typeface, fontSize);
        initTexture(bitmap);
        bitmap.recycle();
    }

    /**
     * フォントの描画色を設定する。
     * 白・黒以外の中途半端な色はこれで指定すると補正される。
     * @param fontColorRGBA
     */
    public void setFontColorRGBA(int fontColorRGBA) {
        final GL10 gl = glManager.getGL();
        gl.glTexEnvfv(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_COLOR, new float[] {
                Color.toColorRf(fontColorRGBA), //
                Color.toColorGf(fontColorRGBA), //
                Color.toColorBf(fontColorRGBA), //
                Color.toColorAf(fontColorRGBA), //
        }, 0);
    }

    @Override
    public void bind() {
        super.bind();
        final GL10 gl = glManager.getGL();
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_BLEND);
    }

    @Override
    public void unbind() {
        final GL10 gl = glManager.getGL();
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
        super.unbind();
    }

    /**
     * フォントの大きさを取得する。
     * @return
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * 生成済みのテクスチャを取得する。
     * @return
     */
    public String getText() {
        return text;
    }
}
