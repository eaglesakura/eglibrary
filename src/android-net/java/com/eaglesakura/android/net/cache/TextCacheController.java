package com.eaglesakura.android.net.cache;

import android.content.Context;

import com.eaglesakura.android.db.DBOpenType;
import com.eaglesakura.android.db.TextKeyValueStore;
import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.util.StringUtil;

import java.io.File;
import java.io.IOException;

/**
 * テキストベースAPIのキャッシュを制御する
 */
public class TextCacheController implements CacheController {
    private final Context context;

    private final TextKeyValueStore kvs;

    private boolean encodeBase64 = false;

    public TextCacheController(Context context) {
        this.context = context.getApplicationContext();

        File dbFile = new File(context.getCacheDir(), "es_net_cache.db");
        this.kvs = new TextKeyValueStore(this.context, dbFile, TextKeyValueStore.TABLE_NAME_DEFAULT);
    }

    /**
     * base64エンコードを行う場合true
     *
     * @param encodeBase64
     */
    public void setEncodeBase64(boolean encodeBase64) {
        this.encodeBase64 = encodeBase64;
    }

    private String toDdValue(byte[] buffer) {
        if (encodeBase64) {
            return StringUtil.toString(buffer);
        } else {
            return new String(buffer);
        }
    }

    private byte[] toByteArray(String dbText) {
        if (encodeBase64) {
            return StringUtil.toByteArray(dbText);
        } else {
            return dbText.getBytes();
        }
    }

    @Override
    public byte[] getCacheOrNull(ConnectRequest request) throws IOException {
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
                return toByteArray(data.value);
            }
        } finally {
            kvs.close();
        }
    }

    @Override
    public void putCache(ConnectRequest request, HttpHeader respHeader, byte[] buffer) {
        CachePolicy policy = request.getCachePolicy();
        if (policy == null) {
            return;
        }

        long timeMs = policy.getCacheLimitTimeMs();
        if (timeMs < 1000 || buffer.length > policy.getMaxItemBytes()) {
            // 適当なしきい値以下のタイムアウトは実質的に無視すべき
            // もしくは最大キャッシュサイズを超えていたら何もしない
            return;
        }

        try {
            kvs.open(DBOpenType.Write);
            final String key = policy.getCacheKey(request);
            String dbTest = toDdValue(buffer);
            kvs.putDirect(key, dbTest);
        } finally {
            kvs.close();
        }
    }
}
