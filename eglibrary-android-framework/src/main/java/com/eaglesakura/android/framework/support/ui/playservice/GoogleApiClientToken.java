package com.eaglesakura.android.framework.support.ui.playservice;

import android.os.Bundle;

import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class GoogleApiClientToken {
    private int refs;

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

    public GoogleApiClientToken(GoogleApiClient.Builder builder) {
        this.builder = builder;
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
     *
     * @return
     */
    public boolean isLoginCompleted() {
        return client != null && client.isConnected();
    }

    /**
     * 最後に取得したコネクション情報を取得する
     *
     * @return
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

    private boolean tryApiConnectBlocking() {
        Util.sleep(connectSleepTime);
        // clientを作成
        client = builder.build();
        Util.sleep(connectSleepTime);

        this.connectionResult = client.blockingConnect(1000 * 30, TimeUnit.MILLISECONDS);
        if (this.connectionResult.isSuccess()) {
            this.connectionResult = null;
            return true;
        } else if (this.connectionResult.hasResolution()) {
            return true;
        } else {
            client = null;
            return false;
        }
    }

    private void tryApiConnectAsync() {
        Util.sleep(connectSleepTime);
        // clientを作成
        client = builder.build();
        Util.sleep(connectSleepTime);

        this.connectionResult = null;

        final Object waitLock = new Object();
        GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                synchronized (waitLock) {
                    try {
                        waitLock.notifyAll();
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                synchronized (waitLock) {
                    try {
                        waitLock.notifyAll();
                    } catch (Exception e) {

                    }
                }
            }
        };

        GoogleApiClient.OnConnectionFailedListener failedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult newConnectionResult) {
                connectionResult = newConnectionResult;
                synchronized (waitLock) {
                    try {
                        waitLock.notifyAll();
                    } catch (Exception e) {

                    }
                }
            }
        };

        synchronized (waitLock) {
            client.registerConnectionCallbacks(connectionCallbacks);
            client.registerConnectionFailedListener(failedListener);
//            client.reconnect();
            client.connect();
            try {
                waitLock.wait(1000 * 30);
            } catch (Exception e) {
            }

            client.unregisterConnectionCallbacks(connectionCallbacks);
            client.unregisterConnectionFailedListener(failedListener);
        }
    }

    /**
     * Google Apiの実行を行う。
     * <br>
     * 裏スレッドから呼び出さなくてはならない。
     *
     * @param task
     */
    public <T> T executeGoogleApi(GoogleApiTask<T> task) {
        if (AndroidUtil.isUIThread()) {
            throw new IllegalStateException();
        }

        UIHandler.getInstance().removeCallbacks(disconnectChecker);
        try {
            synchronized (lock) {
                ++refs;
                if (refs == 1) {
                    connectionResult = null;
                    if (!tryApiConnectBlocking()) {
                        tryApiConnectAsync();
                    }
                }

                if (task.isCanceled()) {
                    return null;
                }

                if (connectionResult != null) {
                    // 接続に失敗している
                    return task.connectedFailed(client, connectionResult);
                }
            }

            try {
                if (task.isCanceled()) {
                    return null;
                }
                return task.executeTask(client);
            } catch (Exception e) {
                LogUtil.log(e);
                return null;
            }
        } finally {
            synchronized (lock) {
                --refs;
                if (isDisconnectTarget()) {
                    LogUtil.log("req clean GoogleApiClient");
                    UIHandler.postDelayedUI(disconnectChecker, disconnectPendingTime);
                }
            }
        }
    }

    private boolean isDisconnectTarget() {
        return refs == 0 && client != null && client.isConnected();
    }

    Runnable disconnectChecker = new Runnable() {
        @Override
        public void run() {
            new Thread() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (isDisconnectTarget()) {
                            client.disconnect();
                            client = null;
                        }
                    }
                }
            }.start();
        }
    };

}
