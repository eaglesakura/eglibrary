package com.eaglesakura.android.net.cache;

import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.request.ConnectRequest;

import java.io.IOException;

public interface CacheController {

    long CACHE_ONE_MINUTE = 1000 * 60;
    long CACHE_ONE_HOUR = CACHE_ONE_MINUTE * 60;
    long CACHE_ONE_DAY = CACHE_ONE_HOUR * 24;
    long CACHE_ONE_WEEK = CACHE_ONE_DAY * 7;
    long CACHE_ONE_MONTH = CACHE_ONE_WEEK * 4;
    long CACHE_ONE_YEAR = CACHE_ONE_DAY * 365;

    /**
     * キャッシュされたデータを開く
     * <p/>
     * キャッシュが見つからない、もしくはタイムアウトした場合はnullを返却する。
     * <p/>
     * 問題が発生した場合は例外を投げる。
     * このキャッシュはオンメモリに乗る程度の小さなデータを前提としており、巨大なファイルの場合は別途最適化されたキャッシュを用いることが前提となる。
     *
     * @param request
     * @return
     */
    byte[] getCacheOrNull(ConnectRequest request) throws IOException;

    /**
     * キャッシュされたデータを登録する
     *
     * @param request
     * @param buffer
     */
    void putCache(ConnectRequest request, HttpHeader respHeader, byte[] buffer);
}
