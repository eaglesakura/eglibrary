package com.eaglesakura.android.device.vibrate;

import android.content.Context;
import android.os.Vibrator;

/**
 *
 */
public class VibrateManager {
    /**
     * 短めに鳴らす時間
     */
    public static final long VIBRATE_TIME_SHORT_MS = 100;

    /**
     * 長めに鳴らす
     */
    public static final long VIBRATE_TIME_LONG_MS = 500;

    /**
     * バイブを短く一回鳴らす
     *
     * @param context Context
     */
    public static void vibrateShort(Context context) {
        vibratePattern(context, new long[]{
                0, VIBRATE_TIME_SHORT_MS,
        }, -1);
    }

    /**
     * バイブを長めに一回鳴らす
     *
     * @param context Context
     */
    public static void vibrateLong(Context context) {
        vibratePattern(context, new long[]{
                0, VIBRATE_TIME_LONG_MS,
        }, -1);
    }

    /**
     * 指定時間振動を行う
     *
     * @param context Context
     * @param timeMs  振動時間（ミリ秒）
     */
    public static void vibrate(Context context, long timeMs) {
        vibratePattern(context, new long[]{
                0, timeMs,
        }, -1);
    }

    /**
     * 振動を開始する
     *
     * @param context Context
     * @param pattern 振動パターン（ミリ秒単位、停止時間から入力）
     * @param repeat  繰り返し回数。負の値でリピートなし
     */
    public static void vibratePattern(Context context, long[] pattern, int repeat) {
        try {
            Vibrator vibrator = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (!vibrator.hasVibrator()) {
                // バイブを持たないなら何もしない
                return;
            }

            vibrator.vibrate(pattern, repeat);
        } catch (Exception e) {

        }
    }
}
