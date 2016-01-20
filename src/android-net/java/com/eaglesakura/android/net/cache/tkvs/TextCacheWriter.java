package com.eaglesakura.android.net.cache.tkvs;

import com.eaglesakura.android.db.DBOpenType;
import com.eaglesakura.android.db.TextKeyValueStore;
import com.eaglesakura.android.net.cache.ICacheWriter;
import com.eaglesakura.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TextCacheWriter implements ICacheWriter {
    /**
     * Base64エンコードを行う場合はtrue
     */
    final boolean encodeBase64;

    /**
     * データベース本体
     */
    final TextKeyValueStore kvs;

    /**
     * 保存用ストリーム
     */
    final ByteArrayOutputStream stream;

    /**
     * 保存用のキー
     */
    final String cacheKey;

    public TextCacheWriter(TextKeyValueStore kvs, String cacheKey, boolean encodeBase64) {
        this.encodeBase64 = encodeBase64;
        this.kvs = kvs;
        this.cacheKey = cacheKey;
        this.stream = new ByteArrayOutputStream(1024 * 16);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        stream.write(buffer, offset, length);
    }

    @Override
    public void commit() throws IOException {
        final byte[] buffer = stream.toByteArray();
        final String value;
        if (encodeBase64) {
            value = StringUtil.toString(buffer);
        } else {
            value = new String(buffer);
        }

        try {
            kvs.open(DBOpenType.Write);
            kvs.putDirect(cacheKey, value);
        } finally {
            kvs.close();
        }
    }

    @Override
    public void abort() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
