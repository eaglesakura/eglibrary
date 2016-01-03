package com.eaglesakura.android.async;

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
            this.listener = listener;

            // 既にタスクが完了してしまっている場合はリスナをコールさせてローカルに残さない
            if (isTaskFinished()) {
                handleListener();
                this.listener = null;
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
    public T await(long timeoutMs) throws Exception {
        synchronized (awaitLock) {
            if (!isTaskFinished()) {
                awaitLock.wait(timeoutMs);
            }
        }

        throwIfError();
        return result;
    }

    /**
     * タスクが成功、もしくは失敗していたらtrue
     *
     * @return
     */
    public boolean isTaskFinished() {
        return result != null || error != null;
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

            result = task.doInBackground(controller);
        } catch (Exception e) {
            error = e;
        }

        synchronized (resultLock) {
            this.result = result;
            this.error = error;
            handleListener();
            this.listener = null;
        }

        synchronized (awaitLock) {
            awaitLock.notifyAll();
        }
    }

    /**
     * リスナのハンドリングを行う
     */
    void handleListener() {
        controller.taskHandler.request(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    if (result != null) {
                        listener.onTaskCompleted(AsyncTaskResult.this, result);
                    } else {
                        listener.onTaskFailed(AsyncTaskResult.this, error);
                    }
                }
            }
        });
    }

    /**
     * エラーが発生していたら例外を投げ、それ以外は何もしない
     *
     * @throws Exception
     */
    void throwIfError() throws Exception {
        if (error == null) {
            return;
        }
        throw error;
    }

    public interface Listener<T> {
        void onTaskCompleted(AsyncTaskResult<T> task, T result);

        void onTaskFailed(AsyncTaskResult<T> task, Exception error);
    }
}
