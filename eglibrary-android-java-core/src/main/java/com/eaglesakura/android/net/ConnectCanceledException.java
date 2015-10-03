package com.eaglesakura.android.net;

import java.io.IOException;

/**
 * エラー
 */
public class ConnectCanceledException extends IOException{
    public ConnectCanceledException() {
    }

    public ConnectCanceledException(String detailMessage) {
        super(detailMessage);
    }

    public ConnectCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectCanceledException(Throwable cause) {
        super(cause);
    }
}
