package com.eaglesakura.android.net.request;

import com.eaglesakura.android.net.RetryPolicy;
import com.eaglesakura.android.net.cache.CachePolicy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SimplePostRequest extends ConnectRequest {
    private CachePolicy cachePolicy = new CachePolicy();

    private RetryPolicy retryPolicy = new RetryPolicy(10);

    private byte[] buffer;

    private File localFile;

    private String contentType;

    public SimplePostRequest() {
        super(Method.POST);
    }

    /**
     * オンメモリのバッファをPOSTする
     *
     * @param contentType
     * @param buffer
     */
    public void setPostBuffer(String contentType, byte[] buffer) {
        this.buffer = buffer;
        this.contentType = contentType;
    }

    /**
     * ローカルにあるファイルをPOSTする
     *
     * @param contentType
     * @param file
     * @throws IOException
     */
    public void setPostFile(String contentType, File file) throws IOException {
        if (!file.isFile() || file.length() <= 0) {
            throw new IOException("file access failed :: " + file.getAbsolutePath());
        }

        this.localFile = file;
        this.contentType = contentType;
    }

    @Override
    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    @Override
    public ConnectContent getContent() {
        if (buffer != null) {
            return new ConnectContent() {
                @Override
                public long getLength() {
                    return buffer.length;
                }

                @Override
                public InputStream openStream() throws IOException {
                    return new ByteArrayInputStream(buffer);
                }

                @Override
                public String getContentType() {
                    return contentType;
                }
            };
        } else {
            final long length = localFile.length();
            return new ConnectContent() {
                @Override
                public long getLength() {
                    return length;
                }

                @Override
                public InputStream openStream() throws IOException {
                    return new FileInputStream(localFile);
                }

                @Override
                public String getContentType() {
                    return contentType;
                }
            };
        }
    }
}
