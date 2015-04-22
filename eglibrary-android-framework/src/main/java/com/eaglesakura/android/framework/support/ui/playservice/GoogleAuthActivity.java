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
import com.eaglesakura.util.Util;
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

    @InstanceState
    protected int loginTryCount = 0;

    int sleepTimeMs = 1000 * 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_auth);

        setGoogleApiClientToken(new GoogleApiClientToken(newGoogleApiClient()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialLogout();
    }

    /**
     * Google API Clientを生成する
     *
     * @return
     */
    protected abstract GoogleApiClient.Builder newGoogleApiClient();

    @Background
    protected void initialLogout() {
        getGoogleApiClientToken().executeGoogleApi(new GoogleApiTask<Object>() {
            @Override
            public Object executeTask(GoogleApiClient client) throws Exception {
                try {
                    Plus.AccountApi.revokeAccessAndDisconnect(client).await();
                } catch (Exception e) {

                }

                try {
                    client.clearDefaultAccountAndReconnect().await();
                } catch (Exception e) {
                }
                return null;
            }

            @Override
            public Object connectedFailed(GoogleApiClient client, ConnectionResult connectionResult) {
                return null;
            }

            @Override
            public boolean isCanceled() {
                return false;
            }
        });
        loginOnBackground();
    }

    /**
     * バックグラウンドでログインを行う
     */
    @Background
    protected void loginOnBackground() {
        if (apiClientAuthInProgress) {
            // ログイン処理中なら何もしない
            return;
        }

        // 適当にスリープをかける
        Util.sleep(sleepTimeMs);
        if (isFinishing()) {
            return;
        }

        // ブロッキングログインを行う
        getGoogleApiClientToken().executeGoogleApi(new GoogleApiTask<Object>() {
            @Override
            public Object executeTask(GoogleApiClient client) throws Exception {
                final BasicSettings basicSettings = FrameworkCentral.getSettings();
                log("login completed");
                basicSettings.setLoginGoogleClientApi(true);
                // Emailを保存する
                try {
                    basicSettings.setLoginGoogleAccount(Plus.AccountApi.getAccountName(client));
                    log("email connected success");
                } catch (Exception e) {
                    log("email connected fail");
                }
                basicSettings.commit();
                onSuccess();
                return null;
            }

            @Override
            public Object connectedFailed(GoogleApiClient client, ConnectionResult connectionResult) {
                if (connectionResult.hasResolution() && loginTryCount == 0) {
                    // 試行は一度だけ許される
                    ++loginTryCount;
                    apiClientAuthInProgress = true;
                    log("start auth dialog");
                    showLoginDialog(connectionResult);
                } else {
                    onFailed(connectionResult.getErrorCode());
                }
                return null;
            }

            @Override
            public boolean isCanceled() {
                return false;
            }
        });
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
                // ユーザーが一度操作しているから回数リセット
                loginTryCount = 0;
                if (sleepTimeMs < 1000 * 5) {
                    // back off
                    sleepTimeMs += 1000;
                }
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
//            loginOnBackground();
            setGoogleApiClientToken(new GoogleApiClientToken(newGoogleApiClient()));
            getGoogleApiClientToken().startInitialConnect();
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
