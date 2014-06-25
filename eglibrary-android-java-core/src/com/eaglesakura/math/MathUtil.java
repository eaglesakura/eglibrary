package com.eaglesakura.math;

import android.graphics.RectF;

public class MathUtil {

    /**
     * フラグ情報を設定する。
     * 
     * 
     * @param flg
     * @param check
     * @param is
     *            ビットを立てる場合はtrue、下げる場合はfalse
     * @return
     */
    public static final int setFlag(int flg, int check, boolean is) {
        if (is) {
            return flg | check;
        } else {
            return flg & (~check);
        }
    }

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
     * 目標数値へ移動する。
     *
     * 
     * @param now
     * @param offset
     * @param target
     * @return
     */
    public static final float targetMove(float now, float offset, float target) {
        offset = Math.abs(offset);
        if (Math.abs(target - now) <= offset) {
            return target;
        } else if (target > now) {
            return now + offset;
        } else {
            return now - offset;
        }
    }

    /**
     * 目標数値へ移動する。
     *
     * 
     * @param now
     * @param offset
     * @param target
     * @return
     */
    public static final int targetMove(int now, int offset, int target) {
        offset = Math.abs(offset);
        if (Math.abs(target - now) <= offset) {
            return target;
        } else if (target > now) {
            return now + offset;
        } else {
            return now - offset;
        }
    }
    /**
     * 係数ブレンドを行い、結果を返す。
     * 1.0に近いほどaに近い値となる。
     * blend == 1 -> a
     * blend == 0 -> b
     * @param a
     * @param b
     * @param blend aのブレンド値
     * @return
     */
    public static float blendValue(float a, float b, float blend) {
        return a * blend + b * (1.0f - blend);
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
