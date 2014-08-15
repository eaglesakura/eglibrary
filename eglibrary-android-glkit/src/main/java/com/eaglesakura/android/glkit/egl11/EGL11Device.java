package com.eaglesakura.android.glkit.egl11;

import android.opengl.GLES20;

import com.eaglesakura.android.glkit.GLKitUtil;
import com.eaglesakura.android.glkit.egl.GLESVersion;
import com.eaglesakura.android.glkit.egl.IEGLContextGroup;
import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.util.LogUtil;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static javax.microedition.khronos.egl.EGL10.*;

/**
 * デバイス
 */
public class EGL11Device implements IEGLDevice {
    /**
     * 所属しているコントローラー
     */
    final EGL11Manager controller;

    /**
     * 所属しているContextGroup
     */
    final EGL11ContextGroup contextGroup;

    /**
     * sync
     */
    final Object surfaceLock = new Object();

    /**
     * 描画用のウィンドウ
     */
    Object nativeWindow;

    /**
     * 使用しているEGLContext
     */
    EGLContext context;

    /**
     * 使用しているEGLSurface
     */
    EGLSurface surface;

    /**
     * サーフェイス幅
     */
    int surfaceWidth;

    /**
     * サーフェイス高
     */
    int surfaceHeight;

    /**
     * 廃棄リクエストを持っている場合はtrue
     */
    boolean surfaceDestroyRequest = false;

    /**
     * Bindしているスレッド
     */
    Thread bindedThread;

    Lock bindLock = new ReentrantLock();

    EGL11Device(EGL11Manager controller, EGL11ContextGroup group) {
        this.controller = controller;
        this.contextGroup = group;
        this.context = group.createContext();
    }

    void destroySurface() {
        synchronized (surfaceLock) {
            if (hasSurface()) {
                EGL10 egl = controller.egl;
                EGLDisplay display = controller.display;

                egl.eglDestroySurface(display, surface);
                LogUtil.log("eglDestroySurface(%s)", surface.toString());
                surface = null;
                surfaceWidth = 0;
                surfaceHeight = 0;
                nativeWindow = null;
            }
        }
    }

    void destroyContext() {
        if (hasContext()) {
            contextGroup.destroyContext(context);
            context = null;
        }
    }

    public boolean hasContext() {
        synchronized (surfaceLock) {
            return context != null && context != EGL_NO_CONTEXT;
        }
    }

    /**
     * レンダリング対象として有効なEGLSurfaceを持っていればtrue
     *
     * @return
     */
    @Override
    public boolean hasSurface() {
        return surface != null && surface != EGL_NO_SURFACE;
    }

    /**
     * オフスクリーンレンダリング用のサーフェイスを生成する。
     * 既にサーフェイスが存在する場合は開放を行う
     *
     * @param width  サーフェイスの幅ピクセル数
     * @param height サーフェイスの高さピクセル数
     */
    @Override
    public void createPBufferSurface(int width, int height) {
        synchronized (surfaceLock) {
            destroySurface();

            EGL10 egl = controller.egl;
            EGLDisplay display = controller.display;
            EGLConfig config = controller.config;
            egl.eglCreatePbufferSurface(display, config, new int[]{
                    EGL_WIDTH, width,
                    EGL_HEIGHT, height,
                    EGL_NONE,
            });
        }
    }

    @Override
    public boolean bind() {
        if (!hasSurface() || isBinded()) {
            // surfaceを持たないならバインドも成功しない
            return false;
        }

        EGL10 egl = controller.egl;
        EGLDisplay display = controller.display;

        if (egl.eglMakeCurrent(display, surface, surface, context)) {
            bindedThread = Thread.currentThread();
            bindLock.lock();
            return true;
        } else {
            GLKitUtil.printEglError(egl.eglGetError());
            return false;
        }
    }

    @Override
    public void unbind() {
        if (!isBindedThread()) {
            // バインドされているスレッドでないなら何も出来ない
            return;
        }
        // call glfinish
        GLES20.glFinish();

        EGL10 egl = controller.egl;
        EGLDisplay display = controller.display;
        if (egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)) {
            // どのThreadにも関連付けられていない
            bindedThread = null;
            bindLock.unlock();
        }
    }

    @Override
    public int getSurfaceWidth() {
        return surfaceWidth;
    }

    @Override
    public int getSurfaceHeight() {
        return surfaceHeight;
    }

    /**
     * 開放を行う
     */
    @Override
    public void dispose() {
        try {
            surfaceDestroyRequest = true;
            bindLock.lock();

            destroySurface();   // サーフェイスの開放
            destroyContext();   // Contextの開放

            controller.onDestroyDevice(this);
        } finally {
            surfaceDestroyRequest = false;
            bindLock.unlock();
        }


    }

    /**
     * 描画用のWindowを持っているならtrue
     */
    @Override
    public boolean isWindowDevice() {
        return nativeWindow != null;
    }

    /**
     * 管理しているContextGroupを取得する
     */
    @Override
    public IEGLContextGroup getContextGroup() {
        return contextGroup;
    }

    /**
     * いずれかのスレッドにバインドされていたらtrue
     */
    @Override
    public boolean isBinded() {
        return bindedThread != null;
    }

    @Override
    public boolean isBindedThread() {
        return Thread.currentThread().equals(bindedThread);
    }


    /**
     * 廃棄リクエストを持っているならばtrue
     * <p/>
     * trueの場合、速やかにunbindとdestroyを行わなければならない
     *
     * @return
     */
    @Override
    public boolean hasSurfaceDestroyRequest() {
        return surfaceDestroyRequest;
    }

    /**
     * サーフェイスサイズが変更された
     *
     * @param native_window 　描画対象のウィンドウ
     * @param newWidth      サーフェイス幅
     * @param newHeight     サーフェイス高さ
     */
    @Override
    public void onSurfaceChanged(Object native_window, int newWidth, int newHeight) {
        native_window = GLKitUtil.getEglNativeWindow(native_window);
        if (native_window == null) {
            throw new IllegalArgumentException("native_window != WindowSurface");
        }

        synchronized (surfaceLock) {
            try {
                surfaceDestroyRequest = true;
                bindLock.lock();

                // サーフェイスを開放する
                destroySurface();

                EGL10 egl = controller.egl;
                EGLDisplay display = controller.display;
                EGLConfig config = controller.config;
                GLESVersion version = controller.getGLESVersion();

                // コンテキストが未生成であれば生成する
                if (context == null) {
                    context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, version.getContextAttribute());
                    if (context == EGL10.EGL_NO_CONTEXT) {
                        GLKitUtil.printEglError(egl.eglGetError());
                        throw new IllegalStateException("eglCreateContext fail...");
                    }
                }

                // 縦横サイズを保存する
                this.surfaceWidth = newWidth;
                this.surfaceHeight = newHeight;

                // 既存サーフェイスを廃棄する
                // サーフェイス縦横サイズが変更された時の処理
                destroySurface();

                // サーフェイスを生成する
                surface = egl.eglCreateWindowSurface(display, config, native_window, null);

                if (surface == EGL10.EGL_NO_SURFACE) {
                    GLKitUtil.printEglError(egl.eglGetError());
                    throw new IllegalStateException("eglCreateWindowSurface fail..");
                }

                nativeWindow = native_window;
            } finally {
                bindLock.unlock();
                surfaceDestroyRequest = false;
            }
        }
    }

    /**
     * サーフェイスが廃棄された
     */
    @Override
    public void onSurfaceDestroyed() {
        synchronized (surfaceLock) {
            try {
                surfaceDestroyRequest = true;
                bindLock.lock();

                // サーフェイスを開放する
                destroySurface();
            } finally {
                bindLock.unlock();
                surfaceDestroyRequest = false;
            }
        }
    }

    /**
     * eglSwapBuffersを呼び出す
     */
    @Override
    public void swapBuffers() {
        if (!isWindowDevice()) {
            throw new IllegalStateException(String.format("is not windowdevice(%s)", toString()));
        }

        if (!isBindedThread()) {
            return;
        }

        EGL10 egl = controller.egl;
        EGLDisplay display = controller.display;
        if (!egl.eglSwapBuffers(display, surface)) {
            GLKitUtil.printEglError(egl.eglGetError());
        }
    }
}
