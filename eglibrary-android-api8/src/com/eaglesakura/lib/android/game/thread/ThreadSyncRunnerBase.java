package com.eaglesakura.lib.android.game.thread;

import android.os.Handler;

import com.eaglesakura.lib.android.game.util.GameUtil;

/**
 * 何らかの事情で別スレッドで特定処理を行わせる必要がある場合に利用するヘルパ。
 * UIスレッド実行まで待つ、GLスレッド実行まで待つ等の用途に利用
 * @author Takeshi
 *
 */
public abstract class ThreadSyncRunnerBase<T> {

    Handler handler;
    /**
     * 戻り値を一時的に格納する
     */
    T result = null;

    /**
     * 投げられた例外を一時的に格納する。
     */
    Exception exception = null;

    /**
     * 別スレッドでの処理が終了したらtrueとなる。
     */
    boolean finish = false;

    /**
     * 
     * @param targetHandler 実行対象スレッドのハンドラ
     */
    public ThreadSyncRunnerBase(Handler targetHandler) {
        if (Thread.currentThread().equals(targetHandler.getLooper().getThread())) {
            //! 呼び出しスレッドと対象スレッドが同じため、ロックが発生してしまう
            throw new IllegalArgumentException("target is current thread!!");
        }
        this.handler = targetHandler;
    }

    /**
     * 別スレッドで実行を行い、実行が終了するまで待つ。
     * @return
     */
    public T run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    result = onOtherThreadRun();
                } catch (Exception e) {
                    exception = e;
                }
                finish = true;
            }
        });

        /**
         * 終了まで待つ
         */
        while (!finish) {
            GameUtil.sleep(1);
        }

        if (exception != null && exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }

        return result;
    }

    /**
     * 指定スレッドでの実行を行う。
     * @return
     */
    public abstract T onOtherThreadRun() throws Exception;

    /**
     * 別スレッドで例外が投げられた場合、例外を取得する。
     * 正常終了している場合、nullを返す。
     * RuntimeExceptionの場合は例外のthrowを代行する。
     * @return
     */
    public Exception getException() {
        return exception;
    }
}
