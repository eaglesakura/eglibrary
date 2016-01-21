package com.eaglesakura.android.net.internal;

import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.NetworkConnector;
import com.eaglesakura.android.net.cache.ICacheWriter;
import com.eaglesakura.android.net.request.ConnectContent;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.android.net.parser.RequestParser;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.thread.async.error.TaskCanceledException;
import com.eaglesakura.android.thread.async.error.TaskException;
import com.eaglesakura.android.thread.async.error.TaskFailedException;
import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.LogUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;

/**
 * Google Http Clientで接続試行を行う
 */
public class GoogleHttpClientConnectImpl<T> extends BaseHttpConnection<T> {
    static HttpRequestFactory requestFactory = AndroidHttp.newCompatibleTransport().createRequestFactory();

    public GoogleHttpClientConnectImpl(NetworkConnector connector, ConnectRequest request, RequestParser<T> parser) {
        super(connector, request, parser);
    }

    private HttpContent newContent() throws IOException {
        final ConnectContent rawContent = request.getContent();
        if (rawContent == null || rawContent.getLength() <= 0) {
            return null;
        }

        HttpContent conotent = new HttpContent() {
            @Override
            public long getLength() throws IOException {
                return rawContent.getLength();
            }

            @Override
            public String getType() {
                return rawContent.getContentType();
            }

            @Override
            public boolean retrySupported() {
                return false;
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                byte[] buffer = new byte[1024 * 128];

                InputStream raw = null;
                try {
                    raw = rawContent.openStream();
                    int ioSize = 0;

                    // すべてをストリームに送る
                    while ((ioSize = raw.read(buffer)) > 0) {
                        out.write(buffer, 0, ioSize);
                    }

                } finally {
                    IOUtil.close(raw);
                }
            }
        };

        return conotent;
    }

    private HttpRequest newRequest() throws IOException {
        String rawUrl = request.getUrl();
        GenericUrl url = new GenericUrl(rawUrl);
        switch (request.getMethod()) {
            case GET:
                return requestFactory.buildGetRequest(url);
            case POST:
                return requestFactory.buildPostRequest(url, newContent());
            case HEAD:
                return requestFactory.buildHeadRequest(url);
            case DELETE:
                return requestFactory.buildDeleteRequest(url);
            case PUT:
                return requestFactory.buildPutRequest(url, newContent());
        }
        throw new IOException("Method Not Support!! :: " + request.getMethod());
    }

    static void close(HttpResponse resp) {
        try {
            if (resp != null) {
                resp.disconnect();
            }
        } catch (Exception e) {

        }
    }

    private HttpHeader wrapHeader(HttpHeaders raw) {
        HttpHeader result = new HttpHeader();
        result.put(HttpHeader.HEADER_CONTENT_TYPE, raw.getContentType());
        result.put(HttpHeader.HEADER_ETAG, raw.getETag());
        result.put(HttpHeader.HEADER_CONTENT_RANGE, raw.getContentRange());
        if (raw.getContentLength() != null) {
            result.put(HttpHeader.HEADER_CONTENT_LENGTH, String.valueOf(raw.getContentLength()));
        }
        return result;
    }

    private HttpHeaders wrapHeader() {
        HttpHeader rawHeader = request.getHeader();
        HttpHeaders result = new HttpHeaders();
        if (rawHeader != null) {
            Iterator<Map.Entry<String, String>> iterator = rawHeader.getValues().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;


//        if (rawHeader.get(HttpHeader.HEADER_AUTHORIZATION) != null) {
//            result.setAuthorization(rawHeader.get(HttpHeader.HEADER_AUTHORIZATION));
//        }
//
//        if (rawHeader.get(HttpHeader.HEADER_CONTENT_RANGE) != null) {
//            result.setContentRange(rawHeader.get(HttpHeader.HEADER_CONTENT_RANGE));
//        }
//
    }

    @Override
    protected T tryNetworkParse(AsyncTaskResult<T> taskResult, MessageDigest digest) throws IOException, TaskException {
        HttpRequest req;
        HttpResponse resp = null;
        InputStream readContent = null;
        ICacheWriter cacheWriter = null;
        T result = null;
        HttpHeader respHeader = null;
        try {
            req = newRequest();
            req.setReadTimeout((int) request.getReadTimeoutMs());
            req.setConnectTimeout((int) request.getConnectTimeoutMs());
            req.setFollowRedirects(true);
            req.setHeaders(wrapHeader());

            resp = req.execute();
            readContent = resp.getContent();
            respHeader = wrapHeader(resp.getHeaders());
            final int status = resp.getStatusCode();

            if (taskResult.isCanceled()) {
                throw new TaskCanceledException();
            }

            if (status == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
                throw new FileNotFoundException("Status Code == 404");
            } else if ((status / 100) != 2) {
                // 200番台以外のステータスコードは例外となる
                throw new IOException("Status Code != 200 -> " + status);
            }

            cacheWriter = newCacheWriter(respHeader);

            // コンテンツのパースを行わせる
            try {
                result = parseFromStream(taskResult, respHeader, readContent, cacheWriter, digest);
                return result;
            } catch (IOException e) {
                throw e;
            } catch (TaskException e) {
                throw e;
            } catch (Exception e) {
                throw new TaskFailedException(e);
            } finally {
                if (result == null) {
                    LogUtil.log("parse failed");
                }
            }
        } finally {
            IOUtil.close(readContent);
            close(resp);
            closeCacheWriter(result, cacheWriter);
        }
    }
}
