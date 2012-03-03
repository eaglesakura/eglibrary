package com.eaglesakura.lib.android.game.util;

public class Timer {
    protected long startTime = System.currentTimeMillis();
    protected long endTime = System.currentTimeMillis();

    public Timer() {

    }

    public Timer(long startTime) {
        this.startTime = startTime;
    }

    /**
     * タイマーを開始する。
     * @param startTime
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * ストップウォッチを開始する。
     */
    public void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * ストップウォッチを停止し、時間を取得する。
     * @return
     */
    public long end() {
        endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
