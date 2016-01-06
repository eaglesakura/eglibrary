package com.eaglesakura.android.async.error;

public class TaskTimeoutException extends TaskException {
    public TaskTimeoutException() {
    }

    public TaskTimeoutException(String detailMessage) {
        super(detailMessage);
    }

    public TaskTimeoutException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TaskTimeoutException(Throwable throwable) {
        super(throwable);
    }

}
