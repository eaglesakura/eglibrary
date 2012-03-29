/**
 *
 * @author eagle.sakura
 * @version 2010/06/23 : 新規作成
 */
package com.eaglesakura.lib.android.game.math;

/**
 * @author eagle.sakura
 * @version 2009/11/14 : 新規作成
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
     * 
     * @author eagle.sakura
     * @version 2009/11/14 : 新規作成
     */
    public Vector2() {
    }

    /**
     * 値を指定して初期化する。
     * 
     * @author eagle.sakura
     * @param x
     * @param y
     * @param z
     * @version 2009/11/14 : 新規作成
     */
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * コピーを行う。
     * 
     * @author eagle.sakura
     * @param origin
     * @version 2009/11/14 : 新規作成
     */
    public Vector2(Vector2 origin) {
        set(origin);
    }

    /**
     * 値のコピーを行う。
     * 
     * @author eagle.sakura
     * @param origin
     * @version 2009/11/14 : 新規作成
     */
    public void set(Vector2 origin) {
        x = origin.x;
        y = origin.y;
    }

    /**
     * 値のコピーを行う。
     * 
     * @author eagle.sakura
     * @param origin
     * @version 2009/11/14 : 新規作成
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 内積を取得する。
     * 
     * @author eagle.sakura
     * @param v
     * @return
     * @version 2010/07/20 : 新規作成
     */
    public float dot(Vector2 v) {
        return (x * v.x) + (y * v.y);
    }

    /**
     * XYをmul倍する。
     * @param mul
     */
    public void mul(float mul) {
        x *= mul;
        y *= mul;
    }

    /**
     * 長さを取得する。
     * 
     * @author eagle.sakura
     * @return
     * @version 2009/11/29 : 新規作成
     */
    public float length() {
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    /**
     * 
     * @author eagle.sakura
     * @param p
     * @return
     */
    public float length(Vector2 p) {
        float tx = x - p.x, ty = y - p.y;
        return (float) Math.sqrt((double) ((tx * tx) + (ty * ty)));
    }

    /**
     * 
     * @author eagle.sakura
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
     * @author eagle.sakura
     * @version 2009/11/29 : 新規作成
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
     * @author eagle.sakura
     * @param obj
     * @version 2009/11/29 : 新規作成
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
     * @author eagle.sakura
     * @return
     * @version 2010/04/13 : 新規作成
     */
    @Override
    public String toString() {
        return "V( " + x + ", " + y + " )";
    }

}
