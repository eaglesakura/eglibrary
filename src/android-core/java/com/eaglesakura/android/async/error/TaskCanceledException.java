package com.eaglesakura.android.async.error;

public class TaskCanceledException extends TaskException {
    public TaskCanceledException() {
    }

    public TaskCanceledException(String detailMessage) {
        super(detailMessage);
    }

    public TaskCanceledException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TaskCanceledException(Throwable throwable) {
        super(throwable);
    }
}
