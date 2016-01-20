package com.eaglesakura.android.net.cache;

import com.eaglesakura.android.net.request.ConnectRequest;

/**
 * キャッシュ制御用の
 */
public class CachePolicy {
    /**
     * キャッシュとして許す最大容量
     * <p/>
     * デフォルトで1MBを許す。
     */
    private long maxItemBytes = 1024 * 1024;

    /**
     * キャッシュの有効時間
     */
    private long cacheLimitTimeMs = 0;


    /**
     * キャッシュに登録する最大バイトサイズを取得する
     *
     * @return
     */
    public long getMaxItemBytes() {
        return maxItemBytes;
    }

    /**
     * キャッシュに登録する最大バイトサイズ指定
     *
     * @param maxItemBytes
     */
    public void setMaxItemBytes(long maxItemBytes) {
        this.maxItemBytes = maxItemBytes;
    }

    /**
     * キャッシュとして有効な最大時間を取得する
     *
     * @return
     */
    public long getCacheLimitTimeMs() {
        return cacheLimitTimeMs;
    }

    /**
     * キャッシュとして有効な最大時間を指定する
     *
     * @return
     */
    public void setCacheLimitTimeMs(long cacheLimitTimeMs) {
        this.cacheLimitTimeMs = cacheLimitTimeMs;
    }

    /**
     * キャッシュのキーとなる文字列を生成する。
     * <p/>
     * 基本的にはメソッド＋URLで生成される。
     *
     * @param request
     * @return
     */
    public String getCacheKey(ConnectRequest request) {
        return String.format("%s/%s", request.getMethod().toString(), request.getUrl());
    }

    public static long getCacheLimitTimeMs(CachePolicy policy) {
        if (policy == null) {
            return 0;
        } else {
            return policy.getCacheLimitTimeMs();
        }
    }
}
