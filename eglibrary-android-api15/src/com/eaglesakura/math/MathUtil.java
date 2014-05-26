package com.eaglesakura.math;

import android.graphics.RectF;

public class MathUtil {

    /**
     * RectFを四捨五入で丸める。
     * 
     * @param rect
     */
    public static void round(RectF rect) {
        rect.left = Math.round(rect.left);
        rect.right = Math.round(rect.right);
        rect.top = Math.round(rect.top);
        rect.bottom = Math.round(rect.bottom);
    }

    /**
     * min <= result <= maxとなるようにnowを補正する。
     * 
     * 
     * @param min
     * @param max
     * @param now
     * @return
     */
    public static final int minmax(int min, int max, int now) {
        if (now < min)
            return min;
        if (now > max)
            return max;
        return now;
    }

    /**
     * min <= result <= maxとなるようにnowを補正する。
     * 
     * 
     * @param min
     * @param max
     * @param now
     * @return
     */
    public static final float minmax(float min, float max, float now) {
        if (now < min)
            return min;
        if (now > max)
            return max;
        return now;
    }

    /**
     * 360度系の正規化を行う。
     * 
     * 
     * @param now
     * @return
     * 
     */
    public static final float normalizeDegree(float now) {
        while (now < 0.0f) {
            now += 360.0f;
        }

        while (now >= 360.0f) {
            now -= 360.0f;
        }

        return now;
    }

    /**
     * 特定のビットフラグが立っていることを検証する。
     * 
     * 
     * @param flg
     * @param check
     * @return
     */
    public static final boolean isFlagOn(int flg, int check) {
        return (flg & check) != 0;
    }

    /**
     * 特定のビットフラグがすべて立っていることを検証する。
     * 
     * 
     * @param flg
     * @param check
     * @return
     */
    public static final boolean isFlagOnAll(int flg, int check) {
        return (flg & check) == 0;
    }

}
