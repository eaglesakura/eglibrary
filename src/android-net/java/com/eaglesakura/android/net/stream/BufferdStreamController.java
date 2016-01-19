package com.eaglesakura.android.net.stream;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.error.TaskException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferdStreamController implements StreamController {
    int bufferBytes = 1024 * 128;

    public BufferdStreamController() {
    }

    public BufferdStreamController(int bufferBytes) {
        this.bufferBytes = bufferBytes;
    }

    @Override
    public <T> InputStream wrapStream(Connection<T> connection, AsyncTaskResult<T> taskResult, HttpHeader respHeader, InputStream originalStream) throws IOException, TaskException {
        return new BufferedInputStream(originalStream, bufferBytes);
    }
}
