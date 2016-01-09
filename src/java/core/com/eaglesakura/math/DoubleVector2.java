package com.eaglesakura.math;

/**
 * double値で操作を行う二次元ベクトル
 */
public class DoubleVector2 {
    public double x = 0;
    public double y = 0;


    public DoubleVector2() {

    }

    public DoubleVector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(double x, double y) {
        this.x += x;
        this.y += y;
    }

    /**
     * lenとdegreeから位置を割り出す
     *
     * @param length
     * @param degree
     * @return
     */
    public static DoubleVector2 rotatedPosition(double length, double degree, DoubleVector2 result) {
        final double rad = Math.toRadians(degree);
        final double x0 = 0;
        final double y0 = length;

        result.x = x0 * Math.cos(rad) - y0 * Math.sin(rad);
        result.y = x0 * Math.sin(rad) + y0 * Math.cos(rad);
        return result;
    }

    /**
     * vec(x0, y0)からvec(x1, y1)への長さを取得する
     *
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return
     */
    public static double length(double x0, double y0, double x1, double y1) {
        double tx = x0 - x1;
        double ty = y0 - y1;
        return (float) Math.sqrt(((tx * tx) + (ty * ty)));
    }
}
