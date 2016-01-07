package com.eaglesakura.android.framework.support.ui.playservice;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.KeyEvent;

import com.eaglesakura.android.R;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.db.BasicSettings;
import com.eaglesakura.android.framework.support.ui.BaseActivity;
import com.eaglesakura.android.framework.support.ui.butterknife.OnActivityResult;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.material.widget.MaterialAlertDialog;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;


/**
 * Googleの認証を専門に行うActivity
 */
public abstract class GoogleAuthActivity extends BaseActivity {

    static final int REQUEST_GOOGLE_CLIENT_AUTH = 0x1200;

    public static final String EXTRA_AUTH_ERROR_CODE = "EXTRA_AUTH_ERROR_CODE";

    final long DEFAULT_SLEEP_TIME = 3000;
    long sleepTime = DEFAULT_SLEEP_TIME;

    final float BACKOFF_MULT = 1.25f;

    final int MAX_RETRY = 3;

    int retryRequest = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_auth);

        setGoogleApiClientToken(new GoogleApiClientToken(newGoogleApiClient()));
        getGoogleApiClientToken().setConnectSleepTime(1000);
        getGoogleApiClientToken().setDisconnectPendingTime(1);
        initialLogout();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Google API Clientを生成する
     *
     * @return
     */
    protected abstract GoogleApiClient.Builder newGoogleApiClient();

    protected void initialLogout() {
        runBackground(new Runnable() {
            @Override
            public void run() {

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
                        Util.sleep(sleepTime);
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

                getGoogleApiClientToken().reconnect();
                loginOnBackground();
            }
        });
    }

    /**
     * バックグラウンドでログインを行う
     */
    protected void loginOnBackground() {
        runBackground(new Runnable() {
            @Override
            public void run() {

                Util.sleep(sleepTime);

                // ブロッキングログインを行う
                getGoogleApiClientToken().executeGoogleApi(new GoogleApiTask<Object>() {
                    @Override
                    public Object executeTask(GoogleApiClient client) throws Exception {
                        final BasicSettings basicSettings = FrameworkCentral.getSettings();
                        LogUtil.log("login completed");
                        basicSettings.setLoginGoogleClientApi(true);
                        // Emailを保存する
                        try {
                            basicSettings.setLoginGoogleAccount(Plus.AccountApi.getAccountName(client));
                            LogUtil.log("email connected success");
                        } catch (Exception e) {

                            LogUtil.log("email connected fail");
                        }
                        basicSettings.commit();
                        onSuccess();
                        return null;
                    }

                    @Override
                    public Object connectedFailed(GoogleApiClient client, ConnectionResult connectionResult) {
                        if (retryRequest >= 0) {
                            // MEMO ログイン直後は正常にログインできない端末があるので、リトライ機構とウェイトを設ける
                            --retryRequest;
                            if (retryRequest == 0) {
                                onFailed(connectionResult.getErrorCode());
                            } else {

                                LogUtil.log("connect retry");
//                        getGoogleApiClientToken().reconnect();
                                sleepTime *= BACKOFF_MULT;
                                loginOnBackground();
                            }
                        } else if (connectionResult.hasResolution()) {
                            LogUtil.log("start auth dialog");
                            showLoginDialog(connectionResult);
                        } else {
                            onFailed(connectionResult.getErrorCode());
                        }
                        return null;
                    }

                    @Override
                    public boolean isCanceled() {
                        return isFinishing();
                    }
                });
            }
        });
    }

    /**
     * Google Play Serviceにログインを行わせる
     *
     * @param connectionResult
     */
    protected void showLoginDialog(final ConnectionResult connectionResult) {
        runUI(new Runnable() {
            @Override
            public void run() {

                try {
                    LogUtil.log("Attempting to resolve failed connection");
                    connectionResult.startResolutionForResult(self(), REQUEST_GOOGLE_CLIENT_AUTH);
                } catch (IntentSender.SendIntentException e) {
                    LogUtil.log("Exception while starting resolution activity", e);
                }
            }
        });
    }

    protected void onSuccess() {
        runUI(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    protected void onFailed(final int errorCode) {
        runUI(new Runnable() {
            @Override
            public void run() {
                MaterialAlertDialog dialog = new MaterialAlertDialog(self());
                dialog.setTitle(R.string.eglibrary_GoogleApi_Error_Title);
                dialog.setMessage(R.string.eglibrary_GoogleApi_Error_Message);
                dialog.setPositiveButton(R.string.eglibrary_GoogleApi_Error_Retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ユーザーが一度操作しているから回数リセット
                        initialLogout();
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
        });

    }

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
            retryRequest = MAX_RETRY;
            sleepTime = DEFAULT_SLEEP_TIME;
            getGoogleApiClientToken().connect();
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
