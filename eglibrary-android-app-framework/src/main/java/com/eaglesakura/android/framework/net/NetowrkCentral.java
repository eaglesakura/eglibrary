package com.eaglesakura.android.framework.net;

import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.eaglesakura.android.db.BlobKeyValueStore;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.thread.MultiRunningTasks;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * ネットワーク処理の中枢系
 * <p/>
 * 早期ローカルキャッシュに対応し、むやみにネットワークアクセスを行わないようにする
 */
public class NetowrkCentral {

    public static final long NOT_TIMEOUT = 0x6FFFFFFFFFFFFFFFL;

    private static RequestQueue requests;

    private static final Object lock = new Object();

    /**
     * システムデフォルトのポリシー指定
     */
    private static NetworkFactory factory = new NetworkFactory() {
        @Override
        public Map<String, String> getHttpHeaders(String url, String method) {
            return Collections.emptyMap();
        }

        @Override
        public RetryPolicy newRetryPolycy(final String url, String method) {
            return new DefaultRetryPolicy(1000, 10, 1.2f) {
                @Override
                public void retry(VolleyError error) throws VolleyError {
                    LogUtil.log("NetowrkCentral retry(%s)", url);
                    super.retry(error);
                }
            };
        }
    };

    /**
     * UIスレッドから呼び出しを行うためのタスクキュー
     */
    private static MultiRunningTasks tasks = new MultiRunningTasks(3);

    static {
        tasks.setThreadPoolMode(false);
        tasks.setThreadName("NetworkCentral");
    }

    private static BlobKeyValueStore getCacheDatabase() {
        File cacheFile = new File(FrameworkCentral.getApplication().getCacheDir(), "app-vlc.db");
        if (!cacheFile.getParentFile().exists()) {
            cacheFile.getParentFile().mkdirs();
        }

        return new BlobKeyValueStore(FrameworkCentral.getApplication(), cacheFile);
    }

    private static String toCacheKey(String url) {
        return EncodeUtil.genSHA1(url.getBytes());
    }

    public static RequestQueue getVolleyRequests() {
        synchronized (lock) {
            if (requests == null) {
                start();
            }
            return requests;
        }
    }

    /**
     * 開始処理を行う
     */
    public static void start() {
        synchronized (lock) {
            if (requests == null) {
                requests = Volley.newRequestQueue(FrameworkCentral.getApplication());
                requests.start();
            }
        }
    }

    /**
     * 終了処理を行う
     */
    public static void cleanup() {
        synchronized (lock) {
            if (requests != null) {
                requests.stop();
                requests = null;
            }
        }
    }

    private static void log(VolleyError error) {
        if (error.networkResponse != null) {
            LogUtil.log("Volley Error has resp(%s)", error.networkResponse.toString());
            if (error.networkResponse.headers != null) {
                LogUtil.log("Volley Error headers(%s)", error.networkResponse.headers.toString());
            }
        }
    }

    public static class ImageOption {
        public int maxWidth = 1024;

        public int maxHeight = 1024;

        public Bitmap.Config imageFormat = Bitmap.Config.ARGB_8888;

        public int cacheQuality = 100;

        public Bitmap.CompressFormat cacheFormat = Bitmap.CompressFormat.PNG;

        /**
         * 画像キャッシュのタイムアウトはデフォルト24時間
         */
        public long cacheTimeoutMs = 1000 * 60 * 60 * 24;

        public ImageOption() {

        }

        public ImageOption(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        public ImageOption(int maxWidth, int maxHeight, long cacheTimeoutMs) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.cacheTimeoutMs = cacheTimeoutMs;
        }
    }

    /**
     * UIスレッドで簡易に受け取れるようにするためのオプション
     */
    public interface ImageListener {
        /**
         * 画像を読み込んだ
         * <p/>
         * このメソッドはUIスレッドから呼び出される
         *
         * @param url
         * @param image
         */
        void onImageLoaded(String url, Bitmap image);

        /**
         * 画像の受け取りに失敗した
         *
         * @param url
         */
        void onImageLoadFailed(String url, IOException exception);
    }

    /**
     * UIスレッドで簡易に受け取れるようにするためのオプション
     *
     * @param <T>
     */
    public interface DataListener<T> {
        void onDataLoaded(String url, T data);

        void onDataLoadFailed(String url, IOException exception);
    }

    /**
     * 非同期に読み込みを行わせる
     *
     * @param url
     * @param option
     * @param uiListener
     */
    public static void getCachedImageAsync(final String url, final ImageOption option, final ImageListener uiListener) {
        AndroidUtil.assertUIThread();

        tasks.setThreadPoolMode(false);
        tasks.pushBack(new Runnable() {
            @Override
            public void run() {
                // バックグラウンドで呼び出す
                getCachedImage(url, option, uiListener);
            }
        }).start();
    }

    /**
     * キャッシュ付き画像をUIスレッドで受け取る
     *
     * @param url
     * @param option
     * @param uiListner
     */
    public static void getCachedImage(final String url, ImageOption option, final ImageListener uiListner) {
        AndroidUtil.assertBackgroundThread();

        try {
            final Bitmap image = getCachedImage(url, option);
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    uiListner.onImageLoaded(url, image);
                }
            });
        } catch (final IOException e) {
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    uiListner.onImageLoadFailed(url, e);
                }
            });
        }
    }

    /**
     * キャッシュ付き画像を同期的に取得する
     *
     * @param url
     * @param option
     * @return
     * @throws IOException
     */
    public static Bitmap getCachedImage(final String url, ImageOption option) throws IOException {
        AndroidUtil.assertBackgroundThread();

        // オプションを組み立てる
        if (option == null) {
            option = new ImageOption();
        }
        BlobKeyValueStore imageDb = getCacheDatabase();

        final String CACHE_KEY = toCacheKey(url);

        // キャッシュを読み込む
        try {
            imageDb.open();
            Bitmap cache = imageDb.getImage(CACHE_KEY, option.cacheTimeoutMs);
            if (cache != null) {
                LogUtil.log("NetworkCentral has cache(%s)", url);
                return cache;
            }
        } finally {
            imageDb.close();
        }

        final Holder<Bitmap> responceHolder = new Holder<>();
        final Holder<Boolean> finishedHolder = new Holder<>();

        // キャッシュが無い場合、Volley経由でデータを読み込む
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        responceHolder.set(bitmap);
                        finishedHolder.set(true);
                    }
                }, option.maxWidth, option.maxHeight, option.imageFormat,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        log(volleyError);
                        finishedHolder.set(true);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return factory.getHttpHeaders(url, "GET");
            }

        };
        request.setRetryPolicy(factory.newRetryPolycy(url, "GET"));
        getVolleyRequests().add(request);
        getVolleyRequests().start();

        // 終了するまで待つ
        finishedHolder.getWithWait();

        // レスポンスをチェックして、存在しなければエラーであると判定できる
        if (responceHolder.get() == null) {
            throw new IOException("Volley Resp Error");
        } else {
            Bitmap bitmap = responceHolder.get();

            // キャッシュに保存する
            try {
                imageDb.open();
                imageDb.put(CACHE_KEY, bitmap, option.cacheFormat, option.cacheQuality);
            } finally {
                imageDb.close();
            }

            return responceHolder.get();
        }
    }

    /**
     * UIスレッドから非同期でデータを得る
     *
     * @param url
     * @param parser
     * @param cacheTimeoutMs
     * @param uiListener
     * @param <T>
     */
    public static <T> void getAsync(final String url, final RequestParser<T> parser, final long cacheTimeoutMs, final DataListener<T> uiListener) {
        AndroidUtil.assertUIThread();

        tasks.pushBack(new Runnable() {
            @Override
            public void run() {
                getSync(url, parser, cacheTimeoutMs, uiListener);
            }
        }).start();
    }

    /**
     * 同期的にデータを取得し、UIスレッドで通知を受け取る
     *
     * @param url
     * @param parser
     * @param cacheTimeoutMs
     * @param uiListener
     * @param <T>
     */
    public static <T> void getSync(final String url, RequestParser<T> parser, long cacheTimeoutMs, final DataListener<T> uiListener) {
        AndroidUtil.assertBackgroundThread();

        try {
            final T data = getSync(url, parser, cacheTimeoutMs);
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    uiListener.onDataLoaded(url, data);
                }
            });
        } catch (final IOException e) {
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    uiListener.onDataLoadFailed(url, e);
                }
            });
        }
    }

    /**
     * グローバルに適用されるリトライ規定
     *
     * @param factory
     */
    public static void setFactory(NetworkFactory factory) {
        NetowrkCentral.factory = factory;
    }

    /**
     * 同期的にデータを得る。
     * <p/>
     * ローカルキャッシュは基本的に利用しない。
     *
     * @param url
     * @param parser
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T getSync(String url, final RequestParser<T> parser) throws IOException {
        return getSync(url, parser, 0);
    }

    /**
     * 同期的にオブジェクトを取得する
     *
     * @param url
     * @param parser
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T getSync(final String url, final RequestParser<T> parser, long cacheTimeoutMs) throws IOException {
        AndroidUtil.assertBackgroundThread();

        // 早期ローカルキャッシュをチェックする
        final String CACHE_KEY = toCacheKey(url);
        BlobKeyValueStore store = getCacheDatabase();
        try {
            store.open();

            byte[] data = store.get(CACHE_KEY, cacheTimeoutMs);
            if (data != null) {
                LogUtil.log("NetworkCentral has cache(%s) data(%d bytes)", url, data.length);
                try {
                    T result = parser.parse(null, data);
                    if (result != null) {
                        return result;
                    }
                } catch (Exception e) {
                    LogUtil.log(e);
                }
            }
        } finally {
            store.close();
        }


        final Holder<T> responceHolder = new Holder<>();
        final Holder<byte[]> rawHolder = new Holder<>();
        final Holder<Boolean> finishedHolder = new Holder<>();

        Request<T> request = new Request<T>(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                log(volleyError);
                finishedHolder.set(true);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return factory.getHttpHeaders(url, "GET");
            }

            @Override
            protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
                try {
                    rawHolder.set(Arrays.copyOf(networkResponse.data, networkResponse.data.length));
                    return Response.success(parser.parse(networkResponse, networkResponse.data), getCacheEntry());
                } catch (Exception e) {
                    return Response.error(new VolleyError("parse error"));
                }
            }

            @Override
            protected void deliverResponse(T object) {
                responceHolder.set(object);
                finishedHolder.set(true);
            }
        };
        request.setRetryPolicy(factory.newRetryPolycy(url, "GET"));
        getVolleyRequests().add(request);
        getVolleyRequests().start();

        finishedHolder.getWithWait();

        if (responceHolder.get() == null) {
            throw new IOException("Volley Resp Error");
        } else {
            // キャッシュに追加する
            try {
                store.open();
                store.put(CACHE_KEY, rawHolder.get());
            } finally {
                store.close();
            }

            return responceHolder.get();
        }
    }

    /**
     * 同期的にPostを行う
     * <p/>
     * postはキャッシュを行わない。
     *
     * @param url
     * @param parser
     * @param params
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T postSync(final String url, final RequestParser<T> parser, final Map<String, String> params) throws IOException {
        AndroidUtil.assertBackgroundThread();

        final Holder<T> responceHolder = new Holder<>();
        final Holder<Boolean> finishedHolder = new Holder<>();

        Request<T> request = new Request<T>(Request.Method.POST, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                log(volleyError);
                finishedHolder.set(true);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return factory.getHttpHeaders(url, "POST");
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            @Override
            protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
                try {
                    return Response.success(parser.parse(networkResponse, networkResponse.data), getCacheEntry());
                } catch (Exception e) {
                    return Response.error(new VolleyError("parse error"));
                }
            }

            @Override
            protected void deliverResponse(T object) {
                responceHolder.set(object);
                finishedHolder.set(true);
            }
        };

        request.setRetryPolicy(factory.newRetryPolycy(url, "POST"));
        getVolleyRequests().add(request);
        getVolleyRequests().start();

        finishedHolder.getWithWait();

        if (responceHolder.get() == null) {
            throw new IOException("Volley Resp Error");
        } else {
            return responceHolder.get();
        }
    }

    public interface RequestParser<T> {
        T parse(NetworkResponse response, byte[] data) throws Exception;
    }

    /**
     * RetryPolycyの生成を行わせる
     */
    public interface NetworkFactory {
        RetryPolicy newRetryPolycy(String url, String method);

        /**
         * Http認証に必要なヘッダを指定
         *
         * @return
         */
        Map<String, String> getHttpHeaders(String url, String method);
    }

    /**
     * 何も返さないParser
     */
    public static RequestParser<Object> VOID_REQUEST = new RequestParser<Object>() {
        @Override
        public Object parse(NetworkResponse response, byte[] data) throws Exception {
            return new Object();
        }
    };
}
