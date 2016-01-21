package com.eaglesakura.android.net.request;

import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.RetryPolicy;
import com.eaglesakura.android.net.cache.CachePolicy;

public abstract class ConnectRequest {
    public enum Method {
        GET,
        POST,
        HEAD,
        DELETE,
        PUT,
    }

    private final Method method;

    protected String url;

    protected HttpHeader header = new HttpHeader();

    /**
     * 通信タイムアウト時間を指定する
     */
    private long readTimeoutMs = 1000 * 10;

    /**
     * 接続タイムアウト時間を指定する
     */
    private long connectTimeoutMs = 1000 * 10;

    protected ConnectRequest(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    /**
     * 通信時のヘッダを取得する
     *
     * @return
     */
    public HttpHeader getHeader() {
        return header;
    }

    /**
     * タイムアウト時間を指定する
     *
     * @param readTimeoutMs
     */
    public void setReadTimeoutMs(long readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public long getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    /**
     * キャッシュ制御を取得する
     * nullを返却した場合、キャッシュ制御を行わない
     *
     * @return
     */
    public abstract CachePolicy getCachePolicy();

    /**
     * リトライ制御を取得する
     * nullを返却した場合、リトライ制御を行わない。
     *
     * @return
     */
    public abstract RetryPolicy getRetryPolicy();

    /**
     * POST時のBodyを取得する
     * <p/>
     * nullを返却した場合、POST時に何もデータを付与しない。
     *
     * @return
     */
    public abstract ConnectContent getContent();
}
