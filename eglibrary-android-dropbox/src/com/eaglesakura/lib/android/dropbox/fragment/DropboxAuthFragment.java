package com.eaglesakura.lib.android.dropbox.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.eaglesakura.lib.android.dropbox.DropboxAPIException;
import com.eaglesakura.lib.android.dropbox.DropboxAPIException.Type;
import com.eaglesakura.lib.android.game.thread.UIHandler;
import com.eaglesakura.lib.android.game.util.LogUtil;

public class DropboxAuthFragment extends Fragment {

    private static final String ARG_APP_KEY = "ARG_APP_KEY";

    private static final String ARG_APP_SECRET = "ARG_APP_SECRET";

    private static final String ARG_ACCESS_TYPE = "ARG_ACCESS_TYPE";

    DropboxAPI<AndroidAuthSession> api = null;

    DropboxAuthListener listener = null;

    public DropboxAuthFragment() {
        setArguments(new Bundle());
    }

    public DropboxAuthFragment(String appKey, String appSecret, AccessType accType) {
        setArguments(new Bundle());

        getArguments().putString(ARG_APP_KEY, appKey);
        getArguments().putString(ARG_APP_SECRET, appSecret);
        getArguments().putString(ARG_ACCESS_TYPE, accType.name());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listener = (DropboxAuthListener) activity;

        // キーを作成する
        AppKeyPair appKeys = new AppKeyPair(getAppKey(), getAppSecret());
        AndroidAuthSession session = new AndroidAuthSession(appKeys, getAccessType());
        api = new DropboxAPI<AndroidAuthSession>(session);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (api.getSession().authenticationSuccessful()) {
            // １サイクル後に通知が来るように修正する
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    try {
                        api.getSession().finishAuthentication();
                        AccessTokenPair tokens = api.getSession().getAccessTokenPair();

                        if (tokens.key == null || tokens.secret == null) {
                            listener.onAuthFailed(DropboxAuthFragment.this, Type.TokensIsNull);
                        } else {
                            listener.onAuthComplete(DropboxAuthFragment.this, tokens.key, tokens.secret);
                        }

                    } catch (Exception e) {
                        LogUtil.log(e);
                        listener.onAuthFailed(DropboxAuthFragment.this, DropboxAPIException.toType(e));
                    }
                }
            });
        } else {
            api.getSession().startAuthentication(getActivity());
        }
    }

    private String getAppKey() {
        return getArguments().getString(ARG_APP_KEY);
    }

    private String getAppSecret() {
        return getArguments().getString(ARG_APP_SECRET);
    }

    private AccessType getAccessType() {
        return AccessType.valueOf(getArguments().getString(ARG_ACCESS_TYPE));
    }

    /**
     * Dropboxの認証を行う
     * Activityが継承している必要がある。
     * @author TAKESHI YAMASHITA
     *
     */
    public interface DropboxAuthListener {
        /**
         * 認証が成功した
         * @param token
         * @param tokenSecret
         */
        public void onAuthComplete(DropboxAuthFragment fragment, String token, String tokenSecret);

        /**
         * エラーが発生した
         * @param error
         */
        public void onAuthFailed(DropboxAuthFragment fragment, DropboxAPIException.Type error);
    }
}
