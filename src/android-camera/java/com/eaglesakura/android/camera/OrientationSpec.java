package com.eaglesakura.android.camera;

import com.eaglesakura.math.MathUtil;

/**
 * カメラの回転角情報
 */
public class OrientationSpec {

    private final int degree;

    private OrientationSpec(int rotateDegree) {
        this.degree = rotateDegree;
    }

    /**
     * 回転角を取得する
     *
     * @return
     */
    public int getDegree() {
        return degree;
    }

    /**
     * 縦向きである場合はtrue
     *
     * @return
     */
    public boolean isVertical() {
        return degree == 90 || degree == 180;
    }

    /**
     * 横向きであればtrue
     *
     * @return
     */
    public boolean isHorizontal() {
        return !isVertical();
    }

    /**
     * 回転0度
     */
    public static final OrientationSpec ROTATE_0 = new OrientationSpec(0);

    /**
     * 回転90度
     */
    public static final OrientationSpec ROTATE_90 = new OrientationSpec(90);

    /**
     * 回転180度
     */
    public static final OrientationSpec ROTATE_180 = new OrientationSpec(180);

    /**
     * 回転270度
     */
    public static final OrientationSpec ROTATE_270 = new OrientationSpec(270);

    /**
     * 回転角度から取得する
     *
     * @param rotate
     * @return
     */
    public static final OrientationSpec fromDegree(int rotate) {
        rotate = (int) MathUtil.normalizeDegree(rotate);
        rotate = (rotate / 90) * 90;    // 90度区切りに修正する
        switch (rotate) {
            case 0:
                return ROTATE_0;
            case 90:
                return ROTATE_90;
            case 180:
                return ROTATE_180;
            case 270:
                return ROTATE_270;
            default:
                throw new IllegalStateException(String.format("Rotate error(%d)", rotate));
        }
    }
}
