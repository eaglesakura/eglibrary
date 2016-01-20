package com.eaglesakura.android.net.cache.tkvs;

import android.content.Context;

import com.eaglesakura.android.db.DBOpenType;
import com.eaglesakura.android.db.TextKeyValueStore;
import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.cache.CachePolicy;
import com.eaglesakura.android.net.cache.ICacheController;
import com.eaglesakura.android.net.cache.ICacheWriter;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * テキストベースAPIのキャッシュを制御する
 */
public class TextCacheController implements ICacheController {
    private final Context context;

    private final TextKeyValueStore kvs;

    private boolean encodeBase64 = false;

    public TextCacheController(Context context) {
        this.context = context.getApplicationContext();

        File dbFile = new File(context.getCacheDir(), "es_net_cache.db");
        this.kvs = new TextKeyValueStore(this.context, dbFile, TextKeyValueStore.TABLE_NAME_DEFAULT);
    }

    @Override
    public ICacheWriter newCacheWriter(ConnectRequest request, HttpHeader respHeader) throws IOException {
        CachePolicy policy = request.getCachePolicy();
        long timeMs = CachePolicy.getCacheLimitTimeMs(request.getCachePolicy());
        if (timeMs < 1000) {
            return null;
        }
        return new TextCacheWriter(kvs, policy.getCacheKey(request), encodeBase64);
    }

    @Override
    public InputStream openCache(ConnectRequest request) throws IOException {
        CachePolicy policy = request.getCachePolicy();
        long timeMs = CachePolicy.getCacheLimitTimeMs(request.getCachePolicy());
        if (timeMs < 1000) {
            // 適当なしきい値以下のタイムアウトは実質的に無視すべき
            throw new FileNotFoundException();
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
                return new ByteArrayInputStream(toByteArray(data.value));
            }
        } finally {
            kvs.close();
        }
    }

    /**
     * base64エンコードを行う場合true
     *
     * @param encodeBase64
     */
    public void setEncodeBase64(boolean encodeBase64) {
        this.encodeBase64 = encodeBase64;
    }

    private byte[] toByteArray(String dbText) {
        if (encodeBase64) {
            return StringUtil.toByteArray(dbText);
        } else {
            return dbText.getBytes();
        }
    }
}
