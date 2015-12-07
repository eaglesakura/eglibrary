package com.eaglesakura.android.thread;

import android.os.Handler;
import android.os.Looper;

import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.util.LogUtil;

/**
 * UIスレッド専用のハンドラ
 *
 * @author TAKESHI YAMASHITA
 */
public class UIHandler extends Handler {

    public UIHandler() {
        super(Looper.getMainLooper());
    }

    private static UIHandler instance = null;

    /**
     * 唯一のインスタンスを取得する。
     *
     * @return UIHandlerインスタンス
     */
    public static UIHandler getInstance() {
        if (instance == null) {
            instance = new UIHandler();
        }
        return instance;
    }

    /**
     * UIスレッドで実行を行わせる。
     *
     * @param runnable
     */
    public static void postUI(Runnable runnable) {
        getInstance().post(runnable);
    }

    /**
     * UIThreadにPostするか、UIThreadの場合はその場で実行する
     *
     * @param runnable
     */
    public static void postUIorRun(Runnable runnable) {
        if (AndroidUtil.isUIThread()) {
            runnable.run();
        } else {
            postUI(runnable);
        }
    }

    /**
     * 指定したディレイをかけてPOSTする
     *
     * @param runnable
     * @param delay
     */
    public static void postDelayedUI(Runnable runnable, long delay) {
        getInstance().postDelayed(runnable, delay);
    }

    /**
     * UIスレッドにPOSTし、実行終了を待つ
     * MEMO: デッドロックの可能性があるため、待合時間の設定を行えるバージョンを利用する。
     *
     * @param runnable
     */
    @Deprecated
    public static void postWithWait(final Runnable runnable) {
        if (AndroidUtil.isUIThread()) {
            runnable.run();
        } else {
            (new ThreadSyncRunnerBase<Void>(getInstance()) {
                @Override
                public Void onOtherThreadRun() throws Exception {
                    runnable.run();
                    return null;
                }
            }).run();
        }
    }

    /**
     * UIスレッドにPOSTし、実行終了を待つ
     *
     * @param runnable
     */
    public static void postWithWait(final Runnable runnable, long timeoutMs) {
        if (AndroidUtil.isUIThread()) {
            runnable.run();
        } else {
            final Object sync = new Object();

            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                    synchronized (sync) {
                        sync.notifyAll();
                    }
                }
            });

            synchronized (sync) {
                try {
                    sync.wait(timeoutMs);
                } catch (InterruptedException e) {
                    LogUtil.log(e);
                }
            }
        }
    }
}
