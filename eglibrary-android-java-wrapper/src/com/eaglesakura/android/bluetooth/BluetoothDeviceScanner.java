package com.eaglesakura.android.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * BluetoothDeviceを自動スキャンする
 */
public class BluetoothDeviceScanner {
    BluetoothManager bluetoothManager;

    BluetoothAdapter bluetoothAdapter;

    /**
     * 発見したデバイス
     */
    List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    /**
     * 
     */
    final Context context;

    final Runnable scanStopRunnable = new Runnable() {

        @Override
        public void run() {
            stopScan();
            listener.onScanTimeout(BluetoothDeviceScanner.this);
        }
    };

    DeviceScanListener listener;

    final BluetoothDeviceType mode;

    /**
     * BLEデバイス検索
     */
    private Object leScanCallback = null;

    /**
     * BLEデバイス検索実装クラス
     * 旧APIでビルドできるように隠ぺいする
     */
    @SuppressLint("NewApi")
    private class LeScanCallbackImpl implements LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (Util.addUnique(devices, device)) {
                // le connected
                listener.onDeviceFound(BluetoothDeviceScanner.this, device);
            }
        }
    };

    /**
     * Bluetoothデバイス検索
     */
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtil.d("receive action :: " + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // デバイス検出
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // リストに追加
                if (device != null && Util.addUnique(devices, device)) {
                    listener.onDeviceFound(BluetoothDeviceScanner.this, device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // スキャンを停止させる
                UIHandler.postUI(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.d("stop scan");
                        stopScan();
                    }
                });
            }
        }
    };

    /**
     * デバイススキャンを行う
     * @param context
     */
    @SuppressLint("InlinedApi")
    public BluetoothDeviceScanner(Context context, BluetoothDeviceType mode) {
        this.context = context;
        this.mode = mode;

        if (mode == BluetoothDeviceType.BluetoothLE) {
            if (!BluetoothUtil.isSupportedBluetoothLeAPILevel()) {
                throw new UnsupportedOperationException("BLE not support API LEVEL");
            }

            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            // コールバッククラスを設定する
            leScanCallback = new LeScanCallbackImpl();
        }
    }

    /**
     * リスナ指定を行う
     * @param listener
     */
    public void setListener(DeviceScanListener listener) {
        this.listener = listener;
    }

    /**
     * スキャンしたデバイスを取得する
     * @return
     */
    public List<BluetoothDevice> getBluetoothDevices() {
        return devices;
    }

    /**
     * スキャンを開始する
     */
    @SuppressLint("NewApi")
    public synchronized void startScan(int timeoutMs) {
        LogUtil.d("scan mode :: " + mode);
        if (mode == BluetoothDeviceType.BluetoothLE) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                throw new IllegalStateException("Bluetooth disabled...");
            }

            bluetoothAdapter.startLeScan((LeScanCallback) leScanCallback);
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(bluetoothReceiver, filter);
            bluetoothAdapter.startDiscovery();
        }
        UIHandler.postDelayedUI(scanStopRunnable, timeoutMs);
    }

    /**
     * スキャンを停止する。
     * タイムアウトコールバックもキャンセルされる
     */
    @SuppressLint("NewApi")
    public synchronized void stopScan() {
        if (bluetoothAdapter != null) {
            if (mode == BluetoothDeviceType.BluetoothLE) {
                bluetoothAdapter.stopLeScan((LeScanCallback) leScanCallback);
            } else {
                context.unregisterReceiver(bluetoothReceiver);
                bluetoothAdapter.cancelDiscovery();
            }
            UIHandler.getInstance().removeCallbacks(scanStopRunnable);

            bluetoothAdapter = null;
        }
    }

    public interface DeviceScanListener {
        /**
         * デバイスを見つけた
         * @param device
         */
        void onDeviceFound(BluetoothDeviceScanner self, BluetoothDevice device);

        /**
         * スキャンがタイムアウトした
         * @param self
         */
        void onScanTimeout(BluetoothDeviceScanner self);
    }
}
