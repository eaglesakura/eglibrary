package com.eaglesakura.android.glkit.gl;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.Window;

import com.eaglesakura.android.glkit.egl.IEGLContextGroup;
import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.android.glkit.egl.IEGLManager;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

/**
 * GL処理管理を行う。
 * <br>
 * 処理は必ず別スレッドが用意されるため、もしUIスレッドで同期的な処理を行いたい場合は自前実装が必要になる。
 * <br>
 * 処理はウィンドウサーフェイスを持つ必要がなく、オフスクリーンレンダリングを可能とする。
 */
@SuppressWarnings("NewApi")
public abstract class GLProcessingManager {
    protected final IEGLManager eglManager;

    /**
     * Context
     */
    protected final Context context;

    /**
     * レンダリング用デバイス
     */
    protected IEGLDevice device;

    /**
     * GL処理用Thread
     */
    ProcessThread thread;

    /**
     * 描画リクエストを持っていたらtrue
     */
    boolean renderingRequest = false;

    public GLProcessingManager(Context context, IEGLManager eglManager) {
        this.eglManager = eglManager;
        this.context = context.getApplicationContext();
    }

    /**
     * オフスクリーンレンダリング用EGLDeviceを生成する
     *
     * @param surfaceWidth
     * @param surfaceHeight
     */
    public void initializeOffscreenDevice(int surfaceWidth, int surfaceHeight, IEGLContextGroup contextGroup) {
        if (surfaceWidth < 1 || surfaceHeight < 1) {
            throw new IllegalArgumentException("surfaceWidth < 1 || surfaceHeight < 1");
        }

        device = eglManager.newDevice(contextGroup);
        device.createPBufferSurface(surfaceWidth, surfaceHeight);
    }

    /**
     * オフスクリーンレンダリング用EGLDeviceを生成する
     */
    public void initializeOffscreenDevice(IEGLContextGroup contextGroup) {
        initializeOffscreenDevice(1, 1, contextGroup);
    }

    /**
     * EGL管理クラスを取得する
     *
     * @return
     */
    public IEGLManager getEglManager() {
        return eglManager;
    }

    /**
     * 処理用デバイスを取得する
     *
     * @return
     */
    public IEGLDevice getDevice() {
        return device;
    }

    public AssetManager getAssets() {
        return context.getAssets();
    }

    /**
     * 画面に反映可能なEGLDeviceを生成する
     *
     * @param windowSurface 描画対象ウィンドウサーフェイス
     * @param contextGroup  EGLContext共有グループ
     */
    public void initializeWindowDevice(Object windowSurface, IEGLContextGroup contextGroup) {
        if (windowSurface instanceof SurfaceView) {
            windowSurface = ((SurfaceView) windowSurface).getHolder();
        } else if (windowSurface instanceof Activity) {
            windowSurface = ((Activity) windowSurface).getWindow();
        }

        if (windowSurface instanceof SurfaceHolder) {
            LogUtil.log("createSurface / SurfacHolder");
            // for SurfaceView/SurfaceHolder
            SurfaceHolder holder = (SurfaceHolder) windowSurface;
            holder.setFormat(PixelFormat.RGBA_8888);
            holder.addCallback(new SurfaceHolderCallback2Impl());
        } else if (windowSurface instanceof Window) {
            LogUtil.log("createSurface / Window");
            // for ActivityDirect
            ((Window) windowSurface).takeSurface(new SurfaceHolderCallback2Impl());
        } else if (windowSurface instanceof TextureView) {
            LogUtil.log("createSurface / TextureView");
            // for TextureView
            ((TextureView) windowSurface).setSurfaceTextureListener(new SurfaceTextureListenerImpl());
        } else {
            throw new IllegalArgumentException(String.format("not support(%s)", windowSurface.getClass().getName()));
        }

        device = eglManager.newDevice(contextGroup);
    }

    /**
     * 廃棄処理を行う
     */
    public void dispose() {
        // 停止待ちを行う
        join();

        if (device != null) {
            device.dispose();
            device = null;
        }
    }

    /**
     * Thread名を生成する
     *
     * @return
     */
    protected String getThreadName() {
        return String.format("GLProc(%d)", thread.hashCode());
    }

    /**
     * 新規にThreadを作成して描画を開始する。start直後はresume済みとして扱う。
     */
    public void start() {
        if (thread != null) {
            throw new IllegalStateException("processing started");
        }
        thread = new ProcessThread();
        thread.setName(getThreadName());
        thread.start();
    }

    /**
     * Threadの処理完了待ちを行う
     */
    public void join() {
        if (thread == null) {
            return;
        }

        try {
            thread.join();
            thread = null;
        } catch (Exception e) {
        }
    }


    /**
     * 自身のThread停止と解放を行う
     */
    public void disposeSelfFromBackground() {
        if (!Thread.currentThread().equals(thread)) {
            throw new IllegalStateException("isUIThread");
        }

        UIHandler.postUI(new Runnable() {
            @Override
            public void run() {
                dispose();
            }
        });
    }

    /**
     * 裏スレッドでの描画を行う。
     * <br>
     * 呼び出しを行った時点でEGLDeviceがバインド可能であることを保証するが、その後廃棄される等の事情は加味されない。
     */
    protected abstract void onBackgroundProcessing();


    /**
     * サーフェイスの生成待ちを行う
     *
     * @return サーフェイス生成に成功したらtrue
     */
    protected boolean waitSurfaceCreated() {
        while (!device.hasSurface()) {
            // sleep
            Util.sleep(1000 / 60);
        }
        return true;
    }

    /**
     * 描画スレッド
     */
    class ProcessThread extends Thread {
        @Override
        public void run() {
            // サーフェイスの生成待ちを行う
            if (!waitSurfaceCreated()) {
                // 生成に失敗したら何もしない
                return;
            }
            onBackgroundProcessing();
        }
    }

    /**
     * for SurfaceHolder
     */
    class SurfaceHolderCallback2Impl implements SurfaceHolder.Callback2 {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (device != null) {
                device.onSurfaceChanged(holder, width, height);
            }
        }

        @Override
        public void surfaceRedrawNeeded(SurfaceHolder holder) {
            renderingRequest = true;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (device != null) {
                device.onSurfaceDestroyed();
            }
        }
    }

    /**
     * for TextureView
     */
    class SurfaceTextureListenerImpl implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            device.onSurfaceChanged(surface, width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (device != null) {
                device.onSurfaceChanged(surface, width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (device != null) {
                device.onSurfaceDestroyed();
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

}
