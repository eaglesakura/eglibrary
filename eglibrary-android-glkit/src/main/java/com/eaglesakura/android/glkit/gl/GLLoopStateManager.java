package com.eaglesakura.android.glkit.gl;

import android.content.Context;

import com.eaglesakura.android.glkit.egl.IEGLManager;
import com.eaglesakura.android.message.JointMessage;
import com.eaglesakura.android.message.MessageHandlingListener;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 描画ループ等を構築する必要が有る場合に使用する
 */
@JCClass(cppNamespace = "es.glkit")
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
     * @param message
     */
    public void post(JointMessage message) {
        synchronized (messageQueue) {
            messageQueue.add(new QueueDataHolder(message, null));
        }
    }

    /**
     * GLスレッドへデータを送信し、終了時ハンドリングを行う
     *
     * @param message
     * @param listener
     */
    public void post(JointMessage message, MessageHandlingListener listener) {
        synchronized (messageQueue) {
            messageQueue.add(new QueueDataHolder(message, listener));
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

        final int SLEEP_TIME = 1000 / 60;   // 何らかの原因でsleepさせる場合の休止時間

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
     * @param message
     */
    protected void onReceivedMessage(JointMessage message) {
        LogUtil.log("onReceivedMessage(%s)", message.getMessage());
    }

    private class QueueDataHolder {
        Runnable runner;

        JointMessage message;

        MessageHandlingListener listener;

        public QueueDataHolder(Runnable runner) {
            this.runner = runner;
        }

        public QueueDataHolder(JointMessage message, MessageHandlingListener listener) {
            this.message = message;
            this.listener = listener;
        }

        void execute() {
            if (runner != null) {
                runner.run();
            } else if (message != null) {
                onReceivedMessage(message);
                if (listener != null) {
                    listener.onMessageHandleCompleted(message);
                }
            }
        }
    }
}
