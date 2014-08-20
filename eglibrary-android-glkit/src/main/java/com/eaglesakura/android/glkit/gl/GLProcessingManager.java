package com.eaglesakura.android.glkit.gl;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.Window;

import com.eaglesakura.android.glkit.egl.IEGLContextGroup;
import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.android.glkit.egl.IEGLManager;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.jc.annotation.JCMethod;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import java.lang.ref.WeakReference;

/**
 * GL処理管理を行う。
 * <p/>
 * 処理は必ず別スレッドが用意されるため、もしUIスレッドで同期的な処理を行いたい場合は自前実装が必要になる。
 * <p/>
 * 処理はウィンドウサーフェイスを持つ必要がなく、オフスクリーンレンダリングを可能とする。
 */
public abstract class GLProcessingManager {
    protected final IEGLManager eglManager;

    protected final Context context;

    /**
     * 現在の描画ステート
     */
    protected ProcessingState processingState = null;

    /**
     * このオブジェクトを管理しているオーナー
     * <p/>
     * weak refが切れた時点でdestroyと同じ扱いとなる
     */
    protected WeakReference<Object> owner;

    public enum ProcessingState {
        /**
         * 描画中
         */
        Run,

        /**
         * 停止中
         */
        Pause,


        /**
         * 廃棄を行う
         */
        Destroyed,
    }

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
     * 画面に反映可能なEGLDeviceを生成する
     *
     * @param windowSurface 描画対象ウィンドウサーフェイス
     * @param contextGroup  EGLContext共有グループ
     */
    public void initializetWindowDevice(Object windowSurface, IEGLContextGroup contextGroup) {
        if (windowSurface instanceof SurfaceView) {
            windowSurface = ((SurfaceView) windowSurface).getHolder();
        } else if (windowSurface instanceof Activity) {
            windowSurface = ((Activity) windowSurface).getWindow();
        }

        if (windowSurface instanceof SurfaceHolder) {
            LogUtil.log("initialize / SurfacHolder");
            // for SurfaceView/SurfaceHolder
            SurfaceHolder holder = (SurfaceHolder) windowSurface;
            holder.setFormat(PixelFormat.RGBA_8888);
            holder.addCallback(new SurfaceHolderCallback2Impl());
        } else if (windowSurface instanceof Window) {
            LogUtil.log("initialize / Window");
            // for ActivityDirect
            ((Window) windowSurface).takeSurface(new SurfaceHolderCallback2Impl());
        } else if (windowSurface instanceof TextureView) {
            LogUtil.log("initialize / TextureView");
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
        if (device != null) {
            stop();

            device.dispose();
            device = null;
        }
    }

    /**
     * 処理の休止を行う
     */
    public void onPause() {
        processingState = ProcessingState.Pause;
    }

    /**
     * 処理のレジュームを行う
     */
    public void onResume() {
        processingState = ProcessingState.Run;
    }

    /**
     * 新規にThreadを作成して描画を開始する。start直後はresume済みとして扱う。
     */
    public void start() {
        // 既存プロセスを止める
        stop();

        processingState = ProcessingState.Run;
        thread = new ProcessThread();
        thread.setName(String.format("GLProc(%d)", thread.hashCode()));
        thread.start();
    }

    /**
     * レンダリングスレッドを速やかに停止する
     */
    public void stop() {
        processingState = ProcessingState.Destroyed;
        if (thread == null) {
            return;
        }

        try {
            thread.join();
        } catch (Exception e) {
            LogUtil.log(e);
        }

        thread = null;
    }

    /**
     * 現在のレンダリング状態を取得する
     */
    public ProcessingState getProcessingState() {
        return processingState;
    }

    public boolean isProcessingPaused() {
        return processingState == ProcessingState.Pause;
    }

    public boolean isProcessingDestroy() {
        return processingState == ProcessingState.Destroyed;
    }

    public boolean isProcessingRunning() {
        return processingState == ProcessingState.Run;
    }

    /**
     * 所有者となるオブジェクトを指定する
     *
     * @param owner
     */
    public void setOwner(Object owner) {
        if (owner == null) {
            throw new NullPointerException("owner == null");
        }
        this.owner = new WeakReference<Object>(owner);
    }

    public boolean hasOwner() {
        return owner != null;
    }

    /**
     * オーナークラスが死んでいたらtrue
     *
     * @return
     */
    public boolean isOwnerDestroyed() {
        if (owner != null && owner.get() == null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 自身のスレッドを停止させる
     */
    protected void stopSelfFromBackground() {
        if (!Thread.currentThread().equals(thread)) {
            throw new IllegalStateException("isUIThread");
        }

        UIHandler.postUI(new Runnable() {
            @Override
            public void run() {
                stop();
            }
        });
    }

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
     * <p/>
     * 呼び出しを行った時点でEGLDeviceがバインド可能であることを保証するが、その後廃棄される等の事情は加味されない。
     */
    protected void onBackgroundProcessing() {
        if (!device.bind()) {
            throw new IllegalStateException("Device Bind Failed");
        }

        // 初期化イベントを発行する
        {
            LogUtil.log("call onProcessingInitialize");
            onProcessingInitialize();
        }

        int nowSurfaceHeight = 0;
        int nowSurfaceWidth = 0;
        int oldSurfaceHeight = 0;
        int oldSurfaceWidth = 0;
        ProcessingState lastState = ProcessingState.Run;

        final int SLEEP_TIME = 1000 / 60;   // 何らかの原因でsleepさせる場合の休止時間

        // 廃棄命令があるまでループする
        while (!isProcessingDestroy()) {
            // bind継続チェック
            if (device.hasSurfaceDestroyRequest()) {
                // 一旦アンバインドして休止
                device.unbind();
                Util.sleep(SLEEP_TIME);
            } else if (device.hasSurface()) {
                if (isProcessingRunning()) {
                    if (!device.isBindedThread()) {
                        // バインドされていなければバインドを行う
                        device.bind();
                    }

                    // 解像度を取得
                    nowSurfaceWidth = device.getSurfaceWidth();
                    nowSurfaceHeight = device.getSurfaceHeight();

                    if (lastState == ProcessingState.Pause) {
                        // pauseから復旧したらresume
                        LogUtil.log("call onProcessingResume");
                        onProcessingResume();
                    }

                    if (nowSurfaceWidth != oldSurfaceWidth || nowSurfaceHeight != oldSurfaceHeight) {
                        // 解像度に変化があったらイベントを発行
                        LogUtil.log("call onProcessingSurfaceSizeChanged old(%dx%d) -> new(%dx%d)", oldSurfaceWidth, oldSurfaceHeight, nowSurfaceWidth, nowSurfaceHeight);
                        onProcessingSurfaceSizeChanged(oldSurfaceWidth, oldSurfaceHeight, nowSurfaceWidth, nowSurfaceHeight);
                    }

                    // 毎フレーム処理
                    onProcessingLoopFrame();

                    // processを通ったら再描画リクエストは廃棄する
                    renderingRequest = false;
                } else if (isProcessingPaused()) {
                    if (renderingRequest) {
                        LogUtil.log("has RenderingRequest(%s)", toString());

                        device.bind();
                        LogUtil.log("call onProcessingRequestRendering");
                        onProcessingRequestRendering();
                        device.unbind();

                        renderingRequest = false;
                    }

                    if (lastState == ProcessingState.Run) {
                        LogUtil.log("call onProcessingPaused");
                        onProcessingPaused();
                    }

                    // デバイスは開放しておく
                    if (device.isBindedThread()) {
                        device.unbind();
                    }
                    Util.sleep(SLEEP_TIME);
                }

                // 現在のステートを保存する
                lastState = processingState;
                oldSurfaceWidth = nowSurfaceWidth;
                oldSurfaceHeight = nowSurfaceHeight;
            } else {
                Util.sleep(SLEEP_TIME);
            }

            // check owner
            if (isOwnerDestroyed()) {
                LogUtil.log("owner lost GL(%s)", toString());
                owner = null;
                disposeSelfFromBackground();
            }
        }

        // 終了処理を行わせる
        {
            device.bind();
            LogUtil.log("call onProcessingFinish");
            onProcessingFinish();
            device.unbind();
        }
        // 処理を終了させる
        device.unbind();
    }

    /**
     * 処理の初期化を行わせる
     */
    protected abstract void onProcessingInitialize();

    /**
     * 処理中にサーフェイスサイズが変更になったら呼び出される
     *
     * @param oldWidth
     * @param oldHeight
     * @param newWidth
     * @param newHeight
     */
    protected abstract void onProcessingSurfaceSizeChanged(int oldWidth, int oldHeight, int newWidth, int newHeight);

    /**
     * 処理の一時停止を行う
     */
    protected abstract void onProcessingPaused();

    /**
     * 処理の復帰を行う
     */
    protected abstract void onProcessingResume();

    /**
     * 再描画のリクエストがあった
     */
    protected abstract void onProcessingRequestRendering();

    /**
     * 処理ループの１フレーム処理を行わせる
     */
    protected abstract void onProcessingLoopFrame();

    /**
     * 処理ループの終了を行わせる
     */
    protected abstract void onProcessingFinish();

    /**
     * 描画スレッド
     */
    class ProcessThread extends Thread {
        @Override
        public void run() {
            while (!device.hasSurface()) {
                // デバイスが生成されるまで待つ
                if (isProcessingDestroy()) {
                    // 初期化前に廃棄命令が出されたから何もしない
                    return;
                }

                // sleep
                Util.sleep(1000 / 60);
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
