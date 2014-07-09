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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * BluetoothDeviceを自動スキャンする
 */
public class BluetoothDeviceScanner {
    BluetoothManager bluetoothManager;

    BluetoothAdapter bluetoothAdapter;

    /**
     * 発見したデバイスのキャッシュ
     */
    List<BluetoothDeviceCache> deviceCaches = new ArrayList<BluetoothDeviceCache>();

    /**
     * 指定時間以上前に発見されたデバイスはclean対象となる
     * デフォルト時間は要調整
     */
    long existCacheTimeMs = 1000 * 15;

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
            BluetoothDeviceCache cache = getDeviceCache(device);
            if (cache == null) {
                // キャッシュがないので、新規ヒットしたデバイスである
                cache = new BluetoothDeviceCache(device, rssi, scanRecord);

                // キャッシュを追加する
                deviceCaches.add(cache);

                // コールバック呼び出し
                listener.onDeviceFound(BluetoothDeviceScanner.this, cache);
            } else {
                // キャッシュを更新する
                cache.sync(device, rssi, scanRecord);

                // コールバック呼び出し
                listener.onDeviceUpdated(BluetoothDeviceScanner.this, cache);
            }
        }
    }

    /**
     * 保持しているキャッシュをチェックし、不要なものを削除する
     */
    public void cleanDeviceCaches() {
        Iterator<BluetoothDeviceCache> iterator = deviceCaches.iterator();
        while (iterator.hasNext()) {
            BluetoothDeviceCache deviceCache = iterator.next();
            if (!deviceCache.exist()) {
                iterator.remove();
            }
        }
    }

    /**
     * キャッシュしているデバイスを全て取得する。
     * <p/>
     * その際、無効なデバイスは排除する。
     */
    public List<BluetoothDeviceCache> getExistDeviceCaches() {
        cleanDeviceCaches();
        return deviceCaches;
    }

    /**
     * キャッシュ時刻が有効なキャッシュ一覧を取得する
     */
    private BluetoothDeviceCache getDeviceCache(BluetoothDevice device) {

        // 不要なデータを削除する
        cleanDeviceCaches();

        // キャッシュチェック
        for (BluetoothDeviceCache cache : deviceCaches) {
            if (cache.address.equals(device.getAddress())) {
                // 一致した
                return cache;
            }
        }

        // キャッシュヒットしなかった
        return null;
    }

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
                if (device != null) {
                    BluetoothDeviceCache cache = getDeviceCache(device);

                    // 電波強度を取得
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, RSSI_UNKNOWN);

                    if (cache == null) {
                        // キャッシュが見つからないので、新規にヒットしたデバイス
                        cache = new BluetoothDeviceCache(device, rssi, null);
                        deviceCaches.add(cache);

                        listener.onDeviceFound(BluetoothDeviceScanner.this, cache);
                    } else {
                        // キャッシュヒットしたので、更新する
                        cache.sync(device, rssi, null);
                        listener.onDeviceUpdated(BluetoothDeviceScanner.this, cache);
                    }
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
     */
    public void setListener(DeviceScanListener listener) {
        this.listener = listener;
    }

    /**
     * キャッシュが有効な時間を指定する
     *
     * @param existCacheTimeMs キャッシュが有効な時間(ミリ秒)
     */
    public void setExistCacheTimeMs(long existCacheTimeMs) {
        this.existCacheTimeMs = existCacheTimeMs;
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
         * デバイスを見つけた場合に呼び出される。
         *
         * @param self   コールバック呼び出し元
         * @param device 発見したデバイス
         */
        void onDeviceFound(BluetoothDeviceScanner self, BluetoothDeviceCache device);

        /**
         * デバイス情報が更新された場合に呼び出される。
         *
         * @param self   　コールバック呼び出し元
         * @param device 更新されたデバイス
         */
        void onDeviceUpdated(BluetoothDeviceScanner self, BluetoothDeviceCache device);

        /**
         * スキャンがタイムアウトした
         *
         * @param self コールバック呼び出し元
         */
        void onScanTimeout(BluetoothDeviceScanner self);
    }

    /**
     * 不明なRSSI値
     */
    public static final short RSSI_UNKNOWN = (short) 0xFEFE;

    /**
     * 発見したデバイスのキャッシュを行う
     */
    public class BluetoothDeviceCache {
        /**
         * デバイス
         */
        BluetoothDevice device;

        /**
         * 電波強度
         */
        int rssi = RSSI_UNKNOWN;


        /**
         * record
         */
        byte[] scanRecord;

        /**
         * 発見された時刻
         */
        final Date connectedDate;

        /**
         * 比較用のアドレス
         */
        final String address;

        private BluetoothDeviceCache(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
            this.address = device.getAddress();
            this.connectedDate = new Date();
        }

        /**
         * キャッシュが有効であればtrue
         */
        public boolean exist() {
            return (System.currentTimeMillis() - connectedDate.getTime()) < existCacheTimeMs;
        }

        /**
         * デバイスを取得する
         */
        public BluetoothDevice getDevice() {
            return device;
        }

        /**
         * 電波強度を取得する
         */
        public int getRssi() {
            return rssi;
        }

        /**
         * recordを取得する
         */
        public byte[] getScanRecord() {
            return scanRecord;
        }

        /**
         * 同期を行う
         */
        private void sync(BluetoothDevice device, int rssi, byte[] scanRecord) {
            assert device != null;
            assert device.getAddress().equals(address);

            this.device = device;
            this.scanRecord = scanRecord;
            this.rssi = rssi;
            this.connectedDate.setTime(System.currentTimeMillis());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BluetoothDeviceCache that = (BluetoothDeviceCache) o;

            if (address != null ? !address.equals(that.address) : that.address != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return address != null ? address.hashCode() : 0;
        }
    }
}
