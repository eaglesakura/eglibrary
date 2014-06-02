package com.eaglesakura.lib.android.splib.egl.animator;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.SimpleRenderer;

/**
 * 自動でリトライを行うレンダラ
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class AutoRetryableRenderer extends SimpleRenderer {
    protected EGLAnimator animator;

    public AutoRetryableRenderer(EGLAnimator animator) {
        this.animator = animator;
    }

    @Override
    public void onSurfaceNotReady(EGLManager egl) {
        animator.fragment.addPendingRunner(this);
    }
}
