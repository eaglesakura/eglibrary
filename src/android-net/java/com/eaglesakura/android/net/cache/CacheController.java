package com.eaglesakura.android.net.cache;

import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.request.HttpConnectRequest;

import java.io.IOException;

public interface CacheController {

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
    byte[] getCacheOrNull(HttpConnectRequest request) throws IOException;

    /**
     * キャッシュされたデータを登録する
     *
     * @param request
     * @param buffer
     */
    void putCache(HttpConnectRequest request, HttpHeader respHeader, byte[] buffer);
}
