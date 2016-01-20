package com.eaglesakura.android.net.stream;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.thread.async.error.TaskException;

import java.io.IOException;
import java.io.InputStream;

public class RawStreamController implements IStreamController {
    @Override
    public <T> InputStream wrapStream(Connection<T> connection, HttpHeader respHeader, InputStream originalStream) throws IOException, TaskException {
        return originalStream;
    }
}
