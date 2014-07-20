package com.eaglesakura.lib.android.web.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout.LayoutParams;

import com.eaglesakura.lib.android.game.util.ContextUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * Twitterログインをサポートするクラス
 * 
 * 利用するには事前にTwitterのデベロッパーサイトでアプリの登録を行い、トークン、トークンシークレットを取得して
 * コールバックURLを設定しておく必要がある。
 * @author Takeshi
 *
 */
public class TwitterLoginActivity extends Activity {

    /**
     * ログインに失敗した。
     */
    public static final int RESULT_FAILED = 0x7f7f7f;

    /**
     * 認証されたトークン
     */
    public static final String INTENT_RESULT_TWITTER_TOKEN = "INTENT_RESULT_TWITTER_TOKEN";

    /**
     * 認証されたトークンシークレット
     */
    public static final String INTENT_RESULT_TWITTER_TOKEN_SECRET = "INTENT_RESULT_TWITTER_TOKEN_SECRET";

    /**
     * アプリのスクリーン名
     */
    public static final String INTENT_RESULT_TWITTER_USER_SCREEN_NAME = "INTENT_RESULT_TWITTER_SCREEN_NAME";

    /**
     * トークン取得用のコンシューマキー
     */
    public static final String INTENT_CONSUMER_KEY = "INTENT_CONSUMER_KEY";

    /**
     * トークン取得用のコンシューマシークレット
     */
    public static final String INTENT_CONSUMER_SECRET = "INTENT_CONSUMER_SECRET";

    /**
     * コールバック用のURL
     */
    public static final String INTENT_CALLBACK_URL = "INTENT_SCHEME";

    /**
     * 認証用のView
     */
    WebView webView = null;

    /**
     * Twitter本体
     */
    Twitter twitter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextUtil.setOrientationFixed(this, true);
        startLoginTask();
    }

    String getConsumerKey() {
        return getIntent().getStringExtra(INTENT_CONSUMER_KEY);
    }

    String getConsumerSecret() {
        return getIntent().getStringExtra(INTENT_CONSUMER_SECRET);
    }

    String getCallbackURL() {
        return getIntent().getStringExtra(INTENT_CALLBACK_URL);
    }

    void onOAuthRequestFailed(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_FAILED);
                finish();
            }
        });
        LogUtil.log(e);
    }

    void onResult(AccessToken token) {
        final String eResultToken = token.getToken();
        final String eResultTokenSecret = token.getTokenSecret();
        final String eResultScreenName = token.getScreenName();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putExtra(INTENT_RESULT_TWITTER_USER_SCREEN_NAME, eResultScreenName);
                intent.putExtra(INTENT_RESULT_TWITTER_TOKEN, eResultToken);
                intent.putExtra(INTENT_RESULT_TWITTER_TOKEN_SECRET, eResultTokenSecret);

                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    void onLoginSuccess(String callbackURL) {
        setContentView(new View(this));
        Uri uri = Uri.parse(callbackURL);
        final String verifier = uri.getQueryParameter("oauth_verifier");
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Please Wait...");
        (new Thread() {
            @Override
            public void run() {
                try {
                    dialog.dismiss();
                    AccessToken accessToken = twitter.getOAuthAccessToken(verifier);
                    onResult(accessToken);
                } catch (Exception e) {
                    onOAuthRequestFailed(e);
                }
            }
        }).start();
        dialog.show();
    }

    /**
     * ログインを開始する。
     */
    void startLoginTask() {

        try {
            webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAppCacheEnabled(false);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.clearCache(true);
            webView.clearFormData();
            webView.clearSslPreferences();
            webView.clearHistory();
            webView.clearMatches();
            webView.clearView();
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    LogUtil.log(url);
                    if (url.startsWith(getCallbackURL())) {
                        onLoginSuccess(url);
                        return;
                    }

                    super.onPageStarted(view, url, favicon);
                }
            });
            setContentView(webView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("Please Wait...");
            (new Thread() {
                @Override
                public void run() {

                    ConfigurationBuilder builder = new ConfigurationBuilder();
                    builder.setOAuthConsumerKey(getConsumerKey()).setOAuthConsumerSecret(getConsumerSecret());
                    twitter = new TwitterFactory(builder.build()).getInstance();

                    try {
                        CookieSyncManager.getInstance();
                        CookieManager.getInstance().removeAllCookie();
                    } catch (Exception e) {

                    }

                    try {
                        final RequestToken oAuthRequestToken = twitter.getOAuthRequestToken(getCallbackURL());
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                dialog.dismiss();
                                webView.loadUrl(oAuthRequestToken.getAuthenticationURL());
                                webView.clearCache(true);
                            }
                        });
                    } catch (TwitterException e) {
                        onOAuthRequestFailed(e);
                    } catch (Exception e) {
                        onOAuthRequestFailed(e);
                    }
                }
            }).start();

            dialog.show();
        } catch (Exception e) {
            setResult(RESULT_FAILED);
            finish();
        }
    }
}
