package com.eaglesakura.android.net.internal;

import com.eaglesakura.android.thread.async.AsyncTaskResult;

import java.io.IOException;
import java.io.InputStream;

public class CancelableInputStream extends InputStream {
    final AsyncTaskResult taskResult;

    final InputStream stream;

    public CancelableInputStream(AsyncTaskResult taskResult, InputStream stream) {
        this.taskResult = taskResult;
        this.stream = stream;
    }

    private void throwIfCanceled() throws IOException {
        if (taskResult.isCanceled()) {
            throw new IOException("Canceled Task Stream");
        }
    }

    @Override
    public int read() throws IOException {
        throwIfCanceled();
        return stream.read();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        throwIfCanceled();
        return stream.read(buffer);
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        throwIfCanceled();
        return stream.read(buffer, byteOffset, byteCount);
    }

    @Override
    public void reset() throws IOException {
        throwIfCanceled();
        stream.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        throwIfCanceled();
        return stream.skip(byteCount);
    }

    @Override
    public int available() throws IOException {
        throwIfCanceled();
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
}
