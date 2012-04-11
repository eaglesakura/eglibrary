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
     * UIハンドラにpostする
     */
    public AsyncAction() {
        this.handler = UIHandler.getInstance();
    }

    /**
     * {@link #onBackgroundAction()}の実行直前に呼び出される。
     */
    protected void onPreExecute() throws Exception {

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

    /**
     * 終了作業を行わせる。
     */
    protected void onFinalize() {

    }

    @Override
    public void run() {
        try {
            (new ThreadSyncRunnerBase<Void>(handler) {
                @Override
                public Void onOtherThreadRun() throws Exception {
                    onPreExecute();
                    return null;
                }
            }).run();

            final Object obj = onBackgroundAction();

            (new ThreadSyncRunnerBase<Void>(handler) {
                @Override
                public Void onOtherThreadRun() throws Exception {
                    onSuccess(obj);
                    return null;
                }
            }).run();
        } catch (final Exception e) {
            (new ThreadSyncRunnerBase<Void>(handler) {
                @Override
                public Void onOtherThreadRun() throws Exception {
                    onFailure(e);
                    return null;
                }
            }).run();
        }

        (new ThreadSyncRunnerBase<Void>(handler) {
            @Override
            public Void onOtherThreadRun() throws Exception {
                onFinalize();
                return null;
            }
        }).run();
    }
}
