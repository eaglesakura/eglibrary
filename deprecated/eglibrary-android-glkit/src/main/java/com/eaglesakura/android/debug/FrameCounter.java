package com.eaglesakura.android.debug;

/**
 * 実際のフレームレートを計算する
 */
public class FrameCounter {
    int updates = 0;
    int realRate = 0;

    long startTime = System.currentTimeMillis();

    public void update() {

        ++updates;

        final long currentMs = System.currentTimeMillis();
        //! 現在時刻が前の記録時刻よりも1秒以上経過していたら、更新する
        if (currentMs - startTime > 1000) {
            realRate = updates;
            updates = 0;
            startTime = currentMs;
        }
    }

    /**
     * 実際のレートを取得する。
     */
    public int getRealRate() {
        return realRate;
    }
}
