package com.eaglesakura.lib.android.game.graphics.gl11.hw;

/**
 * シンプルなGL処理を行う。
 * rendering/working共にrun()を内部で呼び出す。
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class SimpleRenderer implements GLRenderer, Runnable {

    @Override
    public void onRendering(EGLManager egl) {
        run();
    }

    @Override
    public void onWorking(EGLManager egl) {
        run();
    }

    @Override
    public void onSurfaceReady(EGLManager egl) {
        // 何もしない
    }
}
