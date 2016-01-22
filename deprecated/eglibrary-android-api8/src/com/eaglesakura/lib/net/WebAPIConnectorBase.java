package com.eaglesakura.lib.net;

import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.net.WebAPIException.Type;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class WebAPIConnectorBase {

    /**
     * リトライ回数上天
     */
    int maxRetry = 10;

    /**
     * タイムアウト上限
     */
    int connectTimeoutMs = 1000 * 30;

    public WebAPIConnectorBase() {
        super();
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = Math.max(1, maxRetry);
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        //        this.connectTimeoutMs = connectTimeoutMs;
        this.connectTimeoutMs = Math.max(1, connectTimeoutMs);
    }

    protected HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection result = (HttpURLConnection) new URL(url).openConnection();
        result.setRequestMethod(method);
        result.setConnectTimeout(connectTimeoutMs);
        result.setReadTimeout(connectTimeoutMs);
        return result;
    }

    /**
     * 接続用のURLを生成する
     */
    String createURL(String url, Map<String, String> argments) {
        if (argments != null && !argments.isEmpty()) {
            // 引数を追加する
            try {
                if (url.indexOf('?') > 0) {
                    url += "?";
                } else {
                    url += "&";
                }
                Iterator<Entry<String, String>> iterator = argments.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<String, String> entry = iterator.next();
                    url += (entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8"));
                    if (iterator.hasNext()) {
                        url += "&";
                    }
                }
            } catch (Exception e) {
            }

            return url;
        } else {
            return url;
        }
    }

    private void _checkResponse(int responseCode) throws WebAPIException {
        switch (responseCode) {
            // 認証エラーには例外を返す
            case 401:
            case 403: {
                throw new WebAPIException("Responce Error", responseCode, Type.AuthError);
            }
            case 503:
            case 502:
            case 500: {
                throw new WebAPIException("Server Error", responseCode, Type.APIResponseError);
            }
        }
    }

    private WebAPIConnection _get(String url, Map<String, String> argments, long rangeBegin, long rangeEnd)
            throws WebAPIException {
        url = createURL(url, argments);
        HttpURLConnection connection = null;
        try {
            connection = createConnection(url, "GET");
            connection.setDoInput(true);

            // Rangeヘッダが必要
            if (rangeBegin >= 0 && rangeEnd >= 0 && rangeBegin < rangeEnd) {
                connection.setRequestProperty("Range", "bytes=" + rangeBegin + "-" + rangeEnd);
            }

            connection.connect();
            int responseCode = connection.getResponseCode();

            LogUtil.log("ResponseCode = " + responseCode);
            _checkResponse(responseCode);

            // 正常にコネクションを開いた
            WebAPIConnection result = new WebAPIConnection(responseCode, connection);
            return result;
        } catch (Exception e) {
            // コネクションを閉じておく
            if (connection != null) {
                try {
                    InputStream is = connection.getErrorStream();
                    LogUtil.log("connect error = " + new String(GameUtil.toByteArray(is)));
                } catch (Exception ee) {

                }
                try {
                    connection.disconnect();
                    connection = null;
                } catch (Exception __e) {

                }
            }
            if (e instanceof WebAPIException) {
                throw (WebAPIException) e;
            } else {
                LogUtil.log(e);
                throw new WebAPIException(e);
            }
        }
    }

    private WebAPIConnection _postOrPut(String url, String method, Map<String, String> property, long rangeBegin,
                                        long rangeEnd, byte[] body) throws WebAPIException {
        HttpURLConnection connection = null;
        try {
            connection = createConnection(url, method);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if (property != null) {
                Iterator<Entry<String, String>> iterator = property.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<String, String> entry = iterator.next();
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.setRequestProperty("Content-Length", String.valueOf(body.length));

            // Rangeヘッダが必要
            /*
            if (rangeBegin >= 0 && rangeEnd >= 0 && rangeBegin < rangeEnd) {
                connection.setRequestProperty("Range", "bytes=" + rangeBegin + "-" + rangeEnd);
            }
            */

            connection.connect();

            // データを転送する
            {
                OutputStream stream = connection.getOutputStream();
                stream.write(body);
                stream.flush();
            }

            // 結果受け取る
            int responseCode = connection.getResponseCode();
            LogUtil.log("ResponseCode = " + responseCode);
            _checkResponse(responseCode);

            // 正常にコネクションを開いた
            WebAPIConnection result = new WebAPIConnection(responseCode, connection);
            return result;
        } catch (Exception e) {
            // コネクションを閉じておく
            if (connection != null) {
                try {
                    InputStream is = connection.getErrorStream();
                    LogUtil.log("connect error = " + new String(GameUtil.toByteArray(is)));
                } catch (Exception ee) {

                }
                try {
                    connection.disconnect();
                    connection = null;
                } catch (Exception __e) {

                }
            }
            if (e instanceof WebAPIException) {
                throw (WebAPIException) e;
            } else {
                throw new WebAPIException(e);
            }
        }
    }

    /**
     * エラーのハンドリングを行う。
     * ハンドリングに成功したらtrueを返す。それ以外の場合は例外を投げるか、falseを返す。
     */
    protected abstract boolean onAuthError(WebAPIException e) throws WebAPIException;

    /**
     * 例外のハンドリングを行う。
     */
    protected boolean handleException(WebAPIException e, int tryCount) throws WebAPIException {

        if (tryCount == (maxRetry - 1)) {
            throw e;
        }

        if (e.getType() == Type.AuthError) {
            // エラーのハンドリングに失敗したら例外を投げる
            if (!onAuthError(e)) {
                throw e;
            }
        } else if (e.getType() == Type.APIResponseError) {
            // 適当なウェイトをかけてもう一度コールする
            LogUtil.log("APIError sleep...");
            GameUtil.sleep(1000 + 500 * (tryCount + 1));
        } else {
            throw e;
        }
        return true;
    }

    /**
     * パラメータをpost | putする。
     * propertiesパラメーターに設定されている内容はリクエスト時のsetRequestProperty()メソッドに利用される
     */
    public WebAPIConnection postOrPut(String url, String method, Map<String, String> properties, byte[] buffer)
            throws WebAPIException {
        for (int i = 0; i < maxRetry; ++i) {
            try {
                WebAPIConnection conn = _postOrPut(url, method, properties, -1, -1, buffer);
                return conn;
            } catch (WebAPIException e) {
                // ハンドリングに失敗したら例外を投げる
                if (!handleException(e, i)) {
                    throw e;
                }
            }
        }
        throw new WebAPIException("connection failed...", Type.Unknown);
    }

    /**
     * GET操作を明示的に行う。argmentsは自動的にurl?key=valueに設定される。
     */
    public WebAPIConnection get(String url, Map<String, String> argments) throws WebAPIException {
        for (int i = 0; i < maxRetry; ++i) {
            try {
                WebAPIConnection conn = _get(url, argments, -1, -1);
                return conn;
            } catch (WebAPIException e) {
                if (!handleException(e, i)) {
                    throw e;
                }
            }
        }

        LogUtil.log("retry > maxRetry...");
        throw new WebAPIException("connection failed...", Type.Unknown);
    }

    /**
     * ダウンロードを明示的に行わせる。
     * 200 | 206以外が帰ってきた場合は例外として扱う。
     */
    public WebAPIConnection download(String url, long rangeBegin, long rangeEnd) throws WebAPIException {
        for (int i = 0; i < maxRetry; ++i) {
            try {
                WebAPIConnection conn = _get(url, null, rangeBegin, rangeEnd);
                if (conn.getResponceCode() == 200 || conn.getResponceCode() == 206) {
                    return conn;
                } else {
                    conn.dispose();
                    throw new WebAPIException(conn.getResponceCode());
                }
            } catch (WebAPIException e) {
                if (!handleException(e, i)) {
                    throw e;
                }
            }
        }

        throw new WebAPIException("connection failed...", Type.Unknown);
    }

}