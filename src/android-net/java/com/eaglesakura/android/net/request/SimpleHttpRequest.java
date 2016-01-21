package com.eaglesakura.android.net.request;

import com.eaglesakura.android.net.RetryPolicy;
import com.eaglesakura.android.net.cache.CachePolicy;
import com.eaglesakura.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleHttpRequest extends ConnectRequest {
    private Map<String, String> params = new HashMap<>();

    private String encoding = "UTF-8";

    private CachePolicy cachePolicy = new CachePolicy();

    private RetryPolicy retryPolicy = new RetryPolicy(10);

    public SimpleHttpRequest(Method method) {
        super(method);
    }

    public void setUrl(String url, Map<String, String> params) {
        this.url = url;
        if (params != null) {
            this.params = params;
        }
    }

    private String encodeParams() {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            try {
                result.append(URLEncoder.encode(entry.getKey(), encoding));
                result.append('=');
                result.append(URLEncoder.encode(entry.getValue(), encoding));
                result.append('&');
            } catch (Exception e) {

            }
        }
        return result.toString();
    }

    @Override
    public String getUrl() {
        if (getMethod() == Method.GET) {
            String params = encodeParams();
            if (StringUtil.isEmpty(params)) {
                // パラメータが無いのでそのまま返す
                return this.url;
            }

            // URLにパラメータを乗せる
            StringBuilder result = new StringBuilder(this.url);
            if (url.indexOf("?") < 0) {
                result.append('?');
            } else {
                result.append('&');
            }
            result.append(params);
            return result.toString();
        } else {
            return url;
        }
    }

    @Override
    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    @Override
    public ConnectContent getContent() {
        String paramString = encodeParams();
        final byte[] paramStringBytes = paramString.getBytes();
        return new ConnectContent() {
            @Override
            public long getLength() {
                return paramStringBytes.length;
            }

            @Override
            public InputStream openStream() throws IOException {
                return new ByteArrayInputStream(paramStringBytes);
            }

            @Override
            public String getContentType() {
                return "application/x-www-form-urlencoded; charset=" + encoding;
            }
        };
    }
}

