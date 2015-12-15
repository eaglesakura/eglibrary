package com.eaglesakura.android.net;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.eaglesakura.android.dao.net.DaoMaster;
import com.eaglesakura.android.dao.net.DaoSession;
import com.eaglesakura.android.dao.net.DbFileBlock;
import com.eaglesakura.android.dao.net.DbFileBlockDao;
import com.eaglesakura.android.dao.net.DbNetCache;
import com.eaglesakura.android.dao.net.DbNetCacheDao;
import com.eaglesakura.android.db.BaseDatabase;
import com.eaglesakura.thread.MultiRunningTasks;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.query.CloseableListIterator;

/**
 * シンプルにNetのAPI接続を行えるようにするクラス
 * <br>
 * 要件に応じてカスタマイズが行えるようにする。
 * <br>
 * リクエストの発行と同期を別途行えるようになったため、通信・処理速度が向上する。
 */
public class NetworkConnector {

    /**
     * 1ブロックのデータサイズ
     * 検証時は小さくしておく
     */
    static final int BLOCK_SIZE = 1024 * 128;

    public static final long CACHE_ONE_MINUTE = 1000 * 60;
    public static final long CACHE_ONE_HOUR = CACHE_ONE_MINUTE * 60;
    public static final long CACHE_ONE_DAY = CACHE_ONE_HOUR * 24;
    public static final long CACHE_ONE_WEEK = CACHE_ONE_DAY * 7;
    public static final long CACHE_ONE_MONTH = CACHE_ONE_WEEK * 4;
    public static final long CACHE_ONE_YEAR = CACHE_ONE_DAY * 365;

    public enum RequestType {
        Volley,
        GoogleHttpClient,
    }


    static RequestQueue requests;

    /**
     * UIスレッドから呼び出しを行うためのタスクキュー
     */
    static MultiRunningTasks cacheWorkTask = new MultiRunningTasks(1);

    /**
     * ネットワーク処理を行うためのタスクキュー
     */
    static MultiRunningTasks netDownloadTask = new MultiRunningTasks(3);

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

                cacheWorkTask.setThreadName(getClass().getSimpleName() + "/Work");
                cacheWorkTask.setThreadPoolMode(false);

                netDownloadTask.setThreadName(getClass().getSimpleName() + "/Net");
                netDownloadTask.setThreadPoolMode(false);
            }
        }

        this.context = context.getApplicationContext();
        this.cacheFile = new File(this.context.getCacheDir(), "egl-net.db");

        this.factory = new NetworkFactory() {
            HttpRequestFactory requestFactory = AndroidHttp.newCompatibleTransport().createRequestFactory();

            @Override
            public Map<String, String> newHttpHeaders(String url, String method) {
                return Collections.emptyMap();
            }

            @Override
            public RetryPolicy newRetryPolycy(final String url, String method) {
                return new DefaultRetryPolicy(5000, 10, 1.2f) {
                    @Override
                    public void retry(VolleyError error) throws VolleyError {
                        LogUtil.log("%s retry(%s)", NetworkConnector.this.getClass().getSimpleName(), url);
                        super.retry(error);
                    }
                };
            }

            @Override
            public HttpRequest newLargeRequest(String url, String method) {
                method = method.toUpperCase();

                try {
                    if ("GET".equals(method)) {
                        return requestFactory.buildGetRequest(new GenericUrl(url));
                    } else if ("POST".equals(method)) {
                        return requestFactory.buildPostRequest(new GenericUrl(url), null);
                    }
                } catch (Exception e) {
                    LogUtil.log(e);
                }

                return null;
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
     *
     * @param url
     * @param parser
     * @param cacheTimeoutMs
     * @param <T>
     * @return
     */
    public <T> NetworkResult<T> get(String url, RequestParser<T> parser, long cacheTimeoutMs) {
        return get(url, parser, cacheTimeoutMs, RequestType.GoogleHttpClient);
    }

    /**
     * 保存ファイルを指定してDLする
     *
     * @param url
     * @param parser
     * @param downloadFile
     * @param <T>
     * @return
     */
    public <T> NetworkResult<T> get(String url, RequestParser<T> parser, final long cacheTimeoutMs, File downloadFile) {
        if (url == null || !url.startsWith("http")) {
            return newUrlErrorResult(url);
        }

        LargeNetworkResult<T> result = new LargeNetworkResult<>(
                url,
                this,
                factory.newLargeRequest(url, "GET"),
                parser,
                new IConnectParams() {
                    @Override
                    public long getCacheTimeoutMs() {
                        return cacheTimeoutMs;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        return new HashMap<>();
                    }

                    @Override
                    public Map<String, String> getRequestParams() {
                        return new HashMap<>();
                    }

                    @Override
                    public byte[] getPostBuffer() {
                        return null;
                    }
                },
                downloadFile
        );
        start(result);
        return get(url, parser, cacheTimeoutMs, RequestType.GoogleHttpClient);
    }

    /**
     * ネットワーク経由でデータを取得する
     *
     * @param url
     * @param parser
     * @param cacheTimeoutMs
     * @param type
     * @param <T>
     * @return
     */
    public <T> NetworkResult<T> get(String url, RequestParser<T> parser, final long cacheTimeoutMs, RequestType type) {
        return get(url, parser, new IConnectParams() {
            @Override
            public long getCacheTimeoutMs() {
                return cacheTimeoutMs;
            }

            @Override
            public Map<String, String> getHeaders() {
                return new HashMap<>();
            }

            @Override
            public Map<String, String> getRequestParams() {
                return new HashMap<>();
            }

            @Override
            public byte[] getPostBuffer() {
                return null;
            }
        }, type);
    }

    /**
     * ネットワーク経由でデータを取得する
     *
     * @param url
     * @param parser
     * @param params
     * @param type
     * @param <T>
     * @return
     */
    public <T> NetworkResult<T> get(String url, RequestParser<T> parser, final IConnectParams params, RequestType type) {
        if (url == null || !url.startsWith("http")) {
            return newUrlErrorResult(url);
        }

        if (type == RequestType.GoogleHttpClient) {
            LargeNetworkResult<T> result = new LargeNetworkResult<>(
                    url,
                    this,
                    factory.newLargeRequest(url, "GET"),
                    parser,
                    params,
                    null
            );

            start(result);
            return result;
        } else {
            return connect(url, parser, Request.Method.GET, params);
        }
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
     * @return
     */
    public <T> NetworkResult<T> post(String url, RequestParser<T> parser, final Map<String, String> params, final long cacheTimeoutMs) {
        return connect(url, parser, Request.Method.POST, new IConnectParams() {
            @Override
            public long getCacheTimeoutMs() {
                return cacheTimeoutMs;
            }

            @Override
            public Map<String, String> getHeaders() {
                return null;
            }

            @Override
            public Map<String, String> getRequestParams() {
                return params;
            }

            @Override
            public byte[] getPostBuffer() {
                return null;
            }
        });
    }

    /**
     * byte配列を直接POSTする
     *
     * @param url
     * @param parser
     * @param data
     * @param cacheTimeoutMs
     * @param <T>
     * @return
     */
    public <T> NetworkResult<T> post(String url, RequestParser<T> parser, final byte[] data, final long cacheTimeoutMs) {
        return connect(url, parser, Request.Method.POST, new IConnectParams() {
            @Override
            public long getCacheTimeoutMs() {
                return cacheTimeoutMs;
            }

            @Override
            public Map<String, String> getHeaders() {
                return new HashMap<>();
            }

            @Override
            public Map<String, String> getRequestParams() {
                return null;
            }

            @Override
            public byte[] getPostBuffer() {
                return data;
            }
        });
    }

    /**
     * 接続情報を取得する
     */
    public interface IConnectParams {
        long getCacheTimeoutMs();

        Map<String, String> getHeaders();

        /**
         * POST対象のkey-valueを取得する
         *
         * @return
         */
        Map<String, String> getRequestParams();

        /**
         * POST対象のbyte配列を取得する。
         * このメソッドは先に呼びだされ、nullの場合はgetPostParams()がチェックされる
         *
         * @return
         */
        byte[] getPostBuffer();
    }

    /**
     * ネットワーク経由でデータを送信する
     *
     * @param url
     * @param parser
     * @param <T>
     * @return
     */
    public <T> NetworkResult<T> post(String url, RequestParser<T> parser, final Map<String, String> params) {
        return connect(url, parser, Request.Method.POST, new IConnectParams() {
            @Override
            public long getCacheTimeoutMs() {
                return 0;
            }

            @Override
            public Map<String, String> getHeaders() {
                return new HashMap<>();
            }

            @Override
            public Map<String, String> getRequestParams() {
                return params;
            }

            @Override
            public byte[] getPostBuffer() {
                return null;
            }
        });
    }


    /**
     * ネットワーク経由でデータを送信する
     *
     * @param url
     * @param parser
     * @param <T>
     * @return
     */
    public <T> NetworkResult<T> post(String url, RequestParser<T> parser, IConnectParams params) {
        return connect(url, parser, Request.Method.POST, params);
    }


    /**
     * URLエラーが発生した場合のハンドリングResultを返す
     *
     * @param url
     * @param <T>
     * @return
     */
    protected <T> NetworkResult<T> newUrlErrorResult(final String url) {
        NetworkResult<T> result = new NetworkResult<T>(url) {
            @Override
            void startDownloadFromBackground() {
                onError(new IllegalStateException("URL error :: " + url));
            }

            @Override
            void abortRequest() {

            }
        };
        start(result);
        return result;
    }

    /**
     * ネットワーク経由でデータを取得する
     *
     * @param url
     * @param parser
     * @param <T>
     * @return
     */
    protected <T> NetworkResult<T> connect(final String url, final RequestParser<T> parser, final int volleyMethod, IConnectParams params) {
        if (url == null || !url.startsWith("http")) {
            return newUrlErrorResult(url);
        }
        NetworkResult<T> result = new VolleyNetworkResult<>(url, this, parser, volleyMethod, params);
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
        cacheWorkTask.pushBack(new Runnable() {
            @Override
            public void run() {
                if (result.isCanceled()) {
                    return;
                }

                result.startDownloadFromBackground();
            }
        });
        cacheWorkTask.start();
    }

    /**
     * タイムアウトチェックを行い、必要であればdropする
     *
     * @param cache
     * @param timeoutMs
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

    static final int CACHETYPE_DB = 0;
//    static final int CACHETYPE_FILE = 1;

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

    private static final int SUPPORTED_DATABASE_VERSION = 3;

    class CacheDatabase extends BaseDatabase<DaoSession> {
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
         * データを追記する
         *
         * @param block
         */
        public void put(DbFileBlock block) {
            session.getDbFileBlockDao().insert(block);
        }

        /**
         * キャッシュを取得する
         *
         * @param url
         * @return
         */
        public DbNetCache get(String url) {
            return session.getDbNetCacheDao().load(url);
        }

        /**
         * 有効期間が切れていないキャッシュを取得する
         *
         * @param url
         * @return
         */
        public DbNetCache getIfExist(String url) {
            List<DbNetCache> list = session.getDbNetCacheDao().queryBuilder()
                    .where(DbNetCacheDao.Properties.Url.eq(url), DbNetCacheDao.Properties.CacheLimit.ge(System.currentTimeMillis()))
                    .limit(1)
                    .list();
            if (list.isEmpty()) {
                cleanFileBlock(url);
                return null;
            } else {
                return list.get(0);
            }
        }

        public void cleanFileBlock(String url) {
            session.getDbFileBlockDao()
                    .queryBuilder()
                    .where(DbFileBlockDao.Properties.Url.eq(url))
                    .buildDelete()
                    .executeDeleteWithoutDetachingEntities();

        }

        public CloseableListIterator<DbFileBlock> listFileBlocks(String url) {
            CloseableListIterator<DbFileBlock> iterator = session.getDbFileBlockDao()
                    .queryBuilder()
                    .where(DbFileBlockDao.Properties.Url.eq(url))
                    .orderAsc(DbFileBlockDao.Properties.Number)
                    .listIterator();
            return iterator;
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

    static void log(VolleyError error) {
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

        /**
         * 大容量のデータを受け取る場合に利用するリクエスト
         *
         * @param url
         * @param method
         * @return
         */
        HttpRequest newLargeRequest(String url, String method);
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
     * byte配列に変換する
     */
    public static RequestParser<byte[]> BYTEARRAY_PARSER = new RequestParser<byte[]>() {
        @Override
        public byte[] parse(NetworkResult<byte[]> sender, InputStream data) throws Exception {
            return IOUtil.toByteArray(data, false);
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

    /**
     * コンテンツのRangeを指定してダウンロードする。
     *
     * @param header
     * @param offset
     * @param length
     * @return
     */
    public static String setRange(Map<String, String> header, long offset, long length) {
        String result = String.format("bytes=%d-%d", offset, (offset + length - 1));
        if (header != null) {
            header.put("Range", result);
        }
        return result;
    }

    /**
     * コンテンツ全体の長さを取得する
     *
     * @param headers
     * @return 不明な場合は0を返却する
     */
    public static long getContentFullSize(Map<String, String> headers) {
        String range = headers.get("Content-Range");
        String length = headers.get("Content-Length");
        try {
            if (!StringUtil.isEmpty(range)) {
                String[] split = range.split("/");
                return Long.parseLong(split[1]);
            }

            if (!StringUtil.isEmpty(length)) {
                return Long.parseLong(length);
            }
        } catch (Exception e) {
        }
        return 0;
    }
}
