package com.eaglesakura.android.net_legacy;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.eaglesakura.android.dao.net.DbNetCache;
import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Volleyを利用してデータを取得する
 * <BR>
 * コードが大きくなったので、NetworkConnectorから分離
 */
public class VolleyNetworkResult<T> extends LegacyNetworkResult<T> {
    final String httpMethod;

    final LegacyNetworkConnector connector;

    final long cacheTimeoutMs;

    final Map<String, String> postParams;

    final Map<String, String> headers;

    final byte[] postBuffer;

    final LegacyNetworkConnector.RequestParser<T> parser;

    final int volleyMethod;

    final long connectStartTime = System.currentTimeMillis();

    long connectEndTime;

    Map<String, String> receivedHeaders = new HashMap<>();

    public VolleyNetworkResult(String url, LegacyNetworkConnector connector, LegacyNetworkConnector.RequestParser<T> parser, int volleyMethod, LegacyNetworkConnector.IConnectParams params) {
        super(url);

        this.connector = connector;
        this.cacheTimeoutMs = params.getCacheTimeoutMs();
        this.postParams = params.getRequestParams();
        this.parser = parser;
        this.headers = params.getHeaders();
        this.postBuffer = params.getPostBuffer();
        this.volleyMethod = volleyMethod;

        switch (volleyMethod) {
            case Request.Method.GET:
                httpMethod = "GET";
                break;
            case Request.Method.POST:
                httpMethod = "POST";
                break;
            case Request.Method.HEAD:
                httpMethod = "HEAD";
                break;
            default:
                throw new IllegalArgumentException("Method : " + volleyMethod);
        }
    }


    Request<T> volleyRequest;

    LegacyNetworkResult<T> self() {
        return this;
    }

    boolean loadFromCache() throws Exception {
        DbNetCache cache = connector.loadCache(url);

        if (cache == null) {
            return false;
        }

        // キャッシュDrop前にハッシュを保存する
        oldDataHash = cache.getHash();

        if (connector.dropTimeoutCache(cache, cacheTimeoutMs)) {
            return false;
        }

        BlockInputStream is = new BlockInputStream(connector.getCacheDatabase(), cache.getUrl());
        T parse;
        try {
            parse = parser.parse(this, is);
        } finally {
            IOUtil.close(is);
        }

        if (parse != null) {
            downloadedDataSize = cache.getBodySize();
            currentDataHash = oldDataHash;
            connectEndTime = System.currentTimeMillis();
            onReceived(parse);
            return true;
        } else {
            return false;
        }
    }

    public long getRespTimeMs() {
        if (connectEndTime == 0) {
            return -1;
        } else {
            return connectEndTime - connectStartTime;
        }
    }

    /**
     * 受信したヘッダを取得する
     *
     * @return
     */
    public Map<String, String> getReceivedHeaders() {
        return receivedHeaders;
    }

    void loadFromNetwork() {
        volleyRequest = new Request<T>(volleyMethod, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                LegacyNetworkConnector.log(volleyError);
                onError(volleyError);
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (postParams != null && !postParams.isEmpty()) {
                    return postParams;
                }
                return super.getParams();
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                if (postBuffer != null) {
                    return postBuffer;
                }

                return super.getBody();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> defHeader = connector.factory.newHttpHeaders(url, httpMethod);
                if (headers != null && !headers.isEmpty()) {
                    headers.putAll(defHeader);
                    return headers;
                }
                return defHeader;
            }

            @Override
            protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
                String errorMessage = "parseFromCache error";
                try {
                    connectEndTime = System.currentTimeMillis();
                    LogUtil.log("%s volley received(%s) %.1f KB",
                            connector.getClass().getSimpleName(),
                            url,
                            networkResponse.data == null ? 0 : (float) networkResponse.data.length / 1024.0f,
                            getRespTimeMs()
                    );

                    errorMessage = "hash calc error";
                    currentDataHash = EncodeUtil.genMD5(networkResponse.data);
                    LogUtil.log("data(%s) hash old(%s) -> new(%s) modified(%s)",
                            url,
                            oldDataHash, currentDataHash,
                            "" + isDataModified()
                    );

                    if (networkResponse.headers != null) {
                        receivedHeaders = networkResponse.headers;
                    }
                    downloadedDataSize = networkResponse.data.length;

                    errorMessage = "parseFromCache failed :: " + parser;
                    T resultValue = parser.parse(self(), new ByteArrayInputStream(networkResponse.data));

                    errorMessage = "Resp error";
                    Response<T> result = Response.success(resultValue, getCacheEntry());

                    // キャッシュに追加する
                    if (cacheTimeoutMs > 10) {
                        errorMessage = "put cache";
                        putCache(url, networkResponse.headers, httpMethod, Arrays.copyOf(networkResponse.data, networkResponse.data.length), cacheTimeoutMs);
                    }
                    errorMessage = "completed";
                    return result;
                } catch (Exception e) {
                    return Response.error(new VolleyError(errorMessage));
                }
            }

            @Override
            protected void deliverResponse(T data) {
                onReceived(data);
            }
        };
        volleyRequest.setRetryPolicy(connector.factory.newRetryPolycy(url, httpMethod));
        LegacyNetworkConnector.requests.add(volleyRequest);
    }

    @Override
    void abortRequest() {
        if (volleyRequest != null) {
            volleyRequest.cancel();
            volleyRequest = null;
        }
    }

    @Override
    void startDownloadFromBackground() {
        if (isCanceled()) {
            return;
        }

        try {
            if (loadFromCache()) {
                // キャッシュからの読み込みに成功したらそこで終了
                return;
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }

        if (isCanceled()) {
            return;
        }

        loadFromNetwork();
    }


    /**
     * URLを指定してキャッシュとして登録する
     *
     * @param url
     * @param timeoutMs
     */
    protected void putCache(final String url, final Map<String, String> headers, final String method, final byte[] body, final long timeoutMs) {
        if (body == null || body.length == 0) {
            return;
        }

        LegacyNetworkConnector.cacheWorkTask.pushFront(new Runnable() {
            @Override
            public void run() {
                LegacyNetworkConnector.CacheDatabase db = connector.getCacheDatabase();
                // ブロックとして書き出す
                BlockOutputStream os = null;
                try {
                    db.openWritable();
                    db.cleanFileBlock(url);

                    os = new BlockOutputStream(db, url, 0);
                    os.write(body, 0, body.length);
                    os.onCompleted();
                } catch (Exception e) {
                    LogUtil.log(e);
                    return;
                } finally {
                    IOUtil.close(os);
                    db.close();
                }


                DbNetCache cache = new DbNetCache();
                cache.setUrl(url);
                cache.setBodySize(body.length);
                cache.setDownloadedSize(body.length);
                cache.setBlockSize(LegacyNetworkConnector.BLOCK_SIZE);
                cache.setCacheType(LegacyNetworkConnector.CACHETYPE_DB);
                cache.setMethod(method.toUpperCase());
                cache.setCacheTime(new Date());
                cache.setCacheLimit(new Date(System.currentTimeMillis() + timeoutMs));

                if (headers != null && !headers.isEmpty()) {
                    cache.setEtag(headers.get("ETag"));
                }
                cache.setHash(EncodeUtil.genMD5(body));

                try {
                    db.open();
                    db.put(cache);
                } finally {
                    db.close();
                }
            }
        });
        LegacyNetworkConnector.cacheWorkTask.start();
    }

}
