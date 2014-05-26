package com.eaglesakura.lib.android.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.eaglesakura.lib.android.dropbox.DropboxAPIException.Type;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * API呼び出し補助クラス
 * @author TAKESHI YAMASHITA
 *
 */
public class DropboxAPIHelper {
    String token;
    String tokenSecret;
    AccessType accType;

    String appKey;
    String appSecret;

    DropboxAPI<AndroidAuthSession> api = null;

    /**
     * 
     * @param token
     * @param tokenSecret
     */
    public DropboxAPIHelper(String appKey, String appSecret, String token, String tokenSecret, AccessType accType) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.token = token;
        this.tokenSecret = tokenSecret;
        this.accType = accType;

    }

    /**
     * アカウントに対してログインする。
     * @throws DropboxAPIException
     */
    public void login() throws DropboxAPIException {
        try {
            // キーを作成する
            AccessTokenPair pair = new AccessTokenPair(token, tokenSecret);
            AppKeyPair appKeys = new AppKeyPair(appKey, appSecret);
            AndroidAuthSession session = new AndroidAuthSession(appKeys, accType, pair);
            api = new DropboxAPI<AndroidAuthSession>(session);
        } catch (Exception e) {
            LogUtil.log(e);
            throw new DropboxAPIException("login failed...", Type.LoginFailed);
        }
    }

    /**
     * ユーザーのディスプレイ表示名を取得する
     * @return
     * @throws DropboxAPIException
     */
    public String getUserDisplayName() throws DropboxAPIException {
        try {
            return api.accountInfo().displayName;
        } catch (Exception e) {
            LogUtil.log(e);
            throw new DropboxAPIException(e);
        }
    }

    /**
     * 詳細データを取得する
     * @param path
     * @return
     * @throws DropboxAPIException
     */
    public Entry metadata(String path) throws DropboxAPIException {
        try {
            return api.metadata(path, -1, null, true, null);
        } catch (Exception e) {
            LogUtil.log(e);
            throw new DropboxAPIException(e);
        }
    }

    /**
     * APIクラス本体を取得する
     * @return
     */
    public DropboxAPI<AndroidAuthSession> getAPI() {
        return api;
    }
}
