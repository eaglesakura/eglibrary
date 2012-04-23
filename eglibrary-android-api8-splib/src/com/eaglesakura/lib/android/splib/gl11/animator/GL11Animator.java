package com.eaglesakura.lib.android.splib.gl11.animator;

import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;
import com.eaglesakura.lib.android.splib.fragment.GL11Fragment.GLRunnable;

public abstract class GL11Animator implements GLRunnable {
    GL11Fragment fragment;

    /**
     * delayは30fps程度を基本とする
     */
    int delay = 1000 / 30;

    /**
     * 開始済みだったらtrueを設定する
     */
    boolean started = false;

    public GL11Animator(GL11Fragment fragment) {
        this.fragment = fragment;
    }

    /**
     * アニメーションを開始する。
     */
    public final void start() {
        if (started) {
            return;
        }
        started = true;
        fragment.post(this);
    }

    /**
     * 実行を行わせる。
     * 外部からの呼び出し用で、直接は実行しない。
     */
    @Override
    public final void run() {
        if (!doAnimation(fragment)) {
            fragment.postDelayed(this, delay);
        }
    }

    /**
     * 開始済みの場合trueを返す。
     * @return
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * アニメーションを行わせる。
     * 完了したらtrueを返す。
     * @return
     */
    protected abstract boolean doAnimation(GL11Fragment fragment);

    @Override
    public void onError(Error error, GL11Fragment fragment) {
        switch (error) {
            case Disposed:
                break;
            case NotInitialized:
                fragment.postDelayed(this, 500);
                break;
            case Paused:
                fragment.postDelayed(this, 500);
                break;
            default:
                break;
        }
    }

}
