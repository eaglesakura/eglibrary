package com.eaglesakura.lib.android.game.thread;

import android.os.Handler;

/**
 * 非同期の処理を行う。<BR>
 * こちらはAsyncTaskとは独立しているため、強制的な実行が可能。<BR>
 * Handlerの指定も可能。
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class AsyncAction extends Thread {
    Handler handler;

    /**
     * 
     * @param handler
     */
    public AsyncAction(Handler handler) {
        this.handler = handler;
    }

    /**
     * バックグラウンドスレッドで呼び出される。
     */
    protected abstract Object onBackgroundAction() throws Exception;

    /**
     * 実行に成功した場合に呼び出される。
     * @param object
     */
    protected abstract void onSuccess(Object object);

    /**
     * 実行に失敗したため、エラーハンドルを行わせる。
     * @param exception
     */
    protected abstract void onFailure(Exception exception);

    @Override
    public void run() {
        try {
            final Object obj = onBackgroundAction();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(obj);
                }
            });
        } catch (final Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onFailure(e);
                }
            });
        }
    }
}
