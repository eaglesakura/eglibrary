package com.eaglesakura.android.bluetooth.p2p;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothClient extends BluetoothP2PConnector {

    BluetoothSocket socket;

    public BluetoothClient(BluetoothDevice device) {
        this.connectDevice = device;
    }

    @Override
    protected void requestConnecting(UUID protocol) {
        try {

            socket = connectDevice.createRfcommSocketToServiceRecord(protocol);
            socket.connect();

            startInputThread(socket);
            startOutputThread(socket);
        } catch (Exception ioe) {
            ioe.printStackTrace();

            synchronized (lock) {
                for (P2PConnectorListener listener : listeners) {
                    listener.onConnectorStateChanged(BluetoothClient.this, null, ConnectorState.Failed);
                }
            }
        }

    }

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
