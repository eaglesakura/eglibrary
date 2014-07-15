package com.eaglesakura.math;

public class MathUtil {

    /**
     * フラグ情報を設定する。
     *
     * @param flg
     * @param check
     * @param is    ビットを立てる場合はtrue、下げる場合はfalse
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
     * min <= result <= maxとなるようにnowを補正する。
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
     * <p/>
     * 0 <= now < 360 となる
     *
     * @param now 現在の360度系角度
     * @return 正規化された360度系角度
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
     * 360度系の正規化を行う。
     * <p/>
     * 0 <= return < 360 となる
     *
     * @param now 現在の360度系角度
     * @return 正規化された360度系角度
     */
    public static final double normalizeDegree(double now) {
        while (now < 0.0f) {
            now += 360.0f;
        }

        while (now >= 360.0f) {
            now -= 360.0f;
        }

        return now;
    }

    /**
     * targetの角度を向かせるためにちょうどいい角度を算出する。
     * <p/>
     * targetへの角度が180度を超える場合、正負反転させた角度を選択する
     * <p/>
     * -360 < return <= 360 となる。
     *
     * @param now
     * @param target
     * @return
     */
    public static final double calcNearDegree(double now, double target) {
        now = normalizeDegree(now);
        target = normalizeDegree(target);

        // now -> targetへの角度を求める
        final double diff = target - now;

        if (Math.abs(diff) <= 180) {
            // 半周以内であれば、この角度で返してもいい
            return target;
        } else {
            // 半周を超えているのならば、別角度として返さなければならない。
            if (now > 180) {
                return target + 360;
            } else {
                return target - 360;
            }
        }
    }

    /**
     * 目標数値へ移動する。
     *
     * @param now    現在値
     * @param offset 移動量
     * @param target 目標とすべき値
     * @return 計算後の値
     */
    public static final double targetMove(double now, double offset, double target) {
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
     * @param now    現在値
     * @param offset 移動量
     * @param target 目標とすべき値
     * @return 計算後の値
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
     * @param now    現在値
     * @param offset 移動量
     * @param target 目標とすべき値
     * @return 計算後の値
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
     *
     * @param a     遷移後の値
     * @param b     遷移前の値
     * @param blend aのブレンド値
     * @return ブレンド後の値
     */
    public static float blendValue(float a, float b, float blend) {
        return a * blend + b * (1.0f - blend);
    }


    /**
     * 係数ブレンドを行い、結果を返す。
     * 1.0に近いほどaに近い値となる。
     * blend == 1 -> a
     * blend == 0 -> b
     *
     * @param a     遷移後の値
     * @param b     遷移前の値
     * @param blend aのブレンド値
     * @return ブレンド後の値
     */
    public static double blendValue(double a, double b, double blend) {
        return a * blend + b * (1.0 - blend);
    }

    /**
     * 特定のビットフラグが立っていることを検証する。
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
     * @param flg
     * @param check
     * @return
     */
    public static final boolean isFlagOnAll(int flg, int check) {
        return (flg & check) == 0;
    }

}
