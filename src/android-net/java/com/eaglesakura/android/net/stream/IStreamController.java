package com.eaglesakura.android.net.stream;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.error.TaskException;

import java.io.IOException;
import java.io.InputStream;

/**
 * パーサーに渡すストリームを制御する。
 * <p/>
 * 一度ByteArrayに変換する等、必要に応じた制御を行う。
 * <p/>
 * オンメモリに乗らないキャッシュはそもそもキャッシュDBに載せられないので、ストリームコントロールと同時にキャッシュ制御も行う。
 */
public interface IStreamController {
    /**
     * パーサーに渡すストリームを生成する。
     * <p/>
     * 内部的にByteArrayに変換する等のラップを行う。
     * <p/>
     * 内部でoriginalStreamを閉じる必要はない。
     *
     * @param respHeader
     * @param originalStream
     * @return
     */
    <T> InputStream wrapStream(Connection<T> connection, HttpHeader respHeader, InputStream originalStream) throws IOException, TaskException;
}
