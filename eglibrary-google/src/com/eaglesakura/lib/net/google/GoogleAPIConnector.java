package com.eaglesakura.lib.net.google;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.net.WebAPIConnectorBase;
import com.eaglesakura.lib.net.WebAPIException;

/**
 * GoogleのOAuth2で接続を行うためのヘルパ
 * トークンのリセット要請が行われた場合、自動的にトークンは再生成される。
 * その際、リスナがよばれるため、トークン保存を忘れないこと。
 * @author TAKESHI YAMASHITA
 *
 */
public class GoogleAPIConnector extends WebAPIConnectorBase {

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

    @Override
    protected HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection result = super.createConnection(url, method);
        result.setRequestProperty("Authorization", "OAuth " + accessToken); // OAUth2 アクセストークン
        return result;
    }

    /**
     * エラーハンドリングを行う
     */
    @Override
    protected boolean onAuthError(WebAPIException e) throws WebAPIException {
        // 認証エラーだったら、再度トークンを発行する
        LogUtil.log("token refresh");
        refreshAccessToken();
        return true;
    }

    @Override
    protected boolean handleException(WebAPIException e, int tryCount) throws WebAPIException {
        // 何回かのリトライを行った上で、 503ハンドリングはしない
        if (tryCount > 4 && e.getResponceCode() == 503) {
            return false;
        }
        return super.handleException(e, tryCount);
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
