package com.eaglesakura.lib.android.splib.fragment.gl11;

import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;

/**
 * GLスレッドでの実行を行わせる。
 * onErrorが呼び出された場合、
 * {@link #run()}は実行されない。
 * @author TAKESHI YAMASHITA
 *
 */
public interface GLRunnable extends Runnable {
    public enum GLError {
        /**
         * 初期化されていない
         */
        NotInitialized,

        /**
         * 既にGLが廃棄されている。
         */
        Disposed,

        /**
         * GLが休止状態にある。
         */
        Paused,
    }

    public void onError(GLError error, GL11Fragment fragment);
}
