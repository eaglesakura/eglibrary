package com.eaglesakura.lib.android.game.thread;

import com.eaglesakura.lib.android.game.util.GameUtil;

import android.os.Handler;
import android.os.Looper;

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
     */
    public static void postUI(Runnable runnable) {
        getInstance().post(runnable);
    }

    /**
     * 指定したディレイをかけてPOSTする
     */
    public static void postDelayedUI(Runnable runnable, long delay) {
        getInstance().postDelayed(runnable, delay);
    }

    /**
     * UIスレッドにPOSTし、実行終了を待つ
     */
    public static void postWithWait(final Runnable runnable) {
        if (GameUtil.isUIThread()) {
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
}
