package com.eaglesakura.android.net;

import android.content.Context;

import com.eaglesakura.android.net.cache.CacheController;
import com.eaglesakura.android.net.cache.RESTfulCacheController;
import com.eaglesakura.android.net.internal.GoogleHttpClientConnectImpl;
import com.eaglesakura.android.net.request.HttpConnectRequest;
import com.eaglesakura.android.net.request.RequestParser;
import com.eaglesakura.android.net.stream.ByteArrayStreamController;
import com.eaglesakura.android.net.stream.StreamController;
import com.eaglesakura.android.thread.async.AsyncTaskController;

/**
 * ネットワークの接続制御を行う
 * <p/>
 * 通信そのものは専用スレッドで行われるため、UI/Backgroundどちらからも使用することができる。
 * 同期的に結果を得たい場合はawait()でタスク待ちを行えば良い。
 */
public class NetworkConnector {
    final Context context;

    StreamController streamController;

    CacheController cacheController;

    AsyncTaskController taskController;

    public NetworkConnector(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 通信スレッド数を指定する
     *
     * @param threads
     */
    public void setThreadNum(int threads) {
        taskController = new AsyncTaskController(threads, 1000 * 5);
    }

    public void setStreamController(StreamController streamController) {
        this.streamController = streamController;
    }

    public StreamController getStreamController() {
        return streamController;
    }

    public void setCacheController(CacheController cacheController) {
        this.cacheController = cacheController;
    }

    public CacheController getCacheController() {
        return cacheController;
    }

    /**
     * 接続を行う
     *
     * @param request
     * @param parser
     * @param <T>
     * @return
     */
    public <T> NetworkResult<T> connect(HttpConnectRequest request, RequestParser<T> parser) {
        GoogleHttpClientConnectImpl connect = new GoogleHttpClientConnectImpl(this, request, parser);
        return new NetworkResult<>(connect, taskController.pushBack(connect));
    }

    /**
     * 小さいデータ処理に使用するシンプルなコネクタを生成する
     *
     * @param context
     * @return
     */
    public static NetworkConnector newDefaultConnector(Context context) {
        return newDefaultConnector(context, 3);
    }


    /**
     * 小さいデータ処理に使用するシンプルなコネクタを生成する
     *
     * @param context
     * @param maxThreads 通信を行う最大スレッド数
     * @return
     */
    public static NetworkConnector newDefaultConnector(Context context, int maxThreads) {
        NetworkConnector result = new NetworkConnector(context);
        result.setStreamController(new ByteArrayStreamController());
        result.setCacheController(new RESTfulCacheController(context));
        result.setThreadNum(maxThreads);
        return result;
    }
}
