package com.eaglesakura.lib.net.google.drive.fragment;

import com.eaglesakura.lib.android.game.thread.AsyncAction;
import com.eaglesakura.lib.android.game.thread.UIHandler;
import com.eaglesakura.lib.android.game.util.FileUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.net.WebAPIException;
import com.eaglesakura.lib.net.google.GoogleOAuth2Helper;
import com.eaglesakura.lib.net.google.GoogleOAuth2Helper.AuthToken;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;

import java.io.File;

/**
 * GData認証を行うためのFragment
 *
 * @author TAKESHI YAMASHITA
 */
public class GoogleOAuth2Fragment extends Fragment {
    /**
     * 認証用のWebView
     */
    WebView webView = null;

    /**
     * client-id保持
     */
    static final String ARG_CLIENT_ID = "ARG_CLIENT_ID";

    /**
     * client-secret保持
     */
    static final String ARG_CLIENT_SECRET = "ARG_CLIENT_SECRET";

    /**
     * リダイレクト用URI
     */
    static final String ARG_REDIRECT_URI = "ARG_REDIRECT_URI";

    /**
     * 接続スコープ一覧
     */
    static final String ARG_SCOPE_URLs = "ARG_SCOPE_URLs";

    /**
     * デフォルトのリダイレクト用URI
     */
    static final String ARG_DEFAULT_REDIRECT_URI = "http://localhost";

    OAuth2Listener listener;

    public GoogleOAuth2Fragment() {
        setArguments(new Bundle());
    }

    /**
     *
     * @param clientId
     * @param clientSecret
     */
    public GoogleOAuth2Fragment(final String clientId, final String clientSecret, final String[] scopes) {
        setArguments(new Bundle());

        getArguments().putString(ARG_CLIENT_ID, clientId);
        getArguments().putString(ARG_CLIENT_SECRET, clientSecret);
        getArguments().putString(ARG_REDIRECT_URI, ARG_DEFAULT_REDIRECT_URI);
        getArguments().putStringArray(ARG_SCOPE_URLs, scopes);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    @SuppressWarnings("all")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        webView = new WebView(getActivity());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearSslPreferences();
        webView.clearHistory();
        webView.clearMatches();
        webView.clearView();

        (new Thread() {
            @Override
            public void run() {
                {
                    String cachePath = "/data/data/" + getActivity().getPackageName() + "/files/__es_auth_cache__";
                    File cacheDirectory = new File(cachePath);

                    FileUtil.mkdir(cacheDirectory);
                    FileUtil.cleanDirectory(cacheDirectory);
                    webView.getSettings().setAppCachePath(cacheDirectory.getAbsolutePath());
                    CookieSyncManager.createInstance(getActivity());
                    CookieManager.getInstance().removeAllCookie();

                    WebViewDatabase.getInstance(getActivity()).clearFormData();
                    WebViewDatabase.getInstance(getActivity()).clearHttpAuthUsernamePassword();
                    WebViewDatabase.getInstance(getActivity()).clearUsernamePassword();
                }

                UIHandler.postUI(new Runnable() {

                    @Override
                    public void run() {
                        startAuthorization();
                    }
                });
            }
        }).start();

        return webView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listener = (OAuth2Listener) activity;
    }

    /**
     * フラグメントが生きている場合はtrue
     */
    protected boolean isExist() {
        if (getActivity() == null) {
            return false;
        }

        if (!isAdded()) {
            return false;
        }

        if (isDetached()) {
            return false;
        }

        return true;
    }

    /**
     * 認証URL取得中のダイアログ
     */
    protected Dialog createAuthUrlLoadingDialog() {
        ProgressDialog result = new ProgressDialog(getActivity());
        result.setMessage("認証準備中です");
        return result;
    }

    /**
     * アクセストークン取得中のダイアログを作成する
     */
    protected Dialog createAccesTokenLoadingDialog() {
        ProgressDialog result = new ProgressDialog(getActivity());
        result.setMessage("認証情報を問い合わせ中です");
        return result;
    }

    /**
     * 認証コードを取得できた
     */
    protected void onReceiveAuthCode(final String authCode) {
        if (!isExist()) {
            return;
        }
        final Dialog dialog = createAccesTokenLoadingDialog();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                listener.onAuthCanceled(get_this());
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        (new AsyncAction() {

            @Override
            protected void onSuccess(Object object) {
                if (!isExist()) {
                    return;
                }

                AuthToken token = (AuthToken) object;
                listener.onMakeTokenComplete(get_this(), token);
            }

            @Override
            protected void onFailure(Exception exception) {
                listener.onErrorMakeAuthToken(get_this(), (WebAPIException) exception);
            }

            @Override
            protected void onFinalize() {
                super.onFinalize();

                if (isExist()) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            protected Object onBackgroundAction() throws Exception {
                AuthToken token = GoogleOAuth2Helper.getAuthToken(getClientId(), getClientSecret(), getRedirectURL(),
                        authCode);
                return token;
            }
        }).start();
    }

    /**
     * 認証用のURLを取得した
     */
    protected void onReceiveAuthorizationUrl(final String url) {
        if (!isExist()) {
            return;
        }
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            boolean checked = false;

            synchronized boolean checkURL(final String checkUrl) {
                if (checked) {
                    return false;
                }

                if (checkUrl.startsWith(getRedirectURL())) {
                    // 正常にリダイレクトが動いた場合 
                    int index = checkUrl.indexOf('=');
                    final String authCode = checkUrl.substring(index + 1);
                    LogUtil.log("debug::" + authCode);
                    //
                    LogUtil.log("auth code / complete");
                    UIHandler.postUI(new Runnable() {
                        @Override
                        public void run() {
                            // WebViewを不可視にする
                            webView.setVisibility(View.INVISIBLE);
                            onReceiveAuthCode(authCode);
                        }
                    });
                    checked = true;
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String startUrl, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                checkURL(startUrl);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String startUrl) {
                if (checkURL(startUrl)) {
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, startUrl);
            }
        });
    }

    public String getClientId() {
        return getArguments().getString(ARG_CLIENT_ID);
    }

    public String getClientSecret() {
        return getArguments().getString(ARG_CLIENT_SECRET);
    }

    public String getRedirectURL() {
        return getArguments().getString(ARG_REDIRECT_URI);
    }

    public String[] getScopeUrls() {
        return getArguments().getStringArray(ARG_SCOPE_URLs);
    }

    private GoogleOAuth2Fragment get_this() {
        return this;
    }

    /**
     *
     */
    protected void startAuthorization() {
        final Dialog dialog = createAuthUrlLoadingDialog();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                listener.onAuthCanceled(get_this());
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        (new AsyncAction() {
            String authUrl = null;

            @Override
            protected void onSuccess(Object object) {
                if (!isExist()) {
                    return;
                }

                onReceiveAuthorizationUrl(authUrl);
            }

            @Override
            protected void onFailure(Exception exception) {
                LogUtil.log(exception);
                if (!(exception instanceof WebAPIException)) {
                    exception = new WebAPIException(exception);
                }
                listener.onErrorMakeAuthURL(get_this(), (WebAPIException) exception);
            }

            @Override
            protected void onFinalize() {
                super.onFinalize();

                if (isExist()) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            protected Object onBackgroundAction() throws Exception {
                authUrl = GoogleOAuth2Helper.getAuthorizationUrl(getClientId(), getRedirectURL(), getScopeUrls());
                if (authUrl == null) {
                    throw new NullPointerException("Auth URL is null");
                }
                return authUrl;
            }
        }).start();
    }

    /**
     * OAuth2のトークン取得状態を受け取るリスナ
     */
    public interface OAuth2Listener {
        /**
         * トークンの作成に成功した
         */
        public void onMakeTokenComplete(GoogleOAuth2Fragment fragment, GoogleOAuth2Helper.AuthToken token);

        /**
         * トークンのURL作成に失敗した
         */
        public void onErrorMakeAuthURL(GoogleOAuth2Fragment fragment, WebAPIException e);

        /**
         * トークンの取得に失敗した
         */
        public void onErrorMakeAuthToken(GoogleOAuth2Fragment fragment, WebAPIException e);

        /**
         * キャンセルされた
         */
        public void onAuthCanceled(GoogleOAuth2Fragment fragment);
    }
}
