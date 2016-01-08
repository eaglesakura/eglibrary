package com.eaglesakura.android.thread.async;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 非同期実行用のハンドラ生成Util
 */
public class AsyncHandler extends Handler {
    final HandlerThread thread;

    public AsyncHandler(HandlerThread thread) {
        super(thread.getLooper());
        this.thread = thread;
    }

    /**
     * ハンドラとスレッドを廃棄する。
     * これの呼び出し以降、ハンドラは正常に動作しない。
     */
    public void dispose() {
        try {
            boolean handlerThread = isHandlerThread();
            thread.quit();
            if (!handlerThread) {
                thread.join();
            }
        } catch (Exception e) {

        }
    }

    /**
     * 所属しているスレッドを取得する。
     *
     * @return
     */
    public Thread getThread() {
        return getLooper().getThread();
    }

    /**
     * ハンドラと同じスレッドの場合はtrue
     *
     * @return
     */
    public boolean isHandlerThread() {
        return Thread.currentThread().equals(getThread());
    }

    /**
     * ハンドラを生成する。
     *
     * @return
     */
    public static AsyncHandler createInstance(String name) {
        HandlerThread thread = new HandlerThread(name);
        thread.start();
        return new AsyncHandler(thread);

//        final Holder<AsyncHandler> holder = new Holder<AsyncHandler>();
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                holder.set(new AsyncHandler());
//                Looper.loop();
//            }
//        };
//        thread.setName(name);
//        thread.start();
//        return holder.getWithWait();
    }
}
