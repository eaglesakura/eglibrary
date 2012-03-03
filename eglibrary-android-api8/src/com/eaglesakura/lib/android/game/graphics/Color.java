package com.eaglesakura.lib.android.game.graphics;

import com.eaglesakura.lib.android.game.util.GameUtil;

public class Color {
    /**
     * 透過成分
     */
    public float a = 1;

    /**
     * 赤成分
     */
    public float r = 1;

    /**
     * 緑成分
     */
    public float g = 1;

    /**
     * 青成分
     */
    public float b = 1;

    public Color() {

    }

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(int r, int g, int b, int a) {
        this.r = (float) r / 255.0f;
        this.g = (float) g / 255.0f;
        this.b = (float) b / 255.0f;
        this.a = (float) a / 255.0f;
    }

    public Color(Color origin) {
        this.r = origin.r;
        this.g = origin.g;
        this.b = origin.b;
        this.a = origin.a;
    }

    public Color(int rgba) {
        set(toColorR(rgba), toColorG(rgba), toColorG(rgba), toColorA(rgba));
    }

    public void set(int r, int g, int b, int a) {
        this.r = (float) r / 255.0f;
        this.g = (float) g / 255.0f;
        this.b = (float) b / 255.0f;
        this.a = (float) a / 255.0f;
    }

    public void set(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void set(int rgba) {
        set(toColorR(rgba), toColorG(rgba), toColorB(rgba), toColorA(rgba));
    }

    public void set(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    public int getIntR() {
        return (int) (255 * r) & 0xff;
    }

    public int getIntG() {
        return (int) (255 * g) & 0xff;
    }

    public int getIntB() {
        return (int) (255 * b) & 0xff;
    }

    public int getIntA() {
        return (int) (255 * a) & 0xff;
    }

    public int getRGBA() {
        return Color.toColorRGBA(getIntR(), getIntG(), getIntB(), getIntA());
    }

    /**
     * nexutの色へ少しずつ遷移させる
     * @param next
     * @param offset
     */
    public void move(final Color next, final float offset) {
        r = GameUtil.targetMove(r, offset, next.r);
        g = GameUtil.targetMove(g, offset, next.g);
        b = GameUtil.targetMove(b, offset, next.b);
        a = GameUtil.targetMove(a, offset, next.a);
    }

    /**
     * nexutの色へ少しずつ遷移させる
     * @param next
     * @param offset
     */
    public void move(final int nextRGBA, final int offset) {
        r = GameUtil.targetMove(r, (float) offset / 255.0f, toColorRf(nextRGBA));
        g = GameUtil.targetMove(g, (float) offset / 255.0f, toColorGf(nextRGBA));
        b = GameUtil.targetMove(b, (float) offset / 255.0f, toColorBf(nextRGBA));
        a = GameUtil.targetMove(a, (float) offset / 255.0f, toColorAf(nextRGBA));
    }

    /**
     * RGBA8bitを一つのintにまとめる。
     * 上位ビットからRGBAの順に並ぶ。
     * @param r
     * @param g
     * @param b
     * @param a
     * @return
     */
    public static int toColorRGBA(int r, int g, int b, int a) {
        return ((r & 0xff) << 24) | ((g & 0xff) << 16) | ((b & 0xff) << 8) | (a & 0xff);
    }

    /**
     * RGBAfloatを一つのintにまとめる。
     * 上位ビットからRGBAの順に並ぶ。
     * @param r
     * @param g
     * @param b
     * @param a
     * @return
     */
    public static int toColorRGBA(float r, float g, float b, float a) {
        return toColorRGBA((int) (255.0f * r), (int) (255.0f * g), (int) (255.0f * b), (int) (255.0f * a));
    }

    /**
     * RGBA色からAを取り出す。
     * @param colorRGBA
     * @return
     */
    public static int toColorA(int colorRGBA) {
        return (colorRGBA & 0xff);
    }

    public static float toColorAf(int colorRGBA) {
        return (float) (colorRGBA & 0xff) / 255.0f;
    }

    /**
     * RGBA色からRを取り出す
     * @param colorRGBA
     * @return
     */
    public static int toColorR(int colorRGBA) {
        return (colorRGBA >> 24) & 0xff;
    }

    public static float toColorRf(int colorRGBA) {
        return (float) ((colorRGBA >> 24) & 0xff) / 255.0f;
    }

    /**
     * RGBA色からGを取り出す。
     * @param colorRGBA
     * @return
     */
    public static int toColorG(int colorRGBA) {
        return (colorRGBA >> 16) & 0xff;
    }

    public static float toColorGf(int colorRGBA) {
        return (float) ((colorRGBA >> 16) & 0xff) / 255.0f;
    }

    /**
     * RGBA色からBを取り出す。
     * @param colorRGBA
     * @return
     */
    public static int toColorB(int colorRGBA) {
        return (colorRGBA >> 8) & 0xff;
    }

    public static float toColorBf(int colorRGBA) {
        return (float) ((colorRGBA >> 8) & 0xff) / 255.0f;
    }

    /**
     * 色をブレンドして返す。
     * 
     * @param rgba0
     * @param rgba1
     * @param blend
     * @return
     */
    public static int blendColor(int rgba0, int rgba1, float blend) {
        int result = 0;

        {
            int value0 = (Color.toColorR(rgba0));
            int value1 = (Color.toColorR(rgba1));

            int color = (int) (GameUtil.blendValue(value0, value1, blend));
            result |= ((color & 0xff) << 24);
        }

        {
            int value0 = (Color.toColorG(rgba0));
            int value1 = (Color.toColorG(rgba1));

            int color = (int) (GameUtil.blendValue(value0, value1, blend));
            result |= ((color & 0xff) << 16);
        }

        {
            int value0 = (Color.toColorB(rgba0));
            int value1 = (Color.toColorB(rgba1));

            int color = (int) (GameUtil.blendValue(value0, value1, blend));
            result |= ((color & 0xff) << 8);
        }

        {
            int value0 = (Color.toColorA(rgba0));
            int value1 = (Color.toColorA(rgba1));

            int color = (int) (GameUtil.blendValue(value0, value1, blend));
            result |= ((color) & 0xff);
        }

        return result;
    }
}
