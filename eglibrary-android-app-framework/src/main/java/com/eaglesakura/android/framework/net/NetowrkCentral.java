package com.eaglesakura.android.framework.net;

import android.graphics.Bitmap;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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

/**
 * ネットワーク処理の中枢系
 */
public class NetowrkCentral {

    public static final long IMAGE_NO_TIMEOUT = 0x6FFFFFFFFFFFFFFFL;

    private static RequestQueue requests;

    private static final Object lock = new Object();

    /**
     * UIスレッドから呼び出しを行うためのタスクキュー
     */
    private static MultiRunningTasks tasks = new MultiRunningTasks(3);

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

        public File cacheDb = null;

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
     * 非同期に読み込みを行わせる
     *
     * @param url
     * @param option
     * @param uiListener
     */
    public static void getCachedImageAsync(final String url, final ImageOption option, final ImageListener uiListener) {
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
    public static Bitmap getCachedImage(String url, ImageOption option) throws IOException {
        // オプションを組み立てる
        if (option == null) {
            option = new ImageOption();
        }
        if (option.cacheDb == null) {
            option.cacheDb = new File(FrameworkCentral.getApplication().getCacheDir(), "netimage.db");
        }
        option.cacheDb.getParentFile().mkdirs();
        BlobKeyValueStore imageDb = new BlobKeyValueStore(FrameworkCentral.getApplication(), option.cacheDb);

        final String CACHE_IMAGE_KEY = EncodeUtil.genSHA1(url.getBytes());

        // キャッシュを読み込む
        try {
            imageDb.open();
            Bitmap cache = imageDb.getImage(CACHE_IMAGE_KEY, option.cacheTimeoutMs);
            if (cache != null) {
                LogUtil.log("Volley has cache(%s)", url);
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
                });
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
                imageDb.put(CACHE_IMAGE_KEY, bitmap, option.cacheFormat, option.cacheQuality);
            } finally {
                imageDb.close();
            }

            return responceHolder.get();
        }
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
    public static <T> T getSync(String url, final RequestParser<T> parser) throws IOException {
        if (AndroidUtil.isUIThread()) {
            throw new IllegalStateException("call background");
        }

        final Holder<T> responceHolder = new Holder<>();
        final Holder<Boolean> finishedHolder = new Holder<>();

        Request<T> request = new Request<T>(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                log(volleyError);
                finishedHolder.set(true);
            }
        }) {
            @Override
            protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
                try {
                    return Response.success(parser.parse(networkResponse), getCacheEntry());
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
        T parse(NetworkResponse response) throws Exception;
    }
}
