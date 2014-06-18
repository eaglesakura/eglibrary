package com.eaglesakura.lib.android.game.graphics.gl11;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.GLRenderer;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * EGLの実行を行う。
 * @author TAKESHI YAMASHITA
 *
 * @param <T>
 */
public abstract class EGLWorker<T> implements GLRenderer {
    T result;

    @Override
    public final void onRendering(EGLManager egl) {

    }

    @Override
    public final void onSurfaceReady(EGLManager egl) {

    }

    @Override
    public final void onSurfaceNotReady(EGLManager egl) {

    }

    /**
     * 戻り値を格納しておく
     */
    @Override
    public void onWorking(EGLManager egl) {
        try {
            result = onWork();
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    protected abstract T onWork() throws Exception;

    /**
     * 実行を行う
     * @param egl
     * @return
     */
    public final T execute(EGLManager egl) {
        egl.working(this);
        return result;
    }
}
