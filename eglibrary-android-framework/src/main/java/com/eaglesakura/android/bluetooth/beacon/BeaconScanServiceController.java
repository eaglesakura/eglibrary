package com.eaglesakura.android.bluetooth.beacon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.eaglesakura.android.bluetooth.BluetoothUtil;

/**
 * BeaconScanServiceを利用するコントローラー
 */
public class BeaconScanServiceController {
    final Context context;

    IBeaconScanCallback scanCallback;

    IBeaconScanService service;

    public BeaconScanServiceController(Context context) {
        this.context = context;
    }

    public IBeaconScanService getService() {
        return service;
    }

    public void connect(IBeaconScanCallback callback) {
        if (!BluetoothUtil.isSupportedBluetoothLE(context)) {
            // ble not supported
            return;
        }

        if (!(callback instanceof IBeaconScanCallback.Stub)) {
            throw new IllegalStateException("!= extends IBeaconScanCallback.Stub");
        }
        if (service != null) {
            throw new IllegalStateException("connected service");
        }

        Intent intent = new Intent(context, BeaconScanService.class);
        if (!context.bindService(intent, beaconServiceConnection, Context.BIND_AUTO_CREATE)) {
            throw new IllegalStateException("BeaconScanService bind failed");
        } else {
            this.scanCallback = callback;
        }
    }

    public void disconnect() {
        if (service == null) {
            return;
        }

        context.unbindService(beaconServiceConnection);
        service = null;
        scanCallback = null;
        onServiceUnbinded();
    }

    private ServiceConnection beaconServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder newService) {
            service = IBeaconScanService.Stub.asInterface(newService);
            try {
                service.registerCallback(scanCallback);

                onServiceBinded(service);
            } catch (Exception e) {
                throw new IllegalStateException();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                service.unregisterCallback(scanCallback);
            } catch (Exception e) {
//                throw new IllegalStateException();
            }
            service = null;
            onServiceUnbinded();
        }
    };

    protected void onServiceBinded(IBeaconScanService service) {
    }

    protected void onServiceUnbinded() {
    }
}
