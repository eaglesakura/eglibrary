package com.eaglesakura.android.util;

import android.graphics.RectF;

/**
 * Android専用class系のMathUtil
 */
public class AndroidMathUtil {

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
}
