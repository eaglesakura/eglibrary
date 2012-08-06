package com.eaglesakura.lib.gdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.net.WebAPIConnection;
import com.eaglesakura.lib.net.WebAPIException;
import com.eaglesakura.lib.net.WebAPIException.Type;

/**
 * GoogleのOAuth2で接続を行うためのヘルパ
 * トークンのリセット要請が行われた場合、自動的にトークンは再生成される。
 * その際、リスナがよばれるため、トークン保存を忘れないこと。
 * @author TAKESHI YAMASHITA
 *
 */
public class GoogleAPIConnector {

    /**
     * アクセストークン
     */
    String accessToken;

    /**
     * リセット用のトークン
     */
    String refreshToken;

    /**
     * クライアントID
     */
    String clientId;

    /**
     * クライアント用のキー
     */
    String clientSecret;

    /**
     * リトライ間隔
     */
    int maxRetry = 10;

    /**
     * タイムアウト時間
     */
    int connectTimeoutMs = 1000 * 30;

    TokenListener listener;

    public GoogleAPIConnector(String clientId, String clientSecret, String accessToken, String refreshToken,
            TokenListener listener) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.listener = listener;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * アクセストークンをリセットする
     * @return 新しいトークン
     */
    public synchronized String refreshAccessToken() throws WebAPIException {
        GoogleOAuth2Helper.AuthToken token = GoogleOAuth2Helper.refreshAuthToken(clientId, clientSecret, refreshToken);
        //        LogUtil.log("refreshed token = " + token.access_token);
        LogUtil.log("refreshed token sha1 :: " + GameUtil.genSHA1(token.access_token.getBytes()));
        accessToken = token.access_token;
        listener.onAccessTokenReset(this, token.access_token);
        return accessToken;
    }

    /**
     * リトライの回数制限を設定する
     * @param maxRetry
     */
    public void setMaxRetry(int maxRetry) {
        this.maxRetry = Math.max(1, maxRetry);
    }

    /**
     * タイムアウト時間を設定する
     * @param connectTimeoutMs
     */
    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.connectTimeoutMs = Math.max(1, connectTimeoutMs);
    }

    HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection result = (HttpURLConnection) new URL(url).openConnection();
        result.setRequestProperty("Authorization", "OAuth " + accessToken); // OAUth2 アクセストークン
        result.setRequestMethod(method);
        result.setConnectTimeout(connectTimeoutMs);
        return result;
    }

    String createURL(String url, Map<String, String> argments) {
        if (argments != null && !argments.isEmpty()) {
            // 引数を追加する
            try {
                url += "?";
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

    /**
     * レスポンスをチェックして内容を確認する
     * @param responseCode
     * @throws GoogleAPIException
     */
    private void _checkResponse(int responseCode) throws WebAPIException {
        switch (responseCode) {
        // 認証エラーには例外を返す
            case 401:
            case 403: {
                throw new WebAPIException("Responce Error", Type.AuthError);
            }
            case 503:
            case 502:
            case 500: {
                throw new WebAPIException("Server Error", Type.APIResponseError);
            }
        }
    }

    /**
     * 実際のGET操作を行う
     * @param url
     * @param argments
     * @throws GoogleAPIException
     */
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

    /**
     * 実際のGET操作を行う
     * @param url
     * @param argments
     * @throws GoogleAPIException
     */
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
     * インメモリの内容を全て送信する
     * @param url
     * @param properties
     * @param buffer
     * @return
     * @throws GoogleAPIException
     */
    public WebAPIConnection postOrPut(String url, String method, Map<String, String> properties, byte[] buffer)
            throws WebAPIException {
        for (int i = 0; i < maxRetry; ++i) {
            try {
                WebAPIConnection conn = _postOrPut(url, method, properties, -1, -1, buffer);
                return conn;
            } catch (WebAPIException e) {
                if (i == (maxRetry - 1)) {
                    throw e;
                }

                if (e.getType() == Type.AuthError) {
                    // 認証エラーだったら、再度トークンを発行する
                    LogUtil.log("token refresh");
                    refreshAccessToken();
                } else if (e.getType() == Type.APIResponseError) {
                    // 適当なウェイトをかけてもう一度コールする
                    LogUtil.log("APIError sleep...");
                    GameUtil.sleep(1000 + 500 * (i + 1));
                } else {
                    throw e;
                }
            }
        }
        throw new WebAPIException("connection failed...", Type.Unknown);
    }

    /**
     * GET操作を行う
     * @param url
     * @param argments
     * @return
     * @throws GoogleAPIException
     */
    public WebAPIConnection get(String url, Map<String, String> argments) throws WebAPIException {
        for (int i = 0; i < maxRetry; ++i) {
            try {
                WebAPIConnection conn = _get(url, argments, -1, -1);
                return conn;
            } catch (WebAPIException e) {
                if (i == (maxRetry - 1)) {
                    throw e;
                }

                if (e.getType() == Type.AuthError) {
                    // 認証エラーだったら、再度トークンを発行する
                    LogUtil.log("token refresh");
                    refreshAccessToken();
                } else if (e.getType() == Type.APIResponseError) {
                    // 適当なウェイトをかけてもう一度コールする
                    LogUtil.log("APIError");
                    GameUtil.sleep(1000 + 500 * (i + 1));
                } else {
                    throw e;
                }
            }
        }

        LogUtil.log("retry > maxRetry...");
        throw new WebAPIException("connection failed...", Type.Unknown);
    }

    /**
     * ダウンロード用のGET操作を行う
     * @param url
     * @param argments
     * @return
     * @throws GoogleAPIException
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
                if (i == (maxRetry - 1)) {
                    throw e;
                }

                if (e.getType() == Type.AuthError) {
                    // 認証エラーだったら、再度トークンを発行する
                    LogUtil.log("token refresh");
                    refreshAccessToken();
                } else if (e.getType() == Type.APIResponseError) {
                    // 適当なウェイトをかけてもう一度コールする
                    LogUtil.log("APIError");
                    GameUtil.sleep(500 * (i + 1));
                } else {
                    throw e;
                }
            }
        }

        throw new WebAPIException("connection failed...", Type.Unknown);
    }

    /**
     * APIの制御を受け取る
     */
    public interface TokenListener {
        /**
         * トークンがリセットされた場合に呼び出される。
         * @param newToken
         */
        void onAccessTokenReset(GoogleAPIConnector connector, String newToken);
    }

    /**
     * アップロード中の制御を行う
     * @author TAKESHI YAMASHITA
     *
     */
    public interface UploadCallback {
        /**
         * 指定した範囲の内容をdstに書き込む
         * @param buffer
         * @param startIndex 読み込みたい開始index {@link InputStream#skip(long)}を行える。
         * @param requestLength 読み込みたいバイト数
         * @return 読み込めたバイト数
         * @throws IOException
         */
        int getUploadSource(byte[] dst, long startIndex, long requestLength) throws IOException;

        /**
         * アップロードサイズを取得する
         * @return
         */
        int getUploadSize();

        /**
         * キャンセルを行う場合はtrueを返す
         * @return
         */
        int isCanceled();
    }
}
