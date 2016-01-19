package com.eaglesakura.android.net.parser;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.json.JSON;

import java.io.InputStream;

public class JsonParser<T> implements RequestParser<T> {
    Class<T> clazz;

    public JsonParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T parse(Connection<T> sender, AsyncTaskResult<T> taskResult, InputStream data) throws Exception {
        return JSON.decode(data, clazz);
    }
}
