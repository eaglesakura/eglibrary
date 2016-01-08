package com.eaglesakura.android.thread.loop;

import android.os.Handler;

import com.eaglesakura.android.thread.async.AsyncHandler;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.time.Timer;

/**
 * 指定のハンドラでループ処理を行うUtilクラス
 */
public abstract class HandlerLoopController {
    final private Handler handler;

    /**
     * フレームレート
     * 1未満の場合は2秒に１回等の処理を行うが、正確性は問わない
     */
    double frameRate = 60;

    boolean looping = false;

    /**
     * 解放済みの場合はtrue
     */
    boolean disposed = false;

    final protected Object lock = new Object();

    /**
     * ループ時間管理用タイマー
     */
    Timer timer = new Timer();

    /**
     * 前のフレームからの経過時間
     */
    double deltaTime = 1.0;

    public HandlerLoopController(Handler handler) {
        if (handler != null) {
            this.handler = handler;
        } else {
            this.handler = AsyncHandler.createInstance("HandlerLoopController");
        }
    }

    /**
     * フレームレートの設定
     *
     * @param frameRate
     */
    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
    }

    public double getFrameRate() {
        return frameRate;
    }

    /**
     * 処理を開始する
     */
    public void connect() {
        looping = true;
        handler.removeCallbacks(loopRunner);
        handler.post(loopRunner);
    }

    /**
     * 処理を終了する
     */
    public void disconnect() {
        looping = false;
        handler.removeCallbacks(loopRunner);
    }

    /**
     * 開放処理を行う
     */
    public void dispose() {
        if (handler != UIHandler.getInstance()) {
            handler.getLooper().quit();
        }
    }

    /**
     * このハンドラに処理を行わせる
     */
    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    /**
     * 前のフレームからのデルタ時間を取得する
     *
     * @return
     */
    public double getDeltaTime() {
        return deltaTime;
    }

    /**
     * 使用しているハンドラを取得する
     *
     * @return
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * 更新を行う
     */
    protected abstract void onUpdate();


    private Runnable loopRunner = new Runnable() {
        @Override
        public void run() {
            final long FRAME_TIME = (long) (1000.0 / frameRate); // 1フレームの許容時間
            // デルタ時間を計算
            long deltaMs = timer.end();
            if (deltaMs > 0) {
                deltaTime = (double) deltaMs / 1000.0f;
            } else {
                deltaTime = (double) FRAME_TIME / 1000.0f;
            }
            timer.start();
            // 更新を行わせる
            onUpdate();

            final long UPDATE_TIME = timer.end();
            if (looping) {
                handler.postDelayed(this, Math.max(1, FRAME_TIME - UPDATE_TIME));   // 1フレームにかけた時間を差し引いてpostする
            }
        }
    };
}
