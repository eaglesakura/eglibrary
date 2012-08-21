package com.eaglesakura.lib.android.splib.egl.animator;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.util.Timer;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;
import com.eaglesakura.lib.android.splib.fragment.gl11.AutoRetryableGLRunnler;

public abstract class GL11Animator extends AutoRetryableGLRunnler {
    EGLFragment fragment;

    /**
     * delayは30fps程度を基本とする
     */
    int delay = 1000 / 30;

    /**
     * 開始済みだったらtrueを設定する
     */
    boolean started = false;

    /**
     * 
     */
    Timer timer = new Timer();

    public GL11Animator(EGLFragment fragment) {
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
        fragment.eglWork(this);
    }

    @Override
    public void onWorking(EGLManager egl) {
        if (!started) {
            return;
        }
        timer.start();
        if (!doAnimation(fragment)) {
            fragment.eglWorkDelayed(this, Math.max(1, delay - timer.end()));
        } else {
            started = false;
        }

    }

    /**
     * アニメーションを停止する
     */
    public void stop() {
        started = false;
    }

    /**
     * フレームレートを設定する
     * @param fps
     */
    public void setFrameRate(int fps) {
        delay = 1000 / fps;
    }

    /**
     * 開始済みの場合trueを返す。
     * @return
     */
    public boolean isAnimated() {
        return started;
    }

    /**
     * アニメーションを行わせる。
     * 完了したらtrueを返す。
     * @return
     */
    protected abstract boolean doAnimation(EGLFragment fragment);

}
