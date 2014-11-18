package com.eaglesakura.android.framework.support.ui.playservice;

import android.os.Bundle;

import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.time.Timer;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 *
 */
public class GooleApiClientToken {
    private int refs;

    private GoogleApiClient client;

    /**
     * ログインが失敗で確定したらtrue
     */
    private boolean initialLoginCompleted;

    private final Object lock = new Object();

    /**
     * pending result
     */
    private ConnectionResult connectionResult;

    public GooleApiClientToken(GoogleApiClient.Builder builder) {
        CallbackImpl impl = new CallbackImpl();
        builder.addOnConnectionFailedListener(impl);
        builder.addConnectionCallbacks(impl);

        this.client = builder.build();
    }

    /**
     * 参照カウンタをアップし、クライアントを取得する
     *
     * @return
     */
    public GoogleApiClient lock() {
        synchronized (lock) {
            if (client == null) {
                throw new IllegalStateException("token invalid");
            }
            ++refs;
            return client;
        }
    }

    /**
     * 参照カウンタをデクリメントし、必要であればクライアントを削除する
     */
    public void unlock() {
        synchronized (lock) {
            --refs;

            if (refs == 0) {
                UIHandler.getInstance().removeCallbacks(destroyRunner);

                // 適当なインターバルをおいてチェックする
                // 入れ違いにロックがかかることを防ぐため
                UIHandler.postDelayedUI(destroyRunner, 1000 * 5);
            }
        }
    }

    public void registerConnectionFailedListener(GoogleApiClient.OnConnectionFailedListener listener) {
        client.unregisterConnectionFailedListener(listener);
        client.registerConnectionFailedListener(listener);
    }

    public void registerConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        client.unregisterConnectionCallbacks(callbacks);
        client.registerConnectionCallbacks(callbacks);
    }

    /**
     * ログインが完了していたらtrueを返却する
     *
     * @return
     */
    public boolean isLoginCompleted() {
        return client != null && client.isConnected();
    }

    /**
     * ログインが失敗したらtrueを返却する
     *
     * @return
     */
    public boolean isInitialLoginCompleted() {
        return client != null && initialLoginCompleted;
    }

    /**
     * 最後に取得したコネクション情報を取得する
     *
     * @return
     */
    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }

    /**
     * Google Apiに対する初回ログイン試行が完了している
     *
     * @return
     */
    public boolean isInitialGoogleLoginFinished() {
        return initialLoginCompleted;
    }

    /**
     * Google Apiに対する初回ログインを完了させる
     */
    public boolean waitInitialGoogleLoginFinish(int timeoutMs) {
        Timer timer = new Timer();
        timer.start();

        while (!initialLoginCompleted && client.isConnecting()) {
            if (timer.end() > timeoutMs) {
                return false;
            }

            Util.sleep(100);
        }

        return true;
    }


    private class CallbackImpl implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnected(Bundle bundle) {
            LogUtil.log("onConnected bundle(%s)", "" + bundle);

            initialLoginCompleted = true;
            connectionResult = null;
        }

        @Override
        public void onConnectionSuspended(int status) {
            LogUtil.log("onConnectionSuspended status(%d)", status);

            initialLoginCompleted = true;
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            LogUtil.log("onConnectionFailed connectionResult(%s)", "" + connectionResult);

            initialLoginCompleted = true;
            connectionResult = result;
        }
    }

    private final Runnable destroyRunner = new Runnable() {
        @Override
        public void run() {
            synchronized (lock) {
                if (refs == 0) {
                    if (client.isConnected()) {
                        client.disconnect();
                        client = null;
                    }
                }
            }
        }
    };
}
