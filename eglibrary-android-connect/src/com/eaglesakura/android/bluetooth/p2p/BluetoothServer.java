package com.eaglesakura.android.bluetooth.p2p;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

/**
 * Bluetooth通信用親機となるサーバー
 */
public class BluetoothServer extends BluetoothP2PConnector {

    final Context context;

    BluetoothSocket socket;

    /**
     * サーバーのリクエスト待ちは5分に指定する
     */
    long serverRequestTimeoutMs = 1000 * 60 * 5;

    public BluetoothServer(Context context) {
        this.context = context;
    }

    /**
     * サーバーのリクエスト待ち時間を指定する
     * @param serverRequestTimeout
     */
    public void setServerRequestTimeoutMs(long serverRequestTimeout) {
        this.serverRequestTimeoutMs = serverRequestTimeout;
    }

    /**
     * サーバー用接続を行う
     */
    @Override
    protected void requestConnecting() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothServerSocket serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(context.getPackageName(), PROTOCOL_UUID);

            socket = serverSocket.accept((int) serverRequestTimeoutMs);

            // 1デバイスしか受け付けないため、
            // サーバーソケットは閉じる
            serverSocket.close();

            if (isRequestDisconnect()) {
                socket.close();
                return;
            }

            startInputThread(socket);
            startOutputThread(socket);
        } catch (IOException e) {
            e.printStackTrace();
            synchronized (lock) {
                for (P2PConnectorListener listener : listeners) {
                    listener.onConnectorStateChanged(BluetoothServer.this, null, ConnectorState.Failed);
                }
            }
        }
    }

    /**
     * 切断を行う
     */
    @Override
    protected void requestDisconnecting() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }
}
