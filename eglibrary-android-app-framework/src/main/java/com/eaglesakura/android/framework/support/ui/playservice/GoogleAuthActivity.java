package com.eaglesakura.android.framework.support.ui.playservice;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.KeyEvent;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.R;
import com.eaglesakura.android.framework.db.BasicSettings;
import com.eaglesakura.android.framework.support.ui.BaseActivity;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.time.Timer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;

/**
 * Googleの認証を専門に行うActivity
 */
@EActivity
public abstract class GoogleAuthActivity extends BaseActivity {

    static final int REQUEST_GOOGLE_CLIENT_AUTH = 0x1200;

    public static final String EXTRA_AUTH_ERROR_CODE = "EXTRA_AUTH_ERROR_CODE";

    @InstanceState
    protected boolean apiClientAuthInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_google_auth);
        super.onCreate(savedInstanceState);

        loginOnBackground();
    }

    /**
     * Google API Clientを生成する
     *
     * @return
     */
    protected abstract GoogleApiClient.Builder newGoogleApiClient();

    /**
     * バックグラウンドでログインを行う
     */
    @Background
    public void loginOnBackground() {
        if (apiClientAuthInProgress) {
            // ログイン処理中なら何もしない
            return;
        }

        // ブロッキングログインを行う

        final BasicSettings basicSettings = FrameworkCentral.getSettings();
        GoogleApiClient apiClient = newGoogleApiClient().build();

        Timer timer = new Timer();
        ConnectionResult connect = apiClient.blockingConnect();
        log("loginTime (%d ms)", timer.end());

        if (connect.isSuccess()) {
            log("login completed");
            basicSettings.setLoginGoogleClientApi(true);
            // Emailを保存する
            try {
                basicSettings.setLoginGoogleAccount(Plus.AccountApi.getAccountName(apiClient));
                log("email connected success");
            } catch (Exception e) {
                log("email connected fail");
            }
            basicSettings.commit();
            onSuccess();
            return;
        }

        if (connect.hasResolution()) {
            apiClientAuthInProgress = true;
            log("start auth dialog");
            showLoginDialog(connect);
        } else {
            onFailed(connect.getErrorCode());
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
            connectionResult.startResolutionForResult(this, REQUEST_GOOGLE_CLIENT_AUTH);
        } catch (IntentSender.SendIntentException e) {
            log("Exception while starting resolution activity", e);
            apiClientAuthInProgress = false;

        }
    }

    @UiThread
    protected void onSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @UiThread
    protected void onFailed(final int errorCode) {
        MaterialAlertDialog dialog = new MaterialAlertDialog(this);
        dialog.setTitle(R.string.eglibrary_GoogleApi_Error_Title);
        dialog.setMessage(R.string.eglibrary_GoogleApi_Error_Message);
        dialog.setPositiveButton(R.string.eglibrary_GoogleApi_Error_Retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loginOnBackground();
            }
        });
        dialog.setNegativeButton(R.string.eglibrary_GoogleApi_Error_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_AUTH_ERROR_CODE, errorCode);
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * ログイン戻りの対応
     *
     * @param resultCode
     * @param data
     */
    @OnActivityResult(REQUEST_GOOGLE_CLIENT_AUTH)
    protected void resultGoogleClientAuth(int resultCode, Intent data) {
        apiClientAuthInProgress = false;
        if (resultCode == Activity.RESULT_OK) {
            // 再度ログイン処理
            loginOnBackground();
        } else {
            // キャンセルされた場合はログイン状態も解除しなければならない
            BasicSettings settings = FrameworkCentral.getSettings();
            settings.setLoginGoogleClientApi(false);
            settings.setLoginGoogleAccount("");
            settings.commitAsync();
            onFailed(ConnectionResult.CANCELED);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (ContextUtil.isBackKeyEvent(event)) {
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
