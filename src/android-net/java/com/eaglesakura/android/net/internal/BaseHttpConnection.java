package com.eaglesakura.android.net.internal;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.net.NetworkConnector;
import com.eaglesakura.android.net.RetryPolicy;
import com.eaglesakura.android.net.cache.CacheController;
import com.eaglesakura.android.net.request.HttpConnectRequest;
import com.eaglesakura.android.net.request.RequestParser;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.error.TaskCanceledException;
import com.eaglesakura.android.thread.async.error.TaskException;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;
import com.eaglesakura.util.Util;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * HTTP接続本体を行う
 */
public abstract class BaseHttpConnection<T> extends Connection<T> {

    protected final RequestParser<T> parser;

    protected final HttpConnectRequest request;

    protected final NetworkConnector connector;

    /**
     * キャッシュから生成されたダイジェスト
     */
    protected String cacheDigest;

    /**
     * ネットワークから生成されたダイジェスト
     */
    protected String netDigest;

    public BaseHttpConnection(NetworkConnector connector, HttpConnectRequest request, RequestParser<T> parser) {
        this.parser = parser;
        this.request = request;
        this.connector = connector;
    }

    @Override
    public HttpConnectRequest getRequest() {
        return request;
    }

    @Override
    public CacheController getCacheController() {
        return connector.getCacheController();
    }

    @Override
    public String getCacheDigest() {
        return cacheDigest;
    }

    @Override
    public String getContentDigest() {
        return netDigest;
    }

    /**
     * パースを試みる
     *
     * @param buffer
     * @return
     */
    private T parseFromCache(AsyncTaskResult<T> taskResult, byte[] buffer) {
        if (Util.isEmpty(buffer)) {
            return null;
        }

        try {
            return parser.parse(this, taskResult, new ByteArrayInputStream(buffer));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * キャッシュからデータをパースする
     *
     * @param taskResult
     * @return
     */
    private T parseFromCache(AsyncTaskResult<T> taskResult) {
        CacheController controller = connector.getCacheController();
        if (controller == null) {
            return null;
        }

        try {
            byte[] cache = controller.getCacheOrNull(request);
            if (cache != null) {
                cacheDigest = EncodeUtil.genMD5(cache);
            }
            return parseFromCache(taskResult, cache);
        } catch (IOException e) {
            // キャッシュ読み込み失敗は無視する
            LogUtil.log(e);
            return null;
        }
    }


    /**
     * ネットワーク経由のInputStreamからパースを行う
     *
     * @param stream
     * @param digest
     * @return
     * @throws Exception
     */
    protected T parseFromNet(AsyncTaskResult<T> taskResult, InputStream stream, MessageDigest digest) throws Exception {
        DigestInputStream dis = new DigestInputStream(stream, digest);
        T parsed = parser.parse(this, taskResult, dis);
        if (parsed != null) {
            // パースできたので、ダイジェストを保存する
            byte[] digestBytes = digest.digest();
            netDigest = StringUtil.toHexString(digestBytes);
        }
        return parsed;
    }

    /**
     * 接続を行う
     *
     * @return
     * @throws IOException
     */
    protected abstract T tryConnect(AsyncTaskResult<T> result, MessageDigest digest) throws IOException, TaskException;

    private T parseFromNet(AsyncTaskResult<T> result) throws Exception {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int tryCount = 0;
        final int MAX_RETRY;
        long waitTime = 0;
        if (retryPolicy != null) {
            MAX_RETRY = retryPolicy.getRetryNum();
            waitTime = retryPolicy.getBaseWaitTime();
        } else {
            MAX_RETRY = 0;
        }

        // 施行回数が残っていたら通信を行う
        while ((++tryCount) <= (MAX_RETRY + 1)) {
            if (result.isCanceled()) {
                throw new TaskCanceledException();
            }

            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                return tryConnect(result, digest);
            } catch (FileNotFoundException e) {
                // この例外はリトライしても無駄
                throw e;
            } catch (TaskCanceledException e) {
                // この例外はリトライしても無駄
                throw e;
            } catch (IOException e) {
                // その他のIO例外はひとまずリトライくらいはできる
                LogUtil.log("failed :: " + e.getClass().getSimpleName());
            } catch (Exception e) {
                // Catchしきれなかった例外は何か問題が発生している
                throw e;
            }

            waitTime = retryPolicy.nextBackoffTimeMs(tryCount, waitTime);
        }

        throw new IOException("Connection Failed try : " + tryCount);
    }

    @Override
    public T doInBackground(AsyncTaskResult<T> result) throws Exception {

        T parsed = parseFromCache(result);
        if (parsed != null) {
            return parsed;
        }

        if (result.isCanceled()) {
            return null;
        }

        parsed = parseFromNet(result);
        return parsed;
    }
}
