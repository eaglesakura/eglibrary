package com.eaglesakura.android.net;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.eaglesakura.android.dao.net.DaoMaster;
import com.eaglesakura.android.dao.net.DaoSession;
import com.eaglesakura.android.dao.net.DbNetCache;
import com.eaglesakura.android.dao.net.DbNetCacheDao;
import com.eaglesakura.android.db.BaseDatabase;
import com.eaglesakura.android.thread.MultiRunningTasks;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * シンプルにNetのAPI接続を行えるようにするクラス
 * <br>
 * 要件に応じてカスタマイズが行えるようにする。
 * <br>
 * リクエストの発行と同期を別途行えるようになったため、通信・処理速度が向上する。
 */
public class NetworkConnector {
    public static final long CACHE_ONE_MINUTE = 1000 * 60;
    public static final long CACHE_ONE_HOUR = CACHE_ONE_MINUTE * 60;
    public static final long CACHE_ONE_DAY = CACHE_ONE_HOUR * 24;
    public static final long CACHE_ONE_WEEK = CACHE_ONE_DAY * 7;
    public static final long CACHE_ONE_MONTH = CACHE_ONE_WEEK * 4;
    public static final long CACHE_ONE_YEAR = CACHE_ONE_DAY * 365;


    private static RequestQueue requests;

    /**
     * UIスレッドから呼び出しを行うためのタスクキュー
     */
    private static MultiRunningTasks tasks = new MultiRunningTasks(3);

    private final Context context;

    File cacheFile;

    CacheDatabase cacheDatabase;

    NetworkFactory factory;

    public NetworkConnector(Context context) {
        context = context.getApplicationContext();
        synchronized (NetworkConnector.class) {
            if (requests == null) {
                requests = Volley.newRequestQueue(context);
                requests.start();

                tasks.setThreadName(getClass().getSimpleName());
                tasks.setThreadPoolMode(false);
            }
        }

        this.context = context.getApplicationContext();
        this.cacheFile = new File(this.context.getCacheDir(), "egl-net.db");

        this.factory = new NetworkFactory() {
            @Override
            public Map<String, String> newHttpHeaders(String url, String method) {
                return Collections.emptyMap();
            }

            @Override
            public RetryPolicy newRetryPolycy(final String url, String method) {
                return new DefaultRetryPolicy(1000, 10, 1.2f) {
                    @Override
                    public void retry(VolleyError error) throws VolleyError {
                        LogUtil.log("%s retry(%s)", NetworkConnector.this.getClass().getSimpleName(), url);
                        super.retry(error);
                    }
                };
            }
        };
    }

    /**
     * キャッシュを削除する
     */
    public void deleteCacheDb() {
        File volleyDir = new File(context.getCacheDir(), "volley");
        IOUtil.delete(volleyDir);
        volleyDir.mkdirs();

        CacheDatabase database = getCacheDatabase();
        try {
            database.open();
            database.drop();
        } finally {
            database.close();
        }
    }

    /**
     * 時間制限の切れたキャッシュをクリーンアップして軽量化する
     */
    public void deleteTimeoutCache() {
        CacheDatabase database = getCacheDatabase();
        try {
            database.open();
            database.deleteTimeoutCache();
        } finally {
            database.close();
        }
    }

    /**
     * ネットワーク経由でデータを取得する
     * <br>
     * iteratorが常にKey - Valueの順番で取得できることが前提となる
     *
     * @param url
     * @param parser
     * @param cacheTimeoutMs
     * @param <T>
     *
     * @return
     */
    public <T> NetworkResult<T> get(String url, RequestParser<T> parser, long cacheTimeoutMs) {
        return connect(url, parser, Request.Method.GET, cacheTimeoutMs, null);
    }

    public static Map<String, String> asMap(Collection<String> keyValues) {
        if (keyValues.size() % 2 != 0) {
            throw new IllegalArgumentException("keyValues size");
        }

        Map<String, String> result = new HashMap<>();
        Iterator<String> iterator = keyValues.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = iterator.next();

            result.put(key, value);
        }

        return result;
    }

    /**
     * ネットワーク経由でデータを送信する
     *
     * @param url
     * @param parser
     * @param cacheTimeoutMs
     * @param <T>
     *
     * @return
     */
    public <T> NetworkResult<T> post(String url, RequestParser<T> parser, Map<String, String> params, long cacheTimeoutMs) {
        return connect(url, parser, Request.Method.POST, cacheTimeoutMs, params);
    }

    /**
     * ネットワーク経由でデータを送信する
     *
     * @param url
     * @param parser
     * @param <T>
     *
     * @return
     */
    public <T> NetworkResult<T> post(String url, RequestParser<T> parser, Map<String, String> params) {
        return connect(url, parser, Request.Method.POST, 0, params);
    }

    /**
     * URLエラーが発生した場合のハンドリングResultを返す
     *
     * @param url
     * @param <T>
     *
     * @return
     */
    protected <T> NetworkResult<T> newUrlErrorResult(final String url) {
        return new NetworkResult<T>(url) {
            @Override
            void startDownloadFromBackground() {
                onError(new IllegalStateException("URL error :: " + url));
            }

            @Override
            void abortRequest() {

            }
        };
    }

    /**
     * ネットワーク経由でデータを取得する
     *
     * @param url
     * @param parser
     * @param cacheTimeoutMs
     * @param <T>
     *
     * @return
     */
    protected <T> NetworkResult<T> connect(final String url, final RequestParser<T> parser, final int volleyMethod, final long cacheTimeoutMs, final Map<String, String> params) {
        if (!url.startsWith("http")) {
            return newUrlErrorResult(url);
        }

        final NetworkResult<T> result = new NetworkResult<T>(url) {
            String httpMethod = volleyMethod == Request.Method.GET ? "GET" : "POST";

            Request<T> volleyRequest;

            NetworkResult<T> self() {
                return this;
            }

            boolean loadFromCache() throws Exception {
                DbNetCache cache = loadCache(url);

                if (cache == null) {
                    return false;
                }

                // キャッシュDrop前にハッシュを保存する
                oldDataHash = cache.getHash();

                if (dropTimeoutCache(cache, cacheTimeoutMs)) {
                    return false;
                }

                T parse = parser.parse(this, new ByteArrayInputStream(cache.getBody()));
                if (parse != null) {
                    currentDataHash = oldDataHash;
//                    LogUtil.log("data(%s) hash old(%s) -> new(%s) modified(false : local)",
//                            url,
//                            oldDataHash, currentDataHash
//                    );
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
                        log(volleyError);
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
                        return factory.newHttpHeaders(url, httpMethod);
                    }

                    @Override
                    protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
                        try {
                            LogUtil.log("%s volley received(%s) %.1f KB",
                                    NetworkConnector.this.getClass().getSimpleName(),
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
                volleyRequest.setRetryPolicy(factory.newRetryPolycy(url, httpMethod));
                requests.add(volleyRequest);
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
        };
        start(result);
        return result;
    }

    /**
     * バックグラウンド処理を行う
     *
     * @param result
     * @param <T>
     */
    protected <T> void start(final NetworkResult<T> result) {
        tasks.pushBack(new Runnable() {
            @Override
            public void run() {
                if (result.isCanceled()) {
                    return;
                }

                result.startDownloadFromBackground();
            }
        });
        tasks.start();
    }

    /**
     * タイムアウトチェックを行い、必要であればdropする
     *
     * @param cache
     * @param timeoutMs
     *
     * @return
     */
    protected boolean dropTimeoutCache(DbNetCache cache, long timeoutMs) {
        if ((cache.getCacheTime().getTime() + timeoutMs) < System.currentTimeMillis()) {
            // 現在時刻がリミット時刻を超えてしまっている
            LogUtil.log("%s dropCache(%s) %d ms over", getClass().getSimpleName(), cache.getUrl(), timeoutMs);

            CacheDatabase db = getCacheDatabase();
            try {
                db.open();
                db.delete(cache.getUrl());
            } finally {
                db.close();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * URLを指定してキャッシュを取得する
     *
     * @param url
     *
     * @return
     */
    protected DbNetCache loadCache(String url) {
        CacheDatabase db = getCacheDatabase();
        try {
            db.open();
            DbNetCache cache = db.get(url);
            // キャッシュが無効な場合は何もしない
            if (cache == null) {
                return null;
            }

            LogUtil.log("%s hasCache(%s) %.1f KB ETag(%s) hash(%s)",
                    getClass().getSimpleName(),
                    url,
                    (float) cache.getBodySize() / 1024.0f,
                    cache.getEtag(),
                    cache.getHash()
            );
            return cache;
        } catch (Exception e) {
            return null;
        } finally {
            db.close();
        }
    }

    private static final int CACHETYPE_DB = 0;
    private static final int CACHETYPE_FILE = 1;

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

        tasks.pushFront(new Runnable() {
            @Override
            public void run() {
                DbNetCache cache = new DbNetCache();
                cache.setUrl(url);
                cache.setBody(body);
                cache.setBodySize(body.length);
                cache.setCacheType(CACHETYPE_DB);
                cache.setMethod(method.toUpperCase());
                cache.setCacheTime(new Date());
                cache.setCacheLimit(new Date(System.currentTimeMillis() + timeoutMs));

                if (headers != null && !headers.isEmpty()) {
                    cache.setEtag(headers.get("ETag"));
                }
                cache.setHash(EncodeUtil.genMD5(body));

                CacheDatabase db = getCacheDatabase();
                try {
                    db.open();
                    db.put(cache);
                } finally {
                    db.close();
                }
            }
        });
        tasks.start();
    }

    /**
     * キャッシュ用DBを取得する
     *
     * @return
     */
    protected synchronized CacheDatabase getCacheDatabase() {
        if (cacheDatabase == null) {
            cacheDatabase = new CacheDatabase();
        }
        return cacheDatabase;
    }

    private static final int SUPPORTED_DATABASE_VERSION = 2;

    protected class CacheDatabase extends BaseDatabase<DaoSession> {
        public CacheDatabase() {
            super(NetworkConnector.this.context, DaoMaster.class);
        }

        public void delete(String url) {
            session.getDbNetCacheDao().deleteByKey(url);
        }

        /**
         * タイムアウトしているキャッシュを削除する
         */
        public void deleteTimeoutCache() {
            session.getDbNetCacheDao().queryBuilder()
                    .where(DbNetCacheDao.Properties.CacheLimit.le(System.currentTimeMillis()))
                    .buildDelete()
                    .executeDeleteWithoutDetachingEntities();
        }

        public void put(DbNetCache cache) {
            session.getDbNetCacheDao().insertOrReplace(cache);
        }

        /**
         * キャッシュを取得する
         *
         * @param url
         *
         * @return
         */
        public DbNetCache get(String url) {
            return session.getDbNetCacheDao().load(url);
        }

        /**
         * 有効期間が切れていないキャッシュを取得する
         *
         * @param url
         *
         * @return
         */
        public DbNetCache getIfExist(String url) {
            List<DbNetCache> list = session.getDbNetCacheDao().queryBuilder()
                    .where(DbNetCacheDao.Properties.Url.eq(url), DbNetCacheDao.Properties.CacheLimit.ge(System.currentTimeMillis()))
                    .limit(1)
                    .list();
            if (list.isEmpty()) {
                return null;
            } else {
                return list.get(0);
            }
        }

        @Override
        protected SQLiteOpenHelper createHelper() {
            return new SQLiteOpenHelper(context, cacheFile != null ? cacheFile.getAbsolutePath() : null, null, SUPPORTED_DATABASE_VERSION) {
                @Override
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    // バージョンアップしたら、ローカルキャッシュは削除してしまって差し支えない
                    LogUtil.log("%s db update(%d -> %d) drop", NetworkConnector.this.getClass().getSimpleName(), oldVersion, newVersion);
                    DaoMaster.dropAllTables(db, true);
                    DaoMaster.createAllTables(db, false);
                }

                @Override
                public void onCreate(SQLiteDatabase db) {
                    DaoMaster.createAllTables(db, false);
                }
            };
        }
    }

    /**
     * オブジェクトのパースを行う
     *
     * @param <T>
     */
    public interface RequestParser<T> {
        T parse(NetworkResult<T> sender, InputStream data) throws Exception;
    }

    private static void log(VolleyError error) {
        if (error.networkResponse != null) {
            LogUtil.log("Volley Error has resp(%s)", error.networkResponse.toString());
            if (error.networkResponse.headers != null) {
                LogUtil.log("Volley Error headers(%s)", error.networkResponse.headers.toString());
            }
        }
    }

    private static NetworkConnector defaultConnector;

    public static NetworkConnector getDefaultConnector() {
        synchronized (NetworkConnector.class) {
            if (defaultConnector == null) {
                throw new IllegalStateException();
            }
            return defaultConnector;
        }
    }

    public static void initializeDefaultConnector(Context context) {
        synchronized (NetworkConnector.class) {
            if (defaultConnector == null) {
                defaultConnector = new NetworkConnector(context);
            }
        }
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
        Map<String, String> newHttpHeaders(String url, String method);
    }


    /**
     * 何も返さないParser
     */
    public static RequestParser<Object> VOID_PARSER = new RequestParser<Object>() {
        @Override
        public Object parse(NetworkResult<Object> sender, InputStream data) throws Exception {
            return new Object();
        }
    };


    /**
     * 文字列にパースする
     */
    public static RequestParser<String> STRING_PARSER = new RequestParser<String>() {
        @Override
        public String parse(NetworkResult<String> sender, InputStream data) throws Exception {
            return new String(IOUtil.toString(data, false));
        }
    };

    /**
     * JSONを単純にパースする
     *
     * @param <T>
     */
    public static class JsonParser<T> implements RequestParser<T> {
        final Class<T> clazz;

        public JsonParser(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T parse(NetworkResult<T> sender, InputStream data) throws Exception {
            return JSON.decode(data, clazz);
        }
    }

    public static class ScaledImageParser implements RequestParser<Bitmap> {
        int maxWidth;
        int maxHeight;

        public ScaledImageParser(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        @Override
        public Bitmap parse(NetworkResult<Bitmap> sender, InputStream data) throws Exception {
            Bitmap bitmap = ImageUtil.decode(data);
            Bitmap scaled = ImageUtil.toScaledImage(bitmap, maxWidth, maxHeight);
            if (bitmap != scaled) {
                bitmap.recycle();
            }
            return scaled;
        }
    }

    public static class ScaledAlphaImageParser implements RequestParser<Bitmap> {
        int maxWidth;
        int maxHeight;
        int alphaImageDrawable;
        final Context context;

        public ScaledAlphaImageParser(final Context context, int maxWidth, int maxHeight, int alphaImageDrawable) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.context = context.getApplicationContext();
            this.alphaImageDrawable = alphaImageDrawable;
        }

        @Override
        public Bitmap parse(NetworkResult<Bitmap> sender, InputStream data) throws Exception {
            Bitmap bitmap = ImageUtil.decode(data);
            Bitmap scaled = ImageUtil.toScaledImage(bitmap, maxWidth, maxHeight);
            if (bitmap != scaled) {
                bitmap.recycle();
            }

            Bitmap alpha = ImageUtil.decode(context, alphaImageDrawable);
            Bitmap blend = ImageUtil.blendAlpha(scaled, alpha);
            scaled.recycle();
            alpha.recycle();

            return blend;
        }
    }
}
