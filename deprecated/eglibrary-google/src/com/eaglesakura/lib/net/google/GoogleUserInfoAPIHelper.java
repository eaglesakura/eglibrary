package com.eaglesakura.lib.net.google;

import com.eaglesakura.lib.android.game.util.JsonModel;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.net.WebAPIConnection;
import com.eaglesakura.lib.net.WebAPIConnectorBase;
import com.eaglesakura.lib.net.WebAPIException;
import com.eaglesakura.lib.net.WebAPIException.Type;

import net.arnx.jsonic.JSON;

import java.io.IOException;

/**
 * ユーザー情報を得るためのヘルパクラス
 *
 * @author TAKESHI YAMASHITA
 */
public class GoogleUserInfoAPIHelper {

    /**
     * ユーザーのメールアドレスを問い合わせる。
     *
     * @see {@link GoogleOAuth2Helper#SCOPE_USERINFO_EMAIL}
     */
    public static String getUserEmail(WebAPIConnectorBase connector) throws WebAPIException {

        WebAPIConnection connect = null;
        try {
            LogUtil.log("mail connect start");
            connect = connector.get("https://www.googleapis.com/oauth2/v1/userinfo", null);
            LogUtil.log("mail connected");
            if (connect.getResponceCode() == 200) {
                try {
                    LogUtil.log("mail decode start");
                    UserInfoResponce resp = JSON.decode(connect.getInput(), UserInfoResponce.class);
                    LogUtil.log("mail decoded :: " + resp.email);

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
     *
     * @author TAKESHI YAMASHITA
     */
    public static class UserInfoResponce extends JsonModel {

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
