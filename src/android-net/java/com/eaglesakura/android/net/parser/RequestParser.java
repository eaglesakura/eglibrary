package com.eaglesakura.android.net.parser;


import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.thread.async.AsyncTaskResult;

import java.io.InputStream;

/**
 * オブジェクトのパースを行う
 *
 * @param <T>
 */
public interface RequestParser<T> {
    T parse(Connection<T> sender, AsyncTaskResult<T> taskResult, InputStream data) throws Exception;
}