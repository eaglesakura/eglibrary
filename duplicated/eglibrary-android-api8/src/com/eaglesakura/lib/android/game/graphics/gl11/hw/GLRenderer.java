package com.eaglesakura.lib.android.game.graphics.gl11.hw;

public interface GLRenderer {

    /**
     * レンダリングを行わせる。
     * レンダリング中はEGLがロックされて、他のスレッドから利用することができなくなる。
     */
    void onRendering(EGLManager egl);

    /**
     * テクスチャの読み込み等、GLに関する動作を行わせる。
     * 実行中はEGLがロックされて、他のスレッドから利用することができなくなる。
     */
    void onWorking(EGLManager egl);

    /**
     * onRendering()が呼び出される直前に呼び出される。
     */
    void onSurfaceReady(EGLManager egl);

    /**
     * レンダリング時にサーフェイスの準備ができていなかった場合に呼び出される。
     */
    void onSurfaceNotReady(EGLManager egl);
}
