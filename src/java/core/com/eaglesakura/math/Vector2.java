package com.eaglesakura.math;

import com.eaglesakura.util.MathUtil;

/**
 *
 *
 */
public final class Vector2 {
    /**
     * X成分。
     */
    public float x = 0.0f;

    /**
     * Y成分。
     */
    public float y = 0.0f;

    /**
     * ゼロ初期化したベクトルを作成する。
     */
    public Vector2() {
    }

    /**
     * 値を指定して初期化する。
     *
     * @param x
     * @param y
     */
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * コピーを行う。
     *
     * @param origin
     */
    public Vector2(Vector2 origin) {
        set(origin);
    }

    /**
     * 値のコピーを行う。
     *
     * @param origin
     */
    public void set(Vector2 origin) {
        x = origin.x;
        y = origin.y;
    }

    /**
     * 値のコピーを行う。
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 内積を取得する。
     *
     * @param v
     * @return
     */
    public float dot(Vector2 v) {
        return (x * v.x) + (y * v.y);
    }

    /**
     * XYをmul倍する。
     *
     * @param mul
     */
    public void mul(float mul) {
        x *= mul;
        y *= mul;
    }

    /**
     * 長さを取得する。
     *
     * @return
     */
    public float length() {
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    public float length(Vector2 p) {
        float tx = x - p.x, ty = y - p.y;
        return (float) Math.sqrt((double) ((tx * tx) + (ty * ty)));
    }

    public float length(final float px, final float py) {
        float tx = x - px, ty = y - py;
        return (float) Math.sqrt((double) ((tx * tx) + (ty * ty)));
    }

    public static float length(final float px0, final float py0, final float px1, final float py1) {
        float tx = px0 - px1;
        float ty = py0 - py1;
        return (float) Math.sqrt((double) ((tx * tx) + (ty * ty)));
    }

    public static double length(final double px0, final double py0, final double px1, final double py1) {
        double tx = px0 - px1;
        double ty = py0 - py1;
        return Math.sqrt(((tx * tx) + (ty * ty)));
    }

    /**
     * ベクトルの長さを正規化する。
     */
    public void normalize() {
        final float len = length();
        if (len == 0) {
            return;
        }
        x /= len;
        y /= len;
    }

    /**
     * 整合性確認。
     *
     * @param obj
     */
    @Override
    public boolean equals(Object obj) {
        Vector2 v = (Vector2) obj;
        return x == v.x && y == v.y;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * 文字列変換を行う。
     *
     * @return
     */
    @Override
    public String toString() {
        return "V( " + x + ", " + y + " )";
    }

    /**
     * p0からp1を見た場合の角度をdegreeで求める
     * <br>
     * 角度は真上方向を0として反時計回りに360度で求める。
     *
     * @param p0
     * @param p1
     * @return
     */
    public static double degree(Vector2 p0, Vector2 p1) {
        final double length = Vector2.length(p0.x, p0.y, p1.x, p1.y);
        if (length == 0) {
            return 0;
        }

        final double vecX = (p1.x - p0.x) / length;
        final double vecY = (p1.y - p0.y) / length;

        final double radian = Math.atan2(vecX, vecY);
        return MathUtil.normalizeDegree(360.0 - ((radian * 180.0) / Math.PI));
    }

}
