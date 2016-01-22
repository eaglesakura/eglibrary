package com.eaglesakura.lib.android.game.util;

/**
 * 進捗状態を管理する。
 *
 * @author TAKESHI YAMASHITA
 */
public class Progress {

    /**
     * 進捗の最大値
     */
    double max;

    /**
     * 進捗の現在値
     */
    double progress;

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public void set(double progress, double max) {
        this.max = max;
        this.progress = progress;
    }

    public void set(int progress, int max) {
        this.max = max;
        this.progress = progress;
    }

    public void set(long progress, long max) {
        this.max = max;
        this.progress = progress;
    }

    /**
     * 処理が完了した場合true
     */
    public boolean isComplete() {
        return max > 0 && progress == max;
    }

    /**
     * 進捗を0.0f〜1.0fのウェイトで取得する。
     */
    public double getProgressLevel() {
        if (max == 0) {
            return 0; // 0 divを避ける
        }
        return progress / max;
    }

    /**
     * 進捗を0.0f〜1.0fのウェイトで取得する。
     */
    public float getProgressLevelFloat() {
        return (float) getProgressLevel();
    }

    /**
     * 進捗を0〜100.0のパーセントで取得する。
     */
    public double getProgressPercent() {
        return getProgressLevel() * 100;
    }

    /**
     * 進捗を0〜100のパーセントで取得する。
     * 小数点以下は切り捨て。
     */
    public int getProgressPercentInt() {
        return (int) getProgressPercent();
    }
}
