package com.eaglesakura.andorid.thread;

import android.os.Handler;

import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.time.Timer;

/**
 * 何らかの事情で別スレッドで特定処理を行わせる必要がある場合に利用するヘルパ。
 * UIスレッド実行まで待つ、GLスレッド実行まで待つ等の用途に利用
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class ThreadSyncRunnerBase<T> {

    Handler handler;
    /**
     * 戻り値を一時的に格納する
     */
    T result = null;

    /**
     * タイムアウトした場合true
     */
    boolean timeout = false;

    /**
     * 投げられた例外を一時的に格納する。
     */
    Exception exception = null;

    /**
     * 処理にかけていい最大時間
     */
    long maxTime = -1;

    Object lock = new Object();

    /**
     * 
     * @param targetHandler 実行対象スレッドのハンドラ
     */
    public ThreadSyncRunnerBase(Handler targetHandler) {
        if (AndroidUtil.isUIThread()) {
            maxTime = 1000 * 5;
        }

        this.handler = targetHandler;
    }

    /**
     * 別スレッドで実行を行い、実行が終了するまで待つ。
     * @return
     */
    public T run() {
        if (AndroidUtil.isHandlerThread(handler)) {
            // ハンドラと同一スレッドなら、そのまま実行させる
            try {
                result = onOtherThreadRun();
            } catch (Exception e) {
                exception = e;
            }
        } else {
            // 別スレッドなら、ハンドラにPOSTして実行待ちを行う
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        result = onOtherThreadRun();
                    } catch (Exception e) {
                        exception = e;
                    }
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            });

            final Timer timer = new Timer();

            synchronized (lock) {
                try {
                    lock.wait(maxTime);
                } catch (Exception e) {

                }
            }
            if (timer.end() >= maxTime) {
                timeout = true;
            }
        }

        if (exception != null && exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }

        return result;
    }

    /**
     * 処理がタイムアウトしたらtrue
     * @return
     */
    public boolean isTimeout() {
        return timeout;
    }

    /**
     * 処理にかけていい最大時間を指定する。
     * 0以下でタイムアウト無効
     * @param maxTime
     */
    public ThreadSyncRunnerBase<T> setMaxTime(long maxTime) {
        this.maxTime = maxTime;
        return this;
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
