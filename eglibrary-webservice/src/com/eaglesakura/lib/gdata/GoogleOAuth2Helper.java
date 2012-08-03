package com.eaglesakura.lib.gdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import net.arnx.jsonic.JSON;

import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * Googleの認証用ヘルパ
 * @author TAKESHI YAMASHITA
 *
 */
public class GoogleOAuth2Helper {
    private static final String ENDPOINT = "https://accounts.google.com/o/oauth2";

    /**
     * GoogleDriveへのアクセス
     */
    public static final String SCOPE_GDRIVE = "https://www.googleapis.com/auth/drive";

    /**
     * email情報へのアクセス
     */
    public static final String SCOPE_USERINFO_EMAIL = "https://www.googleapis.com/auth/userinfo.email";

    /**
     * 認証コードを取得する
     * 各コードは"https://code.google.com/apis/console"から作成
     * @throws GDataException
     */
    public static String getAuthorizationUrl(final String clientId, final String redirectUri, final String[] scopeUrls)
            throws GoogleAPIException {
        try {
            String SCOPES = "";
            {
                for (int i = 0; i < scopeUrls.length; ++i) {
                    SCOPES += scopeUrls[i];
                    if (i < (scopeUrls.length - 1)) {
                        SCOPES += " ";
                    }
                }
            }

            // パラメータを組み立てる
            StringBuilder b = new StringBuilder();
            b.append("response_type=code");
            b.append("&client_id=").append(URLEncoder.encode(clientId, "utf-8"));
            b.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "utf-8"));
            b.append("&scope=").append(URLEncoder.encode(SCOPES, "utf-8"));
            b.append("&status=1&access_type=offline&approval_prompt=force");

            HttpURLConnection.setFollowRedirects(false);
            // GET メソッドでリクエストする
            HttpURLConnection c = (HttpURLConnection) new URL(ENDPOINT + "/auth?" + b.toString()).openConnection();
            c.setConnectTimeout(1000 * 10);
            final String resultURL = c.getHeaderField("Location");
            c.disconnect();

            return resultURL;

        } catch (IOException e) {
            throw new GoogleAPIException(e);
        }
    }

    /**
     * 取得した認証コードからアクセス用のトークンとリフレーッシュトークンを作成する
     * @param authCode
     */
    public static AuthToken getAuthToken(final String clientId, final String clientSecret, final String redirectUri,
            final String authCode) throws GoogleAPIException {

        try {
            // パラメータを組み立てる
            StringBuilder b = new StringBuilder();
            b.append("code=").append(URLEncoder.encode(authCode, "utf-8"));
            b.append("&client_id=").append(URLEncoder.encode(clientId, "utf-8"));
            b.append("&client_secret=").append(URLEncoder.encode(clientSecret, "utf-8"));
            b.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "utf-8"));
            //            b.append("&grant_type=authorization_code");
            b.append("&grant_type=").append(URLEncoder.encode("authorization_code", "utf-8"));
            byte[] payload = b.toString().getBytes();

            // POST メソッドでリクエストする
            URL url = new URL(ENDPOINT + "/token");
            LogUtil.log("url = " + url.toString());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setDoInput(true);
            c.setConnectTimeout(1000 * 30);
            c.setRequestProperty("Content-Length", String.valueOf(payload.length));
            c.connect();

            {
                OutputStream os = c.getOutputStream();
                os.write(payload);
                os.flush();
            }

            int response = c.getResponseCode();
            LogUtil.log("Response Code = " + response);

            // 戻り値を確認する
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream is = c.getInputStream();
                String json = new String(GameUtil.toByteArray(is));
                //                LogUtil.log("json = " + json);

                AuthToken token = JSON.decode(json, AuthToken.class);

                c.disconnect();
                return token;
            } else {
                InputStream is = c.getErrorStream();
                String json = new String(GameUtil.toByteArray(is));
                ErrorCode error = JSON.decode(json, ErrorCode.class);
                LogUtil.log("error = " + json);
                c.disconnect();
                throw new GoogleAPIException(error, GoogleAPIException.Type.AuthError);
            }
        } catch (IOException e) {
            LogUtil.log(e);
            throw new GoogleAPIException(e);
        }
    }

    /**
     * 取得した認証コードからアクセス用のトークンとリフレーッシュトークンを作成する
     * @param authCode
     */
    public static AuthToken refreshAuthToken(final String clientId, final String clientSecret,
            final String refreshTocken) throws GoogleAPIException {

        try {
            // パラメータを組み立てる
            StringBuilder b = new StringBuilder();
            b.append("&client_id=").append(URLEncoder.encode(clientId, "utf-8"));
            b.append("&client_secret=").append(URLEncoder.encode(clientSecret, "utf-8"));
            b.append("&grant_type=").append(URLEncoder.encode("refresh_token", "utf-8"));
            b.append("&refresh_token=").append(URLEncoder.encode(refreshTocken, "utf-8"));
            byte[] payload = b.toString().getBytes();

            // POST メソッドでリクエストする
            URL url = new URL(ENDPOINT + "/token");
            LogUtil.log("url = " + url.toString());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setDoInput(true);
            c.setConnectTimeout(1000 * 30);
            c.setRequestProperty("Content-Length", String.valueOf(payload.length));
            c.connect();

            {
                OutputStream os = c.getOutputStream();
                os.write(payload);
                os.flush();
            }

            int response = c.getResponseCode();
            LogUtil.log("Response Code = " + response);

            // 戻り値を確認する
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream is = c.getInputStream();
                String json = new String(GameUtil.toByteArray(is));
                //                LogUtil.log("json = " + json);

                AuthToken token = JSON.decode(json, AuthToken.class);

                c.disconnect();
                if (token == null || token.access_token == null) {
                    throw new GoogleAPIException("token == null", GoogleAPIException.Type.APIResponseError);
                }
                return token;
            } else {
                InputStream is = c.getErrorStream();
                String json = new String(GameUtil.toByteArray(is));
                ErrorCode error = JSON.decode(json, ErrorCode.class);
                LogUtil.log("error = " + json);
                c.disconnect();
                throw new GoogleAPIException(error, GoogleAPIException.Type.AuthError);
            }
        } catch (IOException e) {
            LogUtil.log(e);
            throw new GoogleAPIException(e);
        }
    }

    public static final String ERROR_INVALID_REQUEST = "invalid_request";

    public static final String ERROR_INVALID_GRANT = "invalid_grant";

    /**
     * エラーコード解析用
     * @author TAKESHI YAMASHITA
     *
     */
    public static class ErrorCode {
        public String error = null;
    }

    public static class AuthToken {
        /**
         * アクセス用のトークン
         */
        public String access_token = null;

        /**
         * リフレッシュ用のトークン
         */
        public String refresh_token = null;

        /**
         * 
         */
        public String token_type = null;

        /**
         * 
         */
        public Integer expires_in = null;
    }
}
