package com.eaglesakura.lib.android.game.math;

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
     * @param z
     * 
     */
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * コピーを行う。
     * 
     * @param origin
     * 
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

     * @param origin
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 内積を取得する。
     * 
     * 
     * @param v
     * @return
     * 
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

    /**
     * 
     * 
     * @param p
     * @return
     */
    public float length(Vector2 p) {
        float tx = x - p.x, ty = y - p.y;
        return (float) Math.sqrt((double) ((tx * tx) + (ty * ty)));
    }

    /**
     * 
     * 
     * @param p
     * @return
     */
    public float length(final float px, final float py) {
        float tx = x - px, ty = y - py;
        return (float) Math.sqrt((double) ((tx * tx) + (ty * ty)));
    }

    /**
     * ベクトルの長さを正規化する。
     * 
     * 
     * 
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
     * 
     * @param obj
     * 
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
     * 
     * @return
     * 
     */
    @Override
    public String toString() {
        return "V( " + x + ", " + y + " )";
    }

}
