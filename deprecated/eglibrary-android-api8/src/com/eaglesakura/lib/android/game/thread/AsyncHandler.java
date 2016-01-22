package com.eaglesakura.lib.android.game.thread;

import com.eaglesakura.lib.android.game.util.Holder;

import android.os.Handler;
import android.os.Looper;

public class AsyncHandler extends Handler {
    Thread thread;

    public AsyncHandler() {
        super();
    }

    /**
     * ハンドラとスレッドを廃棄する。
     * これの呼び出し以降、ハンドラは正常に動作しない。
     */
    public void dispose() {
        try {
            boolean handlerThread = isHandlerThread();
            Thread thread = getLooper().getThread();
            getLooper().quit();
            if (!handlerThread) {
                thread.join();
            }
        } catch (Exception e) {

        }
    }

    /**
     * 所属しているスレッドを取得する。
     */
    public Thread getThread() {
        return getLooper().getThread();
    }

    /**
     * ハンドラと同じスレッドの場合はtrue
     */
    public boolean isHandlerThread() {
        return Thread.currentThread().equals(getThread());
    }

    /**
     * ハンドラを生成する。
     */
    public static AsyncHandler createInstance(String name) {
        final Holder<AsyncHandler> holder = new Holder<AsyncHandler>();
        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                holder.set(new AsyncHandler());
                Looper.loop();
            }
        };
        thread.setName(name);
        thread.start();
        return holder.getWithWait();
    }
}
