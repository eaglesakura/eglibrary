package com.eaglesakura.android.thread.async.error;

/**
 *
 */
public class TaskFailedException extends TaskException {
    Exception error;

    public TaskFailedException(Exception baseError) {
        this.error = baseError;
    }

    /**
     * タスクで発生した例外を取得する
     *
     * @return
     */
    public Exception getTaskError() {
        return error;
    }
}
