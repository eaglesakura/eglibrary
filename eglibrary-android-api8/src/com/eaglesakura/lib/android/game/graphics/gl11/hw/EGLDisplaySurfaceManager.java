package com.eaglesakura.lib.android.game.graphics.gl11.hw;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.view.SurfaceHolder;

import com.eaglesakura.lib.android.game.resource.DisposableResource;

/**
 * Displayから生成したSurfaceを管理する
 * @author TAKESHI YAMASHITA
 *
 */
class EGLDisplaySurfaceManager extends DisposableResource {

    EGLSurface surface;

    EGLManager eglManager;

    public EGLDisplaySurfaceManager(EGLManager eglManager) {
        this.eglManager = eglManager;

        final EGL10 egl = eglManager.egl;
        final EGLDisplay eglDisplay = eglManager.display;
        final EGLContextManager context = eglManager.context;
        final SurfaceHolder surface = eglManager.holder;

        this.surface = egl.eglCreateWindowSurface(eglDisplay, context.getConfig(), surface, null);
    }

    /**
     * レンダリング用サーフェイスを取得する
     * @return
     */
    public EGLSurface getSurface() {
        return surface;
    }

    @Override
    public void dispose() {
        final EGL10 egl = eglManager.egl;
        final EGLDisplay eglDisplay = eglManager.display;
        egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(eglDisplay, surface);

        surface = null;
    }
}
