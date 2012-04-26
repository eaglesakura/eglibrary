package com.eaglesakura.lib.android.splib.fragment.gl11;

import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;

/**
 * 自動でリトライを行うGLランナー
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class AutoRetryableGLRunnler implements GLRunnable {

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
}
