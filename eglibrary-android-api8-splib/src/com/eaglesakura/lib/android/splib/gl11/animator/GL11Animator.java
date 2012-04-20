package com.eaglesakura.lib.android.splib.gl11.animator;

import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;
import com.eaglesakura.lib.android.splib.fragment.GL11Fragment.GLRunnable;

public abstract class GL11Animator implements GLRunnable {
    GL11Fragment fragment;

    /**
     * delayは30fps程度を基本とする
     */
    int delay = 1000 / 30;

    boolean started = false;

    public GL11Animator(GL11Fragment fragment) {
        this.fragment = fragment;
    }

    /**
     * アニメーションを開始する。
     */
    public final void start() {
        started = true;
        fragment.post(this);
    }

    @Override
    public final void run() {
        if (!doAnimation(fragment)) {
            fragment.postDelayed(this, delay);
        }
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
