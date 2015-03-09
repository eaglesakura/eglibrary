package com.eaglesakura.android.thread;

import com.eaglesakura.time.Timer;
import com.eaglesakura.util.Util;

/**
 * シンプルにループを記述する
 */
public abstract class SimpleLoopController {
    Thread loopThread;

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

    final String threadName;

    public SimpleLoopController(String threadName) {
        this.threadName = threadName;
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
        if (loopThread != null) {
            throw new IllegalStateException();
        }
        looping = true;
        loopThread = new Thread() {
            @Override
            public void run() {
                threadLoop();
            }
        };
        loopThread.setName(threadName);
        loopThread.start();
    }

    /**
     * 処理を終了する
     */
    public void disconnect() {
        looping = false;

        if (loopThread != null) {
            try {
                loopThread.join();
            } catch (Exception e) {
            }
            loopThread = null;
        }
    }

    /**
     * 開放処理を行う
     */
    public void dispose() {
        disconnect();
    }

    /**
     * 処理を行わせる
     */
    public void post(Runnable runnable) {
        throw new UnsupportedOperationException();
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
     * 更新を行う
     */
    protected abstract void onUpdate();

    private void threadLoop() {
        final long FRAME_TIME = (long) (1000.0 / frameRate); // 1フレームの許容時間
        long nextUpdateTime = 0;
        while (looping) {
            final long currentTimeMs = System.currentTimeMillis();
            if (currentTimeMs > nextUpdateTime) {
                nextUpdateTime = currentTimeMs + FRAME_TIME;
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
            } else {
                Util.sleep(1);
            }
        }
    }

}
