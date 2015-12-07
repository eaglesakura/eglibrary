package com.eaglesakura.android.glkit.gl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.Choreographer;
import android.view.TextureView;

import com.eaglesakura.android.glkit.egl.IEGLContextGroup;
import com.eaglesakura.android.glkit.egl.IEGLManager;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.time.Timer;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 描画ループ等を構築する必要が有る場合に使用する
 */
public abstract class GLLoopStateManager extends GLProcessingManager {
    /**
     * このオブジェクトを管理しているオーナー
     * <br>
     * weak refが切れた時点でdestroyと同じ扱いとなる
     */
    protected WeakReference<Object> owner;

    /**
     * GLスレッドへ伝えるためのメッセージキュー
     * <br>
     * 構造をシンプルにするため、Run状態のみ実行される。
     * <br>
     * 実行は {@link #onLoopFrame()} を呼び出す直前になる。
     */
    private List<QueueDataHolder> messageQueue = new ArrayList<>();

    public enum LoopState {
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
     * 現在の描画ステート
     */
    protected LoopState loopState = null;

    private boolean vsyncEnable = false;

    /**
     * 実行中の自動gc間隔
     */
    private int gcTimeIntervalRunningMs = 1000 * 5;

    /**
     * 一時停止中の自動gc間隔
     */
    private int gcTimeIntervalPausingMs = 1000 * 30;

    protected GLLoopStateManager(Context context, IEGLManager eglManager) {
        super(context, eglManager);
    }

    /**
     * 処理の休止を行う
     */
    public void onPause() {
        loopState = LoopState.Pause;
    }

    /**
     * 処理のレジュームを行う
     */
    public void onResume() {
        loopState = LoopState.Run;
    }

    /**
     * 実行中の自動GC間隔を設定する
     * 負の値の場合、自動gcを行わない。
     *
     * @param gcTimeIntervalRunningMs
     */
    public void setGcTimeIntervalRunningMs(int gcTimeIntervalRunningMs) {
        this.gcTimeIntervalRunningMs = gcTimeIntervalRunningMs;
    }

    /**
     * 休止中の自動GC間隔を設定する
     * 負の値の場合、自動gcを行わない。
     *
     * @param gcTimeIntervalPausingMs
     */
    public void setGcTimeIntervalPausingMs(int gcTimeIntervalPausingMs) {
        this.gcTimeIntervalPausingMs = gcTimeIntervalPausingMs;
    }

    /**
     * 現在のレンダリング状態を取得する
     */
    public LoopState getLoopState() {
        return loopState;
    }

    public boolean isProcessingPaused() {
        return loopState == LoopState.Pause;
    }

    public boolean isProcessingDestroy() {
        return loopState == LoopState.Destroyed;
    }

    public boolean isProcessingRunning() {
        return loopState == LoopState.Run;
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
        this.owner = new WeakReference<>(owner);
    }

    /**
     * 生存期間を統一するためのオーナーオブジェクト
     *
     * @return
     */
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
     * GLスレッドで実行を行わせる
     *
     * @param runner
     */
    public void post(Runnable runner) {
        synchronized (messageQueue) {
            messageQueue.add(new QueueDataHolder(runner));
        }
    }

    /**
     * GLスレッドへデータを投げる
     *
     * @param what 要件
     * @param data データ本体
     */
    public void post(String what, Object data) {
        synchronized (messageQueue) {
            messageQueue.add(new QueueDataHolder(what, data, null));
        }
    }

    /**
     * GLスレッドへデータを送信し、終了時ハンドリングを行う
     *
     * @param what     要件
     * @param data     データ本体
     * @param listener
     */
    public void post(String what, Object data, MessageHandlingListener listener) {
        synchronized (messageQueue) {
            messageQueue.add(new QueueDataHolder(what, data, listener));
        }
    }

    @Override
    public void start() {
        super.start();
        loopState = LoopState.Run;
    }

    @Override
    public void dispose() {
        loopState = LoopState.Destroyed;
        super.dispose();
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

    /**
     * 処理ループを速やかに停止する
     */
    public void stop() {
        loopState = LoopState.Destroyed;
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
     * サーフェイスの生成待ちを行う
     *
     * @return サーフェイス生成に成功したらtrue
     */
    @Override
    protected boolean waitSurfaceCreated() {
        while (!device.hasSurface()) {
            // デバイスが生成されるまで待つ
            if (isProcessingDestroy()) {
                // 初期化前に廃棄命令が出されたから何もしない
                return false;
            }

            // sleep
            Util.sleep(1000 / 60);
        }

        return true;
    }

    /**
     * バックグラウンド処理を行う
     */
    @Override
    protected void onBackgroundProcessing() {
        if (!device.bind()) {
            throw new IllegalStateException("Device Bind Failed");
        }

        // 初期化イベントを発行する
        {
            LogUtil.log("call onLoopInitialize");
            onLoopInitialize();
        }

        int nowSurfaceHeight = 0;
        int nowSurfaceWidth = 0;
        int oldSurfaceHeight = 0;
        int oldSurfaceWidth = 0;
        LoopState lastState = null;

        final int SLEEP_TIME = 1000 / 15;   // 何らかの原因でsleepさせる場合の休止時間
        final Timer gcTimer = new Timer();

        // 廃棄命令があるまでループする
        while (!isProcessingDestroy()) {
            LoopState loopStartState = loopState;

            // bind継続チェック
            if (device.hasSurfaceDestroyRequest()) {
                // 一旦アンバインドして休止
                device.unbind();
                Util.sleep(SLEEP_TIME);
            } else if (device.hasSurface()) {
                if (loopStartState == LoopState.Run) {
                    if (!device.isBindedThread()) {
                        // バインドされていなければバインドを行う
                        device.bind();
                    }

                    // vsync待ちを行う
                    if (vsyncEnable) {
                        waitVsync();
                    }

                    if (lastState != LoopState.Run) {
                        // pauseから復旧したらresume
                        LogUtil.log("call onLoopResume");
                        onLoopResume();
                    }

                    // 解像度を取得
                    nowSurfaceWidth = device.getSurfaceWidth();
                    nowSurfaceHeight = device.getSurfaceHeight();

                    if (nowSurfaceWidth != oldSurfaceWidth || nowSurfaceHeight != oldSurfaceHeight) {
                        // 解像度に変化があったらイベントを発行
                        LogUtil.log("call onLoopSurfaceSizeChanged old(%dx%d) -> new(%dx%d)", oldSurfaceWidth, oldSurfaceHeight, nowSurfaceWidth, nowSurfaceHeight);
                        onLoopSurfaceSizeChanged(oldSurfaceWidth, oldSurfaceHeight, nowSurfaceWidth, nowSurfaceHeight);
                    }

                    // メッセージングの処理を行う
                    {
                        List<QueueDataHolder> datas;
                        synchronized (messageQueue) {
                            // データをコピーして、メンバからは廃棄してすぐさまロックを解除
                            // 同期時間を最低限にしておく
                            datas = new ArrayList<>(messageQueue);
                            messageQueue.clear();
                        }

                        // 処理の実行を行わせる
                        for (QueueDataHolder holder : datas) {
                            holder.execute();
                        }
                    }

                    // 毎フレーム処理
                    onLoopFrame();

                    // processを通ったら再描画リクエストは廃棄する
                    renderingRequest = false;

                    // 強制GCインターバル
                    if (gcTimeIntervalRunningMs > 0 && gcTimer.end() >= gcTimeIntervalRunningMs) {
                        Runtime.getRuntime().gc();
                        gcTimer.start();
                    }
                } else if (loopStartState == LoopState.Pause) {
                    if (renderingRequest) {
                        LogUtil.log("has RenderingRequest(%s)", toString());

                        device.bind();
                        LogUtil.log("call onLoopRequestRendering");
                        onLoopRequestRendering();
                        device.unbind();

                        renderingRequest = false;
                    }

                    if (lastState == LoopState.Run) {
                        LogUtil.log("call onLoopPaused");
                        onLoopPaused();
                    }

                    // デバイスは開放しておく
                    if (device.isBindedThread()) {
                        device.unbind();
                    }
                    Util.sleep(SLEEP_TIME);
                }

                // 現在のステートを保存する
                lastState = loopStartState;
                oldSurfaceWidth = nowSurfaceWidth;
                oldSurfaceHeight = nowSurfaceHeight;


                // 強制GCインターバル
                if (gcTimeIntervalPausingMs > 0 && gcTimer.end() >= gcTimeIntervalPausingMs) {
                    Runtime.getRuntime().gc();
                    gcTimer.start();
                }
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

        // 必要であれば、一時的なサーフェイス復帰を行う
        if (!device.isBinded() && !device.hasSurface()) {
            LogUtil.log("create stub surface");
            device.createPBufferSurface(1, 1);
        }

        // 終了処理を行わせる
        {
            device.bind();
            LogUtil.log("call onLoopFinish");
            onLoopFinish();
//            device.unbind();
        }
        // 処理を終了させる
        device.unbind();
    }

    /**
     * 処理の初期化を行わせる
     */
    protected abstract void onLoopInitialize();

    /**
     * 処理中にサーフェイスサイズが変更になったら呼び出される
     *
     * @param oldWidth
     * @param oldHeight
     * @param newWidth
     * @param newHeight
     */
    protected abstract void onLoopSurfaceSizeChanged(int oldWidth, int oldHeight, int newWidth, int newHeight);

    /**
     * 処理の一時停止を行う
     */
    protected abstract void onLoopPaused();

    /**
     * 処理の復帰を行う
     */
    protected abstract void onLoopResume();

    /**
     * 再描画のリクエストがあった
     */
    protected abstract void onLoopRequestRendering();

    /**
     * 処理ループの１フレーム処理を行わせる
     */
    protected abstract void onLoopFrame();

    /**
     * 処理ループの終了を行わせる
     */
    protected abstract void onLoopFinish();

    /**
     * メッセージキューを受信した際のハンドリングを行う
     *
     * @param what 要件
     * @param data データ本体
     */
    protected void onReceivedMessage(String what, Object data) {
        LogUtil.log("onReceivedMessage(%s)", what);
    }

    private class QueueDataHolder {
        Runnable runner;

        String what;

        Object data;

        MessageHandlingListener listener;

        public QueueDataHolder(Runnable runner) {
            this.runner = runner;
        }

        public QueueDataHolder(String what, Object data, MessageHandlingListener listener) {
            this.what = what;
            this.data = data;
            this.listener = listener;
        }

        void execute() {
            if (runner != null) {
                runner.run();
            } else if (data != null) {
                onReceivedMessage(what, data);
                if (listener != null) {
                    listener.onMessageHandleCompleted(what, data);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void initializeWindowDevice(Object windowSurface, IEGLContextGroup contextGroup) {
        super.initializeWindowDevice(windowSurface, contextGroup);
        if (Build.VERSION.SDK_INT >= 16 &&
                (windowSurface instanceof TextureView ||
                        windowSurface instanceof SurfaceTexture)) {
            // TextureViewはVSyncが取れないため、自前のVSyncを行う。
            vsyncCallbackImpl = new VsyncCallbackImpl();
            vsyncEnable = true;
            Choreographer.getInstance()
                    .postFrameCallback((Choreographer.FrameCallback) vsyncCallbackImpl);
        }
    }

    /**
     * 垂直同期待ちを行う。
     */
    public void waitVsync() {
        if (Build.VERSION.SDK_INT < 16 || vsyncCallbackImpl == null) {
            return;
        }

        synchronized (vsyncCallbackImpl) {
            try {
                vsyncCallbackImpl.wait(1000 / 61);
            } catch (Exception e) {

            }
        }
    }

    @SuppressLint("NewApi")
    class VsyncCallbackImpl implements Choreographer.FrameCallback {
        Choreographer choreographer = Choreographer.getInstance();

        @Override
        public void doFrame(long frameTimeNanos) {
            synchronized (this) {
                this.notifyAll();
            }
            if (device != null) {
                // TextureViewはVSyncが取れないため、自前のVSyncを行う。
                choreographer.postFrameCallback(this);
            }
        }
    }

    Object vsyncCallbackImpl;
}
