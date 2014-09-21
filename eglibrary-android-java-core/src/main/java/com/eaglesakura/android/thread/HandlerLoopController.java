package com.eaglesakura.android.thread;

import android.os.Handler;

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
     * 処理を行わせる
     */
    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    /**
     * 更新を行う
     */
    protected abstract void onUpdate();


    private Runnable loopRunner = new Runnable() {
        @Override
        public void run() {
            // 所定の時間の場合
            onUpdate();
            if (looping) {
                handler.postDelayed(this, (long) (1000.0 / frameRate));
            }
        }
    };
}
