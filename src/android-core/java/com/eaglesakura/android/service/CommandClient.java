package com.eaglesakura.android.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.eaglesakura.android.service.aidl.ICommandClientCallback;
import com.eaglesakura.android.service.aidl.ICommandServerService;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.util.LogUtil;

/**
 * 別プロセスServiceと通信するためのインターフェース
 */
public abstract class CommandClient {
    private final Context context;

    private ICommandServerService server;

    private String id;

    public CommandClient(Context context) {
        this.context = context;
        this.id = getClass().getName();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Serviceに接続済みであればtrue
     *
     * @return
     */
    public boolean isConnected() {
        return server != null;
    }

    protected ICommandServerService getServer() {
        return server;
    }

    protected void connectToSever(Intent intent) {
        AndroidUtil.assertUIThread();

        if (server != null) {
            return;
        }

        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 切断リクエストを送る
     */
    public void disconnect() {
        AndroidUtil.assertUIThread();

        if (server == null) {
            // not connected
            return;
        }

        try {
            server.unregisterCallback(callback);
        } catch (Exception e) {
            LogUtil.log(e);
        }

        context.unbindService(connection);
        server = null;
        onDisconnected();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            UIHandler.postUIorRun(new Runnable() {
                @Override
                public void run() {
                    ICommandServerService newServer = ICommandServerService.Stub.asInterface(service);
                    try {
                        server.registerCallback(getId(), callback);
                    } catch (RemoteException e) {
                        throw new IllegalStateException();
                    }

                    server = newServer;
                    onConnected();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            UIHandler.postUIorRun(new Runnable() {
                @Override
                public void run() {
                    if (server != null) {
                        server = null;
                        onDisconnected();
                    }
                }
            });
        }
    };


    private ICommandClientCallback callback = new ICommandClientCallback.Stub() {
        @Override
        public byte[] postToClient(String cmd, byte[] buffer) throws RemoteException {
            return onReceivedData(cmd, buffer);
        }
    };

    /**
     * サーバーにデータを送信する
     *
     * @param cmd
     * @param buffer
     * @return
     * @throws RemoteException
     */
    protected byte[] requestPostToServer(String cmd, byte[] buffer) throws RemoteException {
        if (server == null) {
            throw new IllegalStateException("Server not connected");
        }

        return server.postToServer(cmd, buffer);
    }

    /**
     * サーバーからのデータ取得時のハンドリングを行う
     *
     * @param cmd
     * @param buffer
     * @return
     */
    protected byte[] onReceivedData(String cmd, byte[] buffer) {
        return null;
    }

    /**
     * サーバーに接続完了した
     */
    protected void onConnected() {

    }

    /**
     * サーバーからデータ切断された
     */
    protected void onDisconnected() {

    }
}
