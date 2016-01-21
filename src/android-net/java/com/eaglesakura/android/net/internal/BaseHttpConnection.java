package com.eaglesakura.android.net.internal;

import com.eaglesakura.android.net.Connection;
import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.NetworkConnector;
import com.eaglesakura.android.net.RetryPolicy;
import com.eaglesakura.android.net.cache.ICacheController;
import com.eaglesakura.android.net.cache.ICacheWriter;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.android.net.parser.RequestParser;
import com.eaglesakura.android.net.stream.IStreamController;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.error.TaskCanceledException;
import com.eaglesakura.android.thread.async.error.TaskException;
import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;
import com.eaglesakura.util.Timer;
import com.eaglesakura.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * HTTP接続本体を行う
 */
public abstract class BaseHttpConnection<T> extends Connection<T> {

    protected final RequestParser<T> parser;

    protected final ConnectRequest request;

    protected final NetworkConnector connector;

    /**
     * キャッシュから生成されたダイジェスト
     */
    protected String cacheDigest;

    /**
     * ネットワークから生成されたダイジェスト
     */
    protected String netDigest;

    public BaseHttpConnection(NetworkConnector connector, ConnectRequest request, RequestParser<T> parser) {
        this.parser = parser;
        this.request = request;
        this.connector = connector;
    }

    @Override
    public ConnectRequest getRequest() {
        return request;
    }

    @Override
    public ICacheController getCacheController() {
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

    protected MessageDigest newMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * ネットワーク経由のInputStreamからパースを行う
     * ストリームのcloseは外部に任せる
     *
     * @param stream
     * @param digest
     * @return
     * @throws Exception
     */
    protected T parseFromStream(AsyncTaskResult<T> taskResult, HttpHeader respHeader, InputStream stream, ICacheWriter cacheWriter, MessageDigest digest) throws Exception {
        // コンテンツをラップする
        // 必要に応じてファイルにキャッシュされたり、メモリに載せたりする。
        IStreamController controller = connector.getStreamController();
        InputStream readStream = null;
        NetworkParseInputStream parseStream = null;
        try {
            parseStream = new NetworkParseInputStream(stream, cacheWriter, digest, taskResult);
            if (controller != null) {
                readStream = controller.wrapStream(this, respHeader, parseStream);
            } else {
                readStream = stream;
            }
            T parsed = parser.parse(this, taskResult, readStream);
            return parsed;
        } finally {
            if (readStream != parseStream) {
                IOUtil.close(parseStream);
            }
            IOUtil.close(readStream);
        }
    }

    protected ICacheWriter newCacheWriter(HttpHeader header) {
        try {
            ICacheController controller = connector.getCacheController();
            if (controller != null) {
                return controller.newCacheWriter(request, header);
            }
        } catch (Exception e) {
        }
        return null;
    }

    protected void closeCacheWriter(T result, ICacheWriter writer) {
        if (writer == null) {
            return;
        }

        try {
            if (result != null) {
                writer.commit();
            } else {
                writer.abort();
            }

        } catch (Exception e) {

        }

        IOUtil.close(writer);
    }

    /**
     * キャッシュからデータをパースする
     *
     * @param taskResult
     * @return
     */
    private T tryCacheParse(AsyncTaskResult<T> taskResult) {
        ICacheController controller = connector.getCacheController();
        if (controller == null) {
            return null;
        }

        MessageDigest digest = newMessageDigest();
        InputStream stream = null;
        try {
            stream = controller.openCache(request);
            T parsed = parseFromStream(taskResult, null, stream, null, newMessageDigest());
            if (parsed != null) {
                // パースに成功したら指紋を残す
                cacheDigest = StringUtil.toHexString(digest.digest());
            }
            return parsed;
        } catch (Exception e) {
            // キャッシュ読み込み失敗は無視する
            LogUtil.log(e);
            return null;
        } finally {
            IOUtil.close(stream);
        }
    }

    /**
     * 接続を行う
     *
     * @return
     * @throws IOException
     */
    protected abstract T tryNetworkParse(AsyncTaskResult<T> result, MessageDigest digest) throws IOException, TaskException;

    private T parseFromStream(AsyncTaskResult<T> result) throws Exception {
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

        Timer waitTimer = new Timer();
        // 施行回数が残っていたら通信を行う
        while ((++tryCount) <= (MAX_RETRY + 1)) {
            try {
                MessageDigest digest = newMessageDigest();
                T parsed = tryNetworkParse(result, digest);
                if (parsed != null) {
                    netDigest = StringUtil.toHexString(digest.digest());
                    return parsed;
                }
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

            // 必要時間だけウェイトをかける
            {
                waitTimer.start();
                // キャンセルされてない、かつウェイト時間が残っていたら眠らせる
                while (!result.isCanceled() && (waitTimer.end() < waitTime)) {
                    Util.sleep(1);
                }
                if (result.isCanceled()) {
                    throw new TaskCanceledException();
                }
            }


            waitTime = retryPolicy.nextBackoffTimeMs(tryCount, waitTime);
        }

        throw new IOException("Connection Failed try : " + tryCount);
    }

    @Override
    public T doInBackground(AsyncTaskResult<T> result) throws Exception {

        T parsed = tryCacheParse(result);
        if (parsed != null) {
            return parsed;
        }

        if (result.isCanceled()) {
            return null;
        }

        parsed = parseFromStream(result);
        return parsed;
    }
}
