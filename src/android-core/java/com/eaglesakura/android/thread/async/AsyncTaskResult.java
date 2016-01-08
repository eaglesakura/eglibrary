package com.eaglesakura.android.thread.async;

import com.eaglesakura.android.thread.async.error.TaskCanceledException;
import com.eaglesakura.android.thread.async.error.TaskException;
import com.eaglesakura.android.thread.async.error.TaskFailedException;
import com.eaglesakura.android.thread.async.error.TaskTimeoutException;
import com.eaglesakura.util.LogUtil;

public class AsyncTaskResult<T> {
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
    private boolean canceled;

    private CancelSignal cancelSignal;

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
     * cancel()されているか、CancelSignal.isCancelがtrueの場合キャンセルとなる
     *
     * @return
     */
    public boolean isCanceled() {
        if (cancelSignal != null && cancelSignal.isCanceled()) {
            return true;
        }
        return canceled;
    }

    /**
     * キャンセルチェック用のコールバックを指定する
     *
     * @param cancelSignal
     */
    public void setCancelSignal(CancelSignal cancelSignal) {
        this.cancelSignal = cancelSignal;
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
        return result != null || error != null || isCanceled();
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
                    if (isCanceled()) {
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

    /**
     * キャンセルチェック用のコールバック
     * <p/>
     * cancel()メソッドを呼び出すか、このコールバックがisCanceled()==trueになった時点でキャンセル扱いとなる。
     */
    public interface CancelSignal {
        /**
         * キャンセルする場合はtrueを返す
         *
         * @return
         */
        boolean isCanceled();
    }

    /**
     * シンプルなリスナ
     *
     * @param <T>
     */
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
