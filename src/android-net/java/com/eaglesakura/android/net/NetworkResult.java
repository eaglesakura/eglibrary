package com.eaglesakura.android.net;

import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.error.TaskException;

public class NetworkResult<T> {
    final Connection<T> connection;

    final AsyncTaskResult<T> taskResult;

    NetworkResult(Connection<T> connection, AsyncTaskResult<T> taskResult) {
        this.connection = connection;
        this.taskResult = taskResult;
    }

    public boolean isContentModified() {
        return connection.isContentModified();
    }

    public String getCacheDigest() {
        return connection.getCacheDigest();
    }

    public String getContentDigest() {
        return connection.getContentDigest();
    }

    public boolean hasContent() {
        return connection.hasContent();
    }

    public boolean isTaskFinished() {
        return taskResult.isTaskFinished();
    }

    public void cancel() {
        taskResult.cancel();
    }

    public boolean isCanceled() {
        return taskResult.isCanceled();
    }

    public void setCancelSignal(AsyncTaskResult.CancelSignal cancelSignal) {
        taskResult.setCancelSignal(cancelSignal);
    }

    public AsyncTaskResult<T> setListener(AsyncTaskResult.Listener<T> listener) {
        return taskResult.setListener(listener);
    }

    public T await(long timeoutMs) throws TaskException {
        return taskResult.await(timeoutMs);
    }

    public AsyncTaskResult<T> getTaskResult() {
        return taskResult;
    }

    public Connection<T> getConnection() {
        return connection;
    }
}
