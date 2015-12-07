package com.eaglesakura.android.bluetooth.p2p;

import java.io.IOException;
import java.util.UUID;

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

    BluetoothAdapter adapter;

    public BluetoothServer(Context context) {
        this.context = context;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * サーバーのリクエスト待ち時間を指定する
     *
     * @param serverRequestTimeout
     */
    public void setServerRequestTimeoutMs(long serverRequestTimeout) {
        this.serverRequestTimeoutMs = serverRequestTimeout;
    }

    /**
     * サーバー用接続を行う
     */
    @Override
    protected boolean requestConnecting(UUID protocol) {
        try {
            BluetoothServerSocket serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(context.getPackageName(), protocol);

            socket = serverSocket.accept((int) serverRequestTimeoutMs);
            connectDevice = socket.getRemoteDevice();

            // 1デバイスしか受け付けないため、
            // サーバーソケットは閉じる
            serverSocket.close();

            if (isRequestDisconnect()) {
                socket.close();
                return false;
            }

            startInputThread(socket);
            startOutputThread(socket);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
