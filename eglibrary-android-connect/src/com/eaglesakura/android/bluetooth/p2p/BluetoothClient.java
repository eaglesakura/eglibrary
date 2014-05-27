package com.eaglesakura.android.bluetooth.p2p;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothClient extends BluetoothP2PConnector {

    final BluetoothDevice device;

    BluetoothSocket socket;

    public BluetoothClient(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    protected void requestConnecting() {
        try {

            socket = device.createRfcommSocketToServiceRecord(PROTOCOL_UUID);
            socket.connect();

            startInputThread(socket);
            startOutputThread(socket);
        } catch (IOException ioe) {
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
