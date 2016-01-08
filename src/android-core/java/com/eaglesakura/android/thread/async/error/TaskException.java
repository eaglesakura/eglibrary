package com.eaglesakura.android.thread.async.error;

public abstract class TaskException extends Exception {
    public TaskException() {
    }

    public TaskException(String detailMessage) {
        super(detailMessage);
    }

    public TaskException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TaskException(Throwable throwable) {
        super(throwable);
    }
}
