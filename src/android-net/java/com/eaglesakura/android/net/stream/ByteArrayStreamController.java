package com.eaglesakura.android.net.stream;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.cache.CacheController;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.error.TaskCanceledException;
import com.eaglesakura.android.thread.async.error.TaskException;
import com.eaglesakura.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 通信結果を一度ByteArrayに変換するコントローラ
 */
public class ByteArrayStreamController implements StreamController {

    @Override
    public <T> InputStream wrapStream(Connection<T> connection, AsyncTaskResult<T> taskResult, HttpHeader respHeader, InputStream originalStream) throws IOException, TaskException {
        if (taskResult.isCanceled()) {
            throw new TaskCanceledException("canceled task");
        }

        byte[] buffer = IOUtil.toByteArray(originalStream, false);
        CacheController controller = connection.getCacheController();
        if (controller != null) {
            controller.putCache(connection.getRequest(), respHeader, buffer);
        }
        return new ByteArrayInputStream(buffer);
    }
}
