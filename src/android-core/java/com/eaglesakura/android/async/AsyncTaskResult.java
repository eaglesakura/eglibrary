package com.eaglesakura.android.async;

import com.eaglesakura.android.async.error.TaskCanceledException;
import com.eaglesakura.android.async.error.TaskException;
import com.eaglesakura.android.async.error.TaskFailedException;
import com.eaglesakura.android.async.error.TaskTimeoutException;
import com.eaglesakura.util.LogUtil;

public final class AsyncTaskResult<T> {
    private final AsyncTaskController controller;

    AsyncTaskResult(AsyncTaskController pipeline) {
        this.controller = pipeline;
    }

    /**
     * 実行対象のタスク
     */
    IAsyncTask<T> task;

    T result;

    Exception error;

    /**
     * リスナ
     */
    Listener<T> listener;

    /**
     * キャンセル状態であればtrue
     */
    boolean canceled;

    private final Object awaitLock = new Object();

    private final Object resultLock = new Object();

    /**
     * 実行をキャンセルする
     */
    public void cancel() {
        this.canceled = true;
    }

    /**
     * キャンセルされていればtrue
     *
     * @return
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * リスナを設定する
     *
     * @param listener
     */
    public AsyncTaskResult<T> setListener(Listener<T> listener) {
        synchronized (resultLock) {
            // 既にタスクが完了してしまっている場合はリスナをコールさせてローカルに残さない
            if (isTaskFinished()) {
                handleListener(listener);
            } else {
                this.listener = listener;
            }
        }
        return this;
    }

    /**
     * タスクの実行待ちを行う
     *
     * @param timeoutMs
     * @return
     * @throws Exception
     */
    public T await(long timeoutMs) throws TaskException {
        synchronized (awaitLock) {
            if (!isTaskFinished()) {
                try {
                    awaitLock.wait(timeoutMs);
                } catch (Exception e) {
                    LogUtil.log(e);
                }

                // 処理がタイムアウトした
                if (!isTaskFinished()) {
                    throw new TaskTimeoutException();
                }
            }
        }

        throwIfError();
        return result;
    }

    /**
     * タスクが成功、もしくは失敗・キャンセルしていたらtrue
     *
     * @return
     */
    public boolean isTaskFinished() {
        return result != null || error != null || canceled;
    }

    /**
     * タスクを実行しているコントローラを取得する
     *
     * @return
     */
    public AsyncTaskController getController() {
        return controller;
    }

    /**
     * 実行を行う
     */
    final void execute() {
        T result = null;
        Exception error = null;

        try {
            if (isCanceled()) {
                // 既にキャンセルされている
                throw new TaskCanceledException();
            }

            result = task.doInBackground(this);
        } catch (Exception e) {
            error = e;
        }

        synchronized (resultLock) {
            this.result = result;
            this.error = error;
            handleListener(this.listener);
            this.listener = null;
        }

        synchronized (awaitLock) {
            awaitLock.notifyAll();
        }
    }

    /**
     * リスナのハンドリングを行う
     */
    void handleListener(final Listener<T> callListener) {
        if (callListener == null) {
            return;
        }
        controller.taskHandler.request(new Runnable() {
            @Override
            public void run() {
                if (callListener != null) {
                    if (canceled) {
                        callListener.onTaskCanceled(AsyncTaskResult.this);
                    } else if (result != null) {
                        callListener.onTaskCompleted(AsyncTaskResult.this, result);
                    } else {
                        callListener.onTaskFailed(AsyncTaskResult.this, error);
                    }

                    callListener.onTaskFinalize(AsyncTaskResult.this);
                }
            }
        });
    }

    /**
     * エラーが発生していたら例外を投げ、それ以外は何もしない
     *
     * @throws Exception
     */
    void throwIfError() throws TaskException {
        if (error == null) {
            return;
        }
        throw new TaskFailedException(error);
    }

    public interface Listener<T> {
        /**
         * @param task
         * @param result
         */
        void onTaskCompleted(AsyncTaskResult<T> task, T result);

        /**
         * タスクがキャンセルされた場合に呼び出される
         *
         * @param task
         */
        void onTaskCanceled(AsyncTaskResult<T> task);

        /**
         * タスクが失敗した場合に呼び出される
         *
         * @param task
         * @param error
         */
        void onTaskFailed(AsyncTaskResult<T> task, Exception error);

        /**
         * タスクの完了時に必ず呼び出される
         *
         * @param task
         */
        void onTaskFinalize(AsyncTaskResult<T> task);
    }
}
