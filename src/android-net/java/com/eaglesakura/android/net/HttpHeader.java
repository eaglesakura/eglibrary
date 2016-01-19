package com.eaglesakura.android.net;

import com.eaglesakura.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class HttpHeader {
    Map<String, String> values = new HashMap<>();

    public static final String HEADER_ETAG = "ETag";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_RANGE = "Range";

    public HttpHeader() {
    }

    public HttpHeader put(String key, String value) {
        values.put(key, value);
        return this;
    }

    /**
     * コンテンツのRangeを指定してダウンロードする。
     *
     * @param offset
     * @param length
     * @return
     */
    public HttpHeader range(long offset, long length) {
        String result = String.format("bytes=%d-%d", offset, (offset + length - 1));
        values.put(HEADER_RANGE, result);
        return this;
    }

    /**
     * ダウンロードするコンテンツの最大サイズを取得する。
     *
     * @return
     */
    public long getContentFullSize() {
        String range = values.get(HEADER_CONTENT_RANGE);
        String length = values.get(HEADER_CONTENT_LENGTH);
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
        return -1;
    }

    /**
     * ヘッダを取得する
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return values.get(key);
    }

    public Map<String, String> getValues() {
        return values;
    }
}
