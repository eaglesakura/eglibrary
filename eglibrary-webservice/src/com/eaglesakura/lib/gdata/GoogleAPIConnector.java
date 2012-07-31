package com.eaglesakura.lib.gdata;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.gdata.GoogleAPIException.Type;

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
    public String refreshAccessToken() throws GoogleAPIException {
        GoogleOAuth2Helper.AuthToken token = GoogleOAuth2Helper.refreshAuthToken(clientId, clientSecret, refreshToken);
        LogUtil.log("refreshed token = " + token.access_token);
        accessToken = token.access_token;
        listener.onAccessTokenReset(token.access_token);
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
     * 実際のGET操作を行う
     * @param url
     * @param argments
     * @throws GoogleAPIException
     */
    private GoogleConnection _get(String url, Map<String, String> argments, long rangeBegin, long rangeEnd)
            throws GoogleAPIException {
        url = createURL(url, argments);
        HttpURLConnection connection = null;
        try {
            connection = createConnection(url, "GET");
            connection.setDoInput(true);

            // Rangeヘッダが必要
            if (rangeBegin >= 0 && rangeEnd >= 0 && rangeBegin > rangeEnd) {
                connection.setRequestProperty("Range", "bytes=" + rangeBegin + "-" + rangeEnd);
            }

            connection.connect();
            int responseCode = connection.getResponseCode();

            LogUtil.log("ResponseCode = " + responseCode);
            switch (responseCode) {
            // 認証エラーには例外を返す
                case 401:
                case 403: {
                    throw new GoogleAPIException("Responce Error", Type.AuthError);
                }
            }

            // 正常にコネクションを開いた
            GoogleConnection result = new GoogleConnection(responseCode, connection);
            return result;
        } catch (Exception e) {
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
            if (e instanceof GoogleAPIException) {
                throw (GoogleAPIException) e;
            } else {
                throw new GoogleAPIException(e);
            }
        }
    }

    /**
     * GET操作を行う
     * @param url
     * @param argments
     * @return
     * @throws GoogleAPIException
     */
    public GoogleConnection get(String url, Map<String, String> argments) throws GoogleAPIException {
        for (int i = 0; i < maxRetry; ++i) {
            try {
                GoogleConnection conn = _get(url, argments, -1, -1);
                return conn;
            } catch (GoogleAPIException e) {
                if (e.getType() == Type.AuthError) {
                    // 認証エラーだったら、再度トークンを発行する
                    LogUtil.log("token refresh");
                    refreshAccessToken();
                } else {
                    throw e;
                }
            }
        }

        throw new GoogleAPIException("connection failed...", Type.Unknown);
    }

    /**
     * GET操作を行う
     * @param url
     * @param argments
     * @return
     * @throws GoogleAPIException
     */
    public GoogleConnection download(String url, long rangeBegin, long rangeEnd) throws GoogleAPIException {
        for (int i = 0; i < maxRetry; ++i) {
            try {
                GoogleConnection conn = _get(url, null, rangeBegin, rangeEnd);
                if (conn.getResponceCode() == 200 || conn.getResponceCode() == 206) {
                    return conn;
                } else {
                    conn.dispose();
                    throw new GoogleAPIException(conn.getResponceCode());
                }
            } catch (GoogleAPIException e) {
                if (e.getType() == Type.AuthError) {
                    // 認証エラーだったら、再度トークンを発行する
                    LogUtil.log("token refresh");
                    refreshAccessToken();
                } else {
                    throw e;
                }
            }
        }

        throw new GoogleAPIException("connection failed...", Type.Unknown);
    }

    /**
     * APIの制御を受け取る
     */
    public interface TokenListener {
        /**
         * トークンがリセットされた場合に呼び出される。
         * @param newToken
         */
        void onAccessTokenReset(String newToken);
    }

    /**
     * 
     * @author TAKESHI YAMASHITA
     *
     */
    public class GoogleConnection extends DisposableResource {
        HttpURLConnection connection;

        /**
         * 正常系ストリーム
         */
        InputStream input;

        /**
         * エラーストリーム
         */
        InputStream error;

        /**
         * ステータスコード
         */
        int responceCode;

        /**
         * 
         * @param connection
         */
        GoogleConnection(int status, HttpURLConnection connection) {
            this.connection = connection;
            this.responceCode = status;
        }

        /**
         * HTTPのレスポンスコードを取得する
         * @return
         */
        public int getResponceCode() {
            return responceCode;
        }

        /**
         * 入力ストリームを取得する
         * @return
         * @throws IOException
         */
        public InputStream getInput() throws IOException {
            if (input == null) {
                input = connection.getInputStream();
            }
            return input;
        }

        @Override
        public void dispose() {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
            }
            try {
                if (error != null) {
                    error.close();
                }
            } catch (Exception e) {
            }

            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
            }

            input = null;
            error = null;
            connection = null;
        }
    }
}
