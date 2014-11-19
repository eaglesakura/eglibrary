package com.eaglesakura.android.framework.support.ui.playservice;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.db.BasicSettings;
import com.eaglesakura.android.framework.support.ui.BaseActivity;
import com.eaglesakura.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;

/**
 * API Base
 * <p/>
 * ログインそのものはGoogleApiAuthFragmentに任せる
 */
@EActivity
public abstract class GoogleApiBaseActivity extends BaseActivity {
    static final int REQUEST_GOOGLE_CLIENT_AUTH = 0x1200;

    private GooleApiClientToken apiClientToken;

    private ConnectionResult connectionResult;

    private boolean apiClientAuthInProgress = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("apiClientAuthInProgress", apiClientAuthInProgress);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            apiClientAuthInProgress = savedInstanceState.getBoolean("apiClientAuthInProgress");
        }
    }


    @Override
    protected void onStart() {
        super.onStart();


        if (apiClientToken == null) {
            GoogleApiClient.Builder builder = newGoogleApiClient()
//                    .addOnConnectionFailedListener(this)
                    ;

            apiClientToken = new GooleApiClientToken(builder);
        }

        apiClientToken.lock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 既にログインが完了しているフラグが立っていたらバックグラウンドでログインを行う
        if (FrameworkCentral.getSettings().getLoginGoogleClientApi()) {
            loginOnBackground();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        apiClientToken.unlock();
        apiClientToken = null;
    }

    /**
     * APIアクセス用トークンを取得する
     *
     * @return
     */
    public GooleApiClientToken getApiClientToken() {
        return apiClientToken;
    }

    /**
     * バックグラウンドでログインを行う
     */
    @Background
    public void loginOnBackground() {
        final BasicSettings basicSettings = FrameworkCentral.getSettings();

        if (apiClientToken.isLoginCompleted()) {
            log("login completed");
            return;
        }

        try {
            pushProgress(R.string.eglibrary_GoogleApi_Connecting);

            // 初期ログインを待つ
            apiClientToken.waitInitialGoogleLoginFinish(1000 * 60);

            if (apiClientToken.isLoginCompleted()) {
                log("login completed");
                return;
            }

            // ブロッキングでログインを行う
            GoogleApiClient client = apiClientToken.lock();
            ConnectionResult result = client.blockingConnect();
            if (result.isSuccess()) {
                log("auth is success!");
                basicSettings.setLoginGoogleClientApi(true);
                basicSettings.commitAsync();
                return;
            }

            if (result.hasResolution()) {
                log("start auth dialog");

                apiClientAuthInProgress = true;
                showLoginDialog(result);

                // ログイン処理が完了するのを待つ
                while (apiClientAuthInProgress) {
                    Util.sleep(100);
                }
            } else {
                showErrorDialog(result);
            }
            apiClientToken.unlock();
        } finally {
            popProgress();
        }
    }

    /**
     * Google Play Serviceにログインを行わせる
     *
     * @param connectionResult
     */
    @UiThread
    protected void showLoginDialog(ConnectionResult connectionResult) {
        try {
            log("Attempting to resolve failed connection");
            connectionResult.startResolutionForResult(this, GoogleApiBaseActivity.REQUEST_GOOGLE_CLIENT_AUTH);
        } catch (IntentSender.SendIntentException e) {
            log("Exception while starting resolution activity", e);
            apiClientAuthInProgress = false;
        }
    }

    /**
     * エラーダイアログを表示する
     *
     * @param connectionResult
     */
    @UiThread
    protected void showErrorDialog(ConnectionResult connectionResult) {
        // Show the localized error dialog
        GooglePlayServicesUtil
                .getErrorDialog(connectionResult.getErrorCode(), this, 0)
                .show();
    }

    /**
     * Google API Clientを生成する
     *
     * @return
     */
    protected abstract GoogleApiClient.Builder newGoogleApiClient();


    /**
     * ログイン戻りの対応
     *
     * @param resultCode
     * @param data
     */
    @OnActivityResult(REQUEST_GOOGLE_CLIENT_AUTH)
    protected void resultGoogleClientAuth(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // 再度ログイン処理
            loginOnBackground();
        } else {
            // キャンセルされた場合はログイン状態も解除しなければならない
            FrameworkCentral.getSettings().setLoginGoogleClientApi(false);
            FrameworkCentral.getSettings().commitAsync();
        }
        apiClientAuthInProgress = false;
    }
}
