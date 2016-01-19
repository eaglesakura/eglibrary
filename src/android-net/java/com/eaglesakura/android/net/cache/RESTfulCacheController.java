package com.eaglesakura.android.net.cache;

import android.content.Context;

import com.eaglesakura.android.db.DBOpenType;
import com.eaglesakura.android.db.TextKeyValueStore;
import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.request.HttpConnectRequest;

import java.io.File;
import java.io.IOException;

/**
 * RESTfulなAPI(テキストベースAPI）のキャッシュを制御する
 */
public class RESTfulCacheController implements CacheController {
    private final Context context;

    private final TextKeyValueStore kvs;

    public RESTfulCacheController(Context context) {
        this.context = context.getApplicationContext();

        File dbFile = new File(context.getCacheDir(), "es_net_cache.db");
        this.kvs = new TextKeyValueStore(this.context, dbFile, TextKeyValueStore.TABLE_NAME_DEFAULT);
    }

    @Override
    public byte[] getCacheOrNull(HttpConnectRequest request) throws IOException {
        CachePolicy policy = request.getCachePolicy();
        if (policy == null) {
            return null;
        }
        long timeMs = policy.getCacheLimitTimeMs();
        if (timeMs < 1000) {
            // 適当なしきい値以下のタイムアウトは実質的に無視すべき
            return null;
        }

        try {
            kvs.open(DBOpenType.Write);

            final String key = policy.getCacheKey(request);
            TextKeyValueStore.Data data = kvs.get(key);
            if (data == null) {
                return null;
            }

            if (System.currentTimeMillis() > (data.date + timeMs)) {
                // タイムアウト時間を超えていたら、キャッシュをローカルから削除する
                kvs.remove(key);
                return null;
            } else {
                // キャッシュが有効なので、それを利用する
                return data.value.getBytes();
            }
        } finally {
            kvs.close();
        }
    }

    @Override
    public void putCache(HttpConnectRequest request, HttpHeader respHeader, byte[] buffer) {
        CachePolicy policy = request.getCachePolicy();
        if (policy == null) {
            return;
        }

        long timeMs = policy.getCacheLimitTimeMs();
        if (timeMs < 1000) {
            // 適当なしきい値以下のタイムアウトは実質的に無視すべき
            return;
        }

        try {
            kvs.open(DBOpenType.Write);
            final String key = policy.getCacheKey(request);
            kvs.putDirect(key, new String(buffer));
        } finally {
            kvs.close();
        }
    }
}
