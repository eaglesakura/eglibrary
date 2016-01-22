package com.eaglesakura.android.playservice;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Timer;
import com.eaglesakura.util.Util;

import android.os.Bundle;

/**
 *
 */
public class GoogleApiClientToken {
    private int refs;

    /**
     * clientは複数回のconnectに対応できる。
     * <br>
     * 何度もclientを生成するような実装だと正常にconnectが行えない場合があるため、一つのclientに長生きしてもらう。
     */
    private GoogleApiClient client;

    private Bundle connectedHint;

    private final Object lock = new Object();

    private long connectSleepTime = 1;

    private long disconnectPendingTime = 500;

    /**
     * pending result
     */
    private ConnectionResult connectionResult;

    private final GoogleApiClient.Builder builder;

    private boolean disconnected = false;

    public GoogleApiClientToken(GoogleApiClient.Builder builder) {
        this.builder = builder;
        this.client = builder.build();
        CallbackImpl impl = new CallbackImpl();
        this.client.registerConnectionCallbacks(impl);
        this.client.registerConnectionFailedListener(impl);

        this.client.connect();
    }

    public boolean registerConnectionFailedListener(GoogleApiClient.OnConnectionFailedListener listener) {
        if (client != null) {
            client.unregisterConnectionFailedListener(listener);
            client.registerConnectionFailedListener(listener);
            return true;
        } else {
            return false;
        }
    }

    public boolean registerConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        if (client != null) {
            client.unregisterConnectionCallbacks(callbacks);
            client.registerConnectionCallbacks(callbacks);
            return true;
        } else {
            return false;
        }
    }

    public boolean unregisterConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        if (client != null) {
            client.unregisterConnectionCallbacks(callbacks);
            return true;
        } else {
            return false;
        }
    }

    /**
     * ログインが完了していたらtrueを返却する
     */
    public boolean isLoginCompleted() {
        return client != null && client.isConnected();
    }

    /**
     * 最後に取得したコネクション情報を取得する
     */
    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }

    public Bundle getConnectedHint() {
        return connectedHint;
    }

    public void setConnectSleepTime(long connectSleepTime) {
        this.connectSleepTime = connectSleepTime;
    }

    public void setDisconnectPendingTime(long disconnectPendingTime) {
        this.disconnectPendingTime = disconnectPendingTime;
    }

    /**
     * Google Apiに対する初回ログインを完了させる
     */
    private boolean waitInitialGoogleLoginFinish(int timeoutMs) {
        Timer timer = new Timer();
        timer.start();

        if (disconnected) {
            LogUtil.log("Token disconnected Req ReConnecting");
            connectionResult = null;
            client.connect();
        }

        do {
            if (timer.end() > timeoutMs) {
                return false;
            }

            if (connectionResult != null) {
                return true;
            } else {
                Util.sleep(10);
//                if (!client.isConnected()) {
//                    client.blockingConnect(1000, TimeUnit.MILLISECONDS);
//                }
            }
        } while (!client.isConnected());

        return true;
    }

    /**
     * Google Apiの実行を行う。
     * <br>
     * 裏スレッドから呼び出さなくてはならない。
     */
    public <T> T executeGoogleApi(GoogleApiTask<T> task) {
        if (AndroidThreadUtil.isUIThread()) {
            throw new IllegalStateException();
        }

        UIHandler.getInstance().removeCallbacks(disconnectChecker);
        try {
            synchronized (lock) {
                ++refs;
                if (refs == 1) {
                    waitInitialGoogleLoginFinish(1000 * 15);
                }

                if (task.isCanceled()) {
                    return null;
                }
            }

            try {
                if (connectionResult != null) {
                    // 接続に失敗している
                    return task.connectedFailed(client, connectionResult);
                } else {
                    return task.executeTask(client);
                }
            } catch (Exception e) {
                LogUtil.log(e);
                return null;
            }
        } finally {
            synchronized (lock) {
                --refs;
                if (isAutoDisconnectTarget()) {
                    LogUtil.log("req clean GoogleApiClient");
                    UIHandler.postDelayedUI(disconnectChecker, disconnectPendingTime);
                }
            }
        }
    }


    void reconnect() {
        connectionResult = null;
        client.disconnect();
        client.reconnect();
    }

    void connect() {
        connectionResult = null;
        client.disconnect();
        client.connect();
    }

    private boolean isAutoDisconnectTarget() {
        return refs == 0 && client != null && client.isConnected();
    }

    Runnable disconnectChecker = new Runnable() {
        @Override
        public void run() {
            new Thread() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (isAutoDisconnectTarget()) {
                            LogUtil.log("auto disconnect client");
                            client.disconnect();
                            disconnected = true;
                            connectionResult = null;
                        }
                    }
                }
            }.start();
        }
    };

    private class CallbackImpl implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnected(Bundle bundle) {
            LogUtil.log("onConnected bundle(%s)", "" + bundle);
            connectedHint = bundle;
            connectionResult = null;
        }

        @Override
        public void onConnectionSuspended(int status) {
            LogUtil.log("onConnectionSuspended status(%d)", status);
            disconnected = true;
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            LogUtil.log("onConnectionFailed connectionResult(%s)", "" + connectionResult);
            connectionResult = result;
            disconnected = true;

//            if (false) {
//                BasicSettings settings = FrameworkCentral.getSettings();
//                settings.setLoginGoogleClientApi(false);
//                settings.setLoginGoogleAccount("");
//                settings.commit();
//            }
        }
    }

}
