package com.eaglesakura.android.graphics;

import com.eaglesakura.util.MathUtil;

/**
 * OpenGL向けRGBA色を扱う。
 * <br>
 * Canvas向けandroid.graphics.ColorクラスはARGB色であることに注意が必要。
 */
public class ColorRGBA {

    /**
     * 白
     */
    public static final int WHITE = toColorRGBA(255, 255, 255, 255);

    /**
     * 赤
     */
    public static final int RED = toColorRGBA(255, 0, 0, 255);

    /**
     * 緑
     */
    public static final int GREEN = toColorRGBA(0, 255, 0, 255);

    /**
     * 青
     */
    public static final int BLUE = toColorRGBA(0, 0, 255, 255);

    /**
     * 黒
     */
    public static final int BLACK = toColorRGBA(0, 0, 0, 255);

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

    public ColorRGBA() {

    }

    public ColorRGBA(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public ColorRGBA(int r, int g, int b, int a) {
        this.r = (float) r / 255.0f;
        this.g = (float) g / 255.0f;
        this.b = (float) b / 255.0f;
        this.a = (float) a / 255.0f;
    }

    public ColorRGBA(ColorRGBA origin) {
        this.r = origin.r;
        this.g = origin.g;
        this.b = origin.b;
        this.a = origin.a;
    }

    public ColorRGBA(int rgba) {
        set(toColorR(rgba), toColorG(rgba), toColorB(rgba), toColorA(rgba));
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

    public void set(ColorRGBA color) {
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
        return ColorRGBA.toColorRGBA(getIntR(), getIntG(), getIntB(), getIntA());
    }

    /**
     * nexutの色へ少しずつ遷移させる
     *
     * @param next
     * @param offset
     */
    public void move(final ColorRGBA next, final float offset) {
        r = MathUtil.targetMove(r, offset, next.r);
        g = MathUtil.targetMove(g, offset, next.g);
        b = MathUtil.targetMove(b, offset, next.b);
        a = MathUtil.targetMove(a, offset, next.a);
    }

    /**
     * nexutの色へ少しずつ遷移させる
     *
     * @param nextRGBA 遷移先RGBA
     * @param offset   オフセット量
     */
    public void move(final int nextRGBA, final int offset) {
        r = MathUtil.targetMove(r, (float) offset / 255.0f, toColorRf(nextRGBA));
        g = MathUtil.targetMove(g, (float) offset / 255.0f, toColorGf(nextRGBA));
        b = MathUtil.targetMove(b, (float) offset / 255.0f, toColorBf(nextRGBA));
        a = MathUtil.targetMove(a, (float) offset / 255.0f, toColorAf(nextRGBA));
    }

    /**
     * ARGB(Canvas color)をRGBA(GL color）に変換する。
     *
     * @param argb
     * @return
     */
    public static int argb2rgba(final int argb) {
        return (argb << 8) | ((argb >> 24) & 0xff);
    }

    /**
     * RGBA(GL color）をARGB(Canvas color)に変換する。
     *
     * @param rgba
     * @return
     */
    public static int rgba2argb(final int rgba) {
        return ((rgba >> 8) & 0x00ffffff) | ((rgba & 0xff) << 24);
    }

    /**
     * RGBA8bitを一つのintにまとめる。
     * 上位ビットからRGBAの順に並ぶ。
     *
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
     *
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
     * RGB565のshort型へ変換する。
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static short toColorRGB565(int r, int g, int b) {
        r = (r & 0xff) >> 3;
        g = (g & 0xff) >> 2;
        b = (b & 0xff) >> 3;
        return (short) ((r << (6 + 5)) | g << 5 | b);
    }

    /**
     * RGBA色からAを取り出す。
     *
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
     *
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
     *
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
     *
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
     * RGB色からHSVへ変換する
     * <br>
     * result[0] : H
     * <br>
     * result[1] : S
     * <br>
     * result[2] : V
     * <br>
     * 参考:http://ja.wikipedia.org/wiki/HSV%E8%89%B2%E7%A9%BA%E9%96%93
     *
     * @param _r8 赤要素8bit
     * @param _g8 緑要素8bit
     * @param _b8 青要素8bit
     * @return HSV要素
     */
    public static float[] toHSV(int _r8, int _g8, int _b8) {
        float r = (float) _r8 / 255.0f;
        float g = (float) _g8 / 255.0f;
        float b = (float) _b8 / 255.0f;

        float max = r > g ? r : g;
        max = max > b ? max : b;
        float min = r < g ? r : g;
        min = min < b ? min : b;
        float h = max - min;
        if (h > 0.0f) {
            if (max == r) {
                h = (g - b) / h;
                if (h < 0.0f) {
                    h += 6.0f;
                }
            } else if (max == g) {
                h = 2.0f + (b - r) / h;
            } else {
                h = 4.0f + (r - g) / h;
            }
        }
        h /= 6.0f;
        float s = (max - min);
        if (max != 0.0f) {
            s /= max;
        }
        float v = max;
        return new float[]{h, s, v};
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
            int value0 = (ColorRGBA.toColorR(rgba0));
            int value1 = (ColorRGBA.toColorR(rgba1));

            int color = (int) (MathUtil.blendValue(value0, value1, blend));
            result |= ((color & 0xff) << 24);
        }

        {
            int value0 = (ColorRGBA.toColorG(rgba0));
            int value1 = (ColorRGBA.toColorG(rgba1));

            int color = (int) (MathUtil.blendValue(value0, value1, blend));
            result |= ((color & 0xff) << 16);
        }

        {
            int value0 = (ColorRGBA.toColorB(rgba0));
            int value1 = (ColorRGBA.toColorB(rgba1));

            int color = (int) (MathUtil.blendValue(value0, value1, blend));
            result |= ((color & 0xff) << 8);
        }

        {
            int value0 = (ColorRGBA.toColorA(rgba0));
            int value1 = (ColorRGBA.toColorA(rgba1));

            int color = (int) (MathUtil.blendValue(value0, value1, blend));
            result |= ((color) & 0xff);
        }

        return result;
    }
}
