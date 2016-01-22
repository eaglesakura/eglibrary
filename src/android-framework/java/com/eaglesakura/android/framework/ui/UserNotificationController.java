package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.thread.ui.UIHandler;

import android.content.Context;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activityが表示されている間に使用される共通UIを定義する
 */
public class UserNotificationController {
    protected AtomicInteger progressNum = new AtomicInteger(0);

    protected NotificationListener listener;

    protected Context context;

    public UserNotificationController(Context context, NotificationListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void setListener(NotificationListener listener) {
        this.listener = listener;
    }

    /**
     * 何らかの処理中であればtrue
     */
    public boolean isProgressing() {
        return progressNum.get() > 0;
    }

    /**
     * 処理を開始する
     */
    public void pushProgress(final Object sender, final String message) {
        final int prg = progressNum.incrementAndGet();
        if (listener == null) {
            return;
        }
        UIHandler.postUIorRun(new Runnable() {
            @Override
            public void run() {
                if (prg == 1) {
                    // 初回起動
                    listener.onVisibleProgress(UserNotificationController.this, sender, message);
                } else {
                    // 二度目以降
                    listener.onUpdateProgress(UserNotificationController.this, prg, sender, message);
                }
            }
        });
    }

    /**
     * 処理を終了する
     */
    public void popProgress(final Object sender) {
        int prg = progressNum.decrementAndGet();
        if (listener != null && prg == 0) {
            UIHandler.postUIorRun(new Runnable() {
                @Override
                public void run() {
                    listener.onDismissProgress(UserNotificationController.this, sender);
                }
            });
        }
    }

    public interface NotificationListener {
        boolean onVisibleProgress(UserNotificationController controller, Object sender, String message);

        boolean onUpdateProgress(UserNotificationController controller, int refs, Object sender, String message);

        boolean onDismissProgress(UserNotificationController controller, Object sender);
    }
}
