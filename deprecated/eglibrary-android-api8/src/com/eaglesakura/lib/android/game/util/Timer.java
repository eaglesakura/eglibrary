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
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * ストップウォッチを開始する。
     */
    public void start() {
        startTime = System.currentTimeMillis();
        endTime = startTime;
    }

    /**
     * ストップウォッチを停止し、時間を取得する。
     */
    public long end() {
        endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * {@link #start()}を呼び出した後、一度でも
     * {@link #end()}を呼び出したらtrue
     */
    public boolean isEnd() {
        return endTime != startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
