package com.eaglesakura.android.bluetooth.p2p;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothClient extends BluetoothP2PConnector {

    final BluetoothDevice device;

    public BluetoothClient(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    protected void requestConnecting() {
        try {

            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(PROTOCOL_UUID);
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

}
