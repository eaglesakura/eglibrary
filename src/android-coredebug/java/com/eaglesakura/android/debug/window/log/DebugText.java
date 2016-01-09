package com.eaglesakura.android.debug.window.log;

import com.eaglesakura.android.graphics.FontCalculator;
import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.android.graphics.ColorRGBA;
import com.eaglesakura.math.Vector2;

/**
 * デバッグ描画用テキスト
 */
public class DebugText extends DebugRenderingItem {
    /**
     * テキスト情報
     */
    FontCalculator fontCalculator = new FontCalculator();

    /**
     * テキストのメッセージ
     */
    String message = "msg";

    /**
     * テキスト色
     */
    int argb = ColorRGBA.WHITE;

    private static int DEFAULT_FONT_SIZE = 64;

    public DebugText() {
    }

    public DebugText(String message) {
        this.message = message;
        fontCalculator.setFontHeight(DEFAULT_FONT_SIZE);
    }


    public DebugText(String message, int fontHeightPixel) {
        this.message = message;
        setTextHeightPixel(fontHeightPixel);
    }


    public void setTextHeightPixel(int textHeightPixel) {
        fontCalculator.setFontHeight(textHeightPixel);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setArgb(int argb) {
        this.argb = argb;
    }

    @Override
    public void getRenderingSize(Vector2 size) {
        size.set(fontCalculator.calcTextArea(message));
    }

    @Override
    public void rendering(Graphics graphics, int x, int y) {
        graphics.setColorARGB(argb);
        fontCalculator.drawString(message, "...", x, y, graphics.getWidth(), 1, 0, graphics.getCanvas(), graphics.getPaint());
    }

    /**
     * デフォルトのフォントサイズをピクセル単位で指定する
     *
     * @param defaultFontSize
     */
    public static void setDefaultFontSize(int defaultFontSize) {
        DEFAULT_FONT_SIZE = defaultFontSize;
    }
}
