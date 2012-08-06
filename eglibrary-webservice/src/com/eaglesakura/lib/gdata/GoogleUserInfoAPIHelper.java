package com.eaglesakura.lib.gdata;

import java.io.IOException;

import net.arnx.jsonic.JSON;

import com.eaglesakura.lib.net.WebAPIConnection;
import com.eaglesakura.lib.net.WebAPIConnectorBase;
import com.eaglesakura.lib.net.WebAPIException;
import com.eaglesakura.lib.net.WebAPIException.Type;

/**
 * ユーザー情報を得るためのヘルパクラス
 * @author TAKESHI YAMASHITA
 *
 */
public class GoogleUserInfoAPIHelper {

    /**
     * ユーザーのメールアドレスを問い合わせる。
     * @param connector
     * @return
     * @throws GoogleAPIException
     * @see {@link GoogleOAuth2Helper#SCOPE_USERINFO_EMAIL}
     */
    public static String getUserEmail(WebAPIConnectorBase connector) throws WebAPIException {

        WebAPIConnection connect = null;
        try {
            connect = connector.get("https://www.googleapis.com/oauth2/v1/userinfo", null);

            if (connect.getResponceCode() == 200) {
                try {
                    UserInfoResponce resp = JSON.decode(connect.getInput(), UserInfoResponce.class);

                    if (resp == null || resp.email == null) {
                        throw new WebAPIException("email not found", Type.APIResponseError);
                    }

                    return resp.email;
                } catch (IOException e) {
                    throw new WebAPIException(e);
                }
            }

            throw new WebAPIException(connect.getResponceCode());
        } finally {
            if (connect != null) {
                connect.dispose();
            }
        }
    }

    /**
     * ユーザー問い合わせの結果を格納する
     * @author TAKESHI YAMASHITA
     *
     */
    public static class UserInfoResponce {

        /**
         * メールアドレス
         */
        public String email = null;

        /**
         * 
         */
        public Boolean verified_email = null;
    }
}
