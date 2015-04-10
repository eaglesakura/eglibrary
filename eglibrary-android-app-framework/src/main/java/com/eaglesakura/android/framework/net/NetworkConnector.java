package com.eaglesakura.android.framework.net;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.eaglesakura.android.dao.net.DaoMaster;
import com.eaglesakura.android.dao.net.DaoSession;
import com.eaglesakura.android.dao.net.DbNetCache;
import com.eaglesakura.android.dao.net.DbNetCacheDao;
import com.eaglesakura.android.db.BaseDatabase;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.thread.MultiRunningTasks;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.util.LogUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * シンプルにNetのAPI接続を行えるようにするクラス
 * <p/>
 * 要件に応じてカスタマイズが行えるようにする。
 * <p/>
 * リクエストの発行と同期を別途行えるようになったため、通信・処理速度が向上する。
 */
public class NetworkConnector {
    private static RequestQueue requests;

    /**
     * UIスレッドから呼び出しを行うためのタスクキュー
     */
    private static MultiRunningTasks tasks = new MultiRunningTasks(3);

    static {
        requests = Volley.newRequestQueue(FrameworkCentral.getApplication());
        requests.start();

        tasks.setThreadName("NetworkConnector");
        tasks.setThreadPoolMode(false);
    }

    private final Context context;

    File cacheFile;

    CacheDatabase cacheDatabase;

    public NetworkConnector(Context context) {
        this.context = context.getApplicationContext();
        this.cacheFile = new File(this.context.getCacheDir(), "egl-net.db");
    }

    /**
     * キャッシュを削除する
     */
    public void deleteCacheDb() {
        Context context = FrameworkCentral.getApplication();
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
    public void cleanTimeoutCache() {
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
    public <T> NetworkResult<T> get(final String url, final RequestParser<T> parser, final long cacheTimeoutMs) {
        final NetworkResult<T> result = new NetworkResult<T>() {
            boolean loadFromCache() throws Exception {

                byte[] cache = getCache(url, cacheTimeoutMs);
                if (cache == null) {
                    return false;
                }

                T parse = parser.parse(cache);
                if (parse != null) {
                    onReceived(parse);
                    LogUtil.log("NetworkConnector hasCache(%s) %d bytes", url, cache.length);
                    return true;
                } else {
                    return false;
                }
            }

            void loadFromNetwork() {
                Request<T> volleyRequest = new Request<T>(Request.Method.GET, url, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        log(volleyError);
                        onError(volleyError);
                    }
                }) {
                    @Override
                    protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
                        try {
                            Response<T> result = Response.success(parser.parse(networkResponse.data), getCacheEntry());

                            // キャッシュに追加する
                            putCache(url, networkResponse.headers, "GET", Arrays.copyOf(networkResponse.data, networkResponse.data.length), dataTimeoutMs);
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
                requests.add(volleyRequest);
            }

            @Override
            void startDownloadFromBackground() {
                try {
                    if (loadFromCache()) {
                        // キャッシュからの読み込みに成功したらそこで終了
                        return;
                    }
                } catch (Exception e) {
                    LogUtil.log(e);
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
     * URLを指定してキャッシュを取得する
     *
     * @param url
     * @param timeoutMs
     * @return
     */
    protected byte[] getCache(String url, long timeoutMs) {
        CacheDatabase db = getCacheDatabase();
        try {
            db.open();
            DbNetCache cache = db.getIfExist(url);
            if (cache == null) {
                return null;
            }

            if ((cache.getCacheTime().getTime() + timeoutMs) < System.currentTimeMillis()) {
                // 現在時刻がリミット時刻を超えてしまっている
                db.delete(url);
                LogUtil.log("Drop Cache url(%s) %d ms over", url, timeoutMs);
                return null;
            }

            return cache.getBody();
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
        if (timeoutMs < 10) {
            // 一定よりも小さな時間のキャッシュは不要
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

    private static final int SUPPORTED_DATABASE_VERSION = 1;

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
        T parse(byte[] data) throws Exception;
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

    public static synchronized NetworkConnector getDefaultConnector() {
        if (defaultConnector == null) {
            defaultConnector = new NetworkConnector(FrameworkCentral.getApplication());
        }
        return defaultConnector;
    }
}
