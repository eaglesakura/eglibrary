package com.eaglesakura.android.net.internal;

import com.eaglesakura.android.net.cache.ICacheWriter;
import com.eaglesakura.android.thread.async.AsyncTaskResult;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class NetworkParseInputStream extends DigestInputStream {
    final AsyncTaskResult taskResult;

    final ICacheWriter cacheWriter;

    public NetworkParseInputStream(InputStream stream, ICacheWriter cacheWriter, MessageDigest digest, AsyncTaskResult taskResult) {
        super(stream, digest);
        this.taskResult = taskResult;
        this.cacheWriter = cacheWriter;
    }

    private void throwIfCanceled() throws IOException {
        if (taskResult.isCanceled()) {
            throw new IOException("Canceled Task Stream");
        }
    }

    private void writeCache(byte[] buffer, int offset, int length) throws IOException {
        if (cacheWriter == null || length <= 0) {
            return;
        } else {
            cacheWriter.write(buffer, offset, length);
        }
    }

    @Override
    public int read() throws IOException {
        throwIfCanceled();
        int result = in.read();
        cacheWriter.write(new byte[]{(byte) result}, 0, 1);
        return result;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        throwIfCanceled();
        int result = in.read(buffer, byteOffset, byteCount);
        writeCache(buffer, byteOffset, result);
        return result;
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        throwIfCanceled();

        if (byteCount < 0) {
            throw new UnsupportedOperationException();
        }

        byte[] temp = new byte[Math.min((int) byteCount, 1024 * 4)];
        int count;
        int sumSkip = 0;
        // 指定量を読み込むことでスキップ扱いとする
        while ((count = read(temp, 0, Math.min(temp.length, (int) byteCount - sumSkip))) > 0 && (sumSkip < byteCount)) {
            throwIfCanceled();
            writeCache(temp, 0, count);
            sumSkip += count;
        }
        return sumSkip;
    }

    @Override
    public int available() throws IOException {
        throwIfCanceled();
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
