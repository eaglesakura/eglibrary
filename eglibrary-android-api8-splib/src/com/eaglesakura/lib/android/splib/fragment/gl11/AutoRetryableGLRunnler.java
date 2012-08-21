package com.eaglesakura.lib.android.splib.fragment.gl11;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.GLRenderer;

/**
 * 自動でリトライを行うGLランナー
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class AutoRetryableGLRunnler implements GLRenderer {

    /*
    @Override
    public void onError(GLError error, GL11Fragment fragment) {
        switch (error) {
            case Disposed:
                break;
            case NotInitialized:
                fragment.addSuspendQueue(this);
                break;
            case Paused:
                fragment.addSuspendQueue(this);
                break;
            default:
                break;
        }
    }
    */

    @Override
    public void onRendering(EGLManager egl) {
    }

    @Override
    public void onSurfaceNotReady(EGLManager egl) {
    }

    @Override
    public void onSurfaceReady(EGLManager egl) {

    }
}
