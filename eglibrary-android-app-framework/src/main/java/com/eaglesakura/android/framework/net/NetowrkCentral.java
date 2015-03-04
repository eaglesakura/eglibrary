package com.eaglesakura.android.framework.net;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.thread.Holder;

import java.io.IOException;

/**
 * ネットワーク処理の中枢系
 */
public class NetowrkCentral {

    private static RequestQueue requests;

    private static final Object lock = new Object();

    public static RequestQueue getVolleyRequests() {
        synchronized (lock) {
            if (requests == null) {
                start();
            }
            return requests;
        }
    }

    /**
     * 開始処理を行う
     */
    public static void start() {
        synchronized (lock) {
            if (requests == null) {
                requests = Volley.newRequestQueue(FrameworkCentral.getApplication());
                requests.start();
            }
        }
    }

    /**
     * 終了処理を行う
     */
    public static void cleanup() {
        synchronized (lock) {
            if (requests != null) {
                requests.stop();
                requests = null;
            }
        }
    }

    /**
     * 同期的にオブジェクトを取得する
     *
     * @param url
     * @param parser
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T getSync(String url, final RequestParser<T> parser) throws IOException {
        if (AndroidUtil.isUIThread()) {
            throw new IllegalStateException("call background");
        }

        final Holder<T> responceHolder = new Holder<>();
        final Holder<Boolean> finishedHolder = new Holder<>();

        Request<T> request = new Request<T>(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                finishedHolder.set(true);
            }
        }) {
            @Override
            protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
                try {
                    return Response.success(parser.parse(networkResponse), getCacheEntry());
                } catch (Exception e) {
                    return Response.error(new VolleyError("parse error"));
                }
            }

            @Override
            protected void deliverResponse(T object) {
                responceHolder.set(object);
                finishedHolder.set(true);
            }
        };
        getVolleyRequests().add(request);
        getVolleyRequests().start();

        finishedHolder.getWithWait();

        if (responceHolder.get() == null) {
            throw new IOException("Volley Resp Error");
        } else {
            return responceHolder.get();
        }
    }

    public interface RequestParser<T> {
        T parse(NetworkResponse response) throws Exception;
    }
}
