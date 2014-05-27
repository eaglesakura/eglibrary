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

    public BluetoothServer(Context context) {
        this.context = context;
    }

    /**
     * サーバー用接続を行う
     */
    @Override
    protected void requestConnecting() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothServerSocket serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(context.getPackageName(), PROTOCOL_UUID);

            BluetoothSocket socket = serverSocket.accept((int) getConnectorTimeoutMs());

            // 1デバイスしか受け付けないため、
            // サーバーソケットは閉じる
            serverSocket.close();

            startInputThread(socket);
            startOutputThread(socket);
        } catch (IOException e) {
            synchronized (lock) {
                for (P2PConnectorListener listener : listeners) {
                    listener.onConnectorStateChanged(BluetoothServer.this, null, ConnectorState.Failed);
                }
            }
        }
    }
}
