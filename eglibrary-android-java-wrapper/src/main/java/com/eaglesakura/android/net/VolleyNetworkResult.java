package com.eaglesakura.android.net;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.eaglesakura.android.dao.net.DbNetCache;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * Volleyを利用してデータを取得する
 * <p/>
 * コードが大きくなったので、NetworkConnectorから分離
 */
public class VolleyNetworkResult<T> extends NetworkResult<T> {
    final String httpMethod;

    final NetworkConnector connector;

    final long cacheTimeoutMs;

    final Map<String, String> params;

    final NetworkConnector.RequestParser<T> parser;

    final int volleyMethod;

    public VolleyNetworkResult(String url, NetworkConnector connector, NetworkConnector.RequestParser<T> parser, int volleyMethod, long cacheTimeoutMs, Map<String, String> params) {
        super(url);

        this.connector = connector;
        this.cacheTimeoutMs = cacheTimeoutMs;
        this.params = params;
        this.parser = parser;
        this.volleyMethod = volleyMethod;
        httpMethod = volleyMethod == Request.Method.GET ? "GET" : "POST";
    }


    Request<T> volleyRequest;

    NetworkResult<T> self() {
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

        T parse = parser.parse(this, new BlockInputStream(connector.getCacheDatabase(), cache.getUrl()));
        if (parse != null) {
            currentDataHash = oldDataHash;
            onReceived(parse);
            return true;
        } else {
            return false;
        }
    }

    void loadFromNetwork() {
        volleyRequest = new Request<T>(volleyMethod, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                NetworkConnector.log(volleyError);
                onError(volleyError);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (params != null && !params.isEmpty()) {
                    return params;
                }
                return super.getParams();
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return connector.factory.newHttpHeaders(url, httpMethod);
            }

            @Override
            protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
                try {
                    LogUtil.log("%s volley received(%s) %.1f KB",
                            connector.getClass().getSimpleName(),
                            url,
                            networkResponse.data == null ? 0 : (float) networkResponse.data.length / 1024.0f);


                    currentDataHash = EncodeUtil.genMD5(networkResponse.data);
                    LogUtil.log("data(%s) hash old(%s) -> new(%s) modified(%s)",
                            url,
                            oldDataHash, currentDataHash,
                            "" + isDataModified()
                    );

                    Response<T> result = Response.success(parser.parse(self(), new ByteArrayInputStream(networkResponse.data)), getCacheEntry());
                    // キャッシュに追加する
                    if (cacheTimeoutMs > 10) {
                        putCache(url, networkResponse.headers, httpMethod, Arrays.copyOf(networkResponse.data, networkResponse.data.length), cacheTimeoutMs);
                    }
                    return result;
                } catch (Exception e) {
                    return Response.error(new VolleyError("parse error"));
                }
            }

            @Override
            protected void deliverResponse(T data) {
                onReceived(data);
            }
        };
        volleyRequest.setRetryPolicy(connector.factory.newRetryPolycy(url, httpMethod));
        NetworkConnector.requests.add(volleyRequest);
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

        NetworkConnector.tasks.pushFront(new Runnable() {
            @Override
            public void run() {
                NetworkConnector.CacheDatabase db = connector.getCacheDatabase();
                // ブロックとして書き出す
                BlockOutputStream os = null;
                try {
                    db.open();
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
                cache.setBlockSize(NetworkConnector.BLOCK_SIZE);
                cache.setCacheType(NetworkConnector.CACHETYPE_DB);
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
        NetworkConnector.tasks.start();
    }

}
