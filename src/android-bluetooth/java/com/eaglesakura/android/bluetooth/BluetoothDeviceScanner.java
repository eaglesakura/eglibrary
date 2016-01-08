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

import com.eaglesakura.android.bluetooth.beacon.BeaconData;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    List<BluetoothDeviceCache> deviceCaches = new ArrayList<>();

    /**
     * 指定時間以上前に発見されたデバイスはclean対象となる
     * デフォルト時間は要調整
     */
    long existCacheTimeMs = 1000 * 15;

    /**
     * RSSIをキャッシュする時間
     * デフォルト時間は要調整
     */
    long rssiCacheTimeMs = 1000 * 5;

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
     * ロックオブジェクト
     */
    private Object cacheLock = new Object();

    /**
     * BLEデバイス検索実装クラス
     * 旧APIでビルドできるように隠ぺいする
     */
    @SuppressLint("NewApi")
    private class LeScanCallbackImpl implements LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            synchronized (cacheLock) {
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
    }

    /**
     * 保持しているキャッシュをチェックし、不要なものを削除する
     */
    public void cleanDeviceCaches() {
        synchronized (cacheLock) {
            Iterator<BluetoothDeviceCache> iterator = deviceCaches.iterator();
            while (iterator.hasNext()) {
                BluetoothDeviceCache deviceCache = iterator.next();
                if (!deviceCache.exist()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * キャッシュしているデバイスを全て取得する。
     * <br>
     * このメソッドはコピーを返すため、外部の影響を受けない。
     * <br>
     * その際、無効なデバイスは排除する。
     */
    public List<BluetoothDeviceCache> getExistDeviceCaches() {
        cleanDeviceCaches();
        synchronized (cacheLock) {
            return new ArrayList<BluetoothDeviceCache>(deviceCaches);
        }
    }

    /**
     * キャッシュから指定したデバイスを削除する
     *
     * @param device
     */
    public void remove(BluetoothDevice device) {
        cleanDeviceCaches();
        synchronized (cacheLock) {
            Iterator<BluetoothDeviceCache> iterator = deviceCaches.iterator();
            while (iterator.hasNext()) {
                BluetoothDeviceCache cache = iterator.next();
                if (cache.device == device) {
                    iterator.remove();
                    return;
                }
            }
        }
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
     * RSSIキャッシュ時間を指定
     *
     * @param rssiCacheTimeMs キャッシュが有効な時間(ミリ秒)
     */
    public void setRssiCacheTimeMs(long rssiCacheTimeMs) {
        this.rssiCacheTimeMs = rssiCacheTimeMs;
    }

    /**
     * スキャンを開始する
     */
    @SuppressLint("NewApi")
    public synchronized void startScan(long timeoutMs) {
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
         * iBeaconとして扱う場合のキャッシュデータ
         */
        BeaconData beacon;

        /**
         * 過去のRSSI値
         */
        private class RssiCache {
            /**
             * RSSI値
             */
            int cacheRssi;

            /**
             * RSSIの検知時刻
             */
            long timeMs;

            private RssiCache(int rssi, long timeMs) {
                this.cacheRssi = rssi;
                this.timeMs = timeMs;
            }
        }


        /**
         * record
         */
        byte[] scanRecord;

        /**
         * 発見された時刻
         */
        final Date updatedDate;

        /**
         * 比較用のアドレス
         */
        final String address;

        /**
         * 過去のRSSI値
         */
        private List<RssiCache> rssiCaches = new ArrayList<RssiCache>();

        private BluetoothDeviceCache(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
            this.address = device.getAddress();
            this.updatedDate = new Date();
        }

        public String getAddress() {
            return address;
        }

        /**
         * キャッシュが有効であればtrue
         */
        public boolean exist() {
            return (System.currentTimeMillis() - updatedDate.getTime()) < existCacheTimeMs;
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
         * 更新日時を取得する
         *
         * @return
         */
        public Date getUpdatedDate() {
            return updatedDate;
        }

        /**
         * デバイスからの距離をメートル単位で算出する。
         * <br>
         * 距離は概算となる。また、揺らぎがかなり大きいので、参考値程度に考える。
         *
         * @param fromAverage trueの場合、平均のRSSIを使用する。falseの場合は最新のRSSIを使用する
         *
         * @return デバイスからの距離(m)
         */
        public double calcDeviceDistanceMeter(boolean fromAverage) {

            int calcRssi = this.rssi;
            if (fromAverage) {
                calcRssi = getRssiAverage();
            }

            int txPower = -55;
            if (beacon != null) {
                txPower = beacon.getTxPower();
            }

            return calcDeviceDistance(calcRssi, txPower);
        }

        /**
         * ビーコン情報をパースする
         */
        public void parseBeacon() throws Exception {
            if (beacon == null) {
                beacon = BeaconData.createInstance(device, rssi, scanRecord);
            }
        }

        /**
         * Beacon情報をパースする
         *
         * @param cacheClear 既に取得済みのBeacon情報のキャッシュを廃棄する
         */
        public void parseBeacon(boolean cacheClear) throws Exception {
            if (cacheClear) {
                beacon = null;
            }
            parseBeacon();
        }

        /**
         * ビーコン情報をパースする
         */
        public BeaconData getBeacon() {
            return beacon;
        }

        /**
         * 有効なスキャンキャッシュ中のRSSI平均を取得する
         */
        public int getRssiAverage() {
            synchronized (this) {
                int rssiNum = 1 + rssiCaches.size();
                int rssiSum = this.rssi;

                // キャシュを足し込む
                for (RssiCache cache : rssiCaches) {
                    rssiSum += cache.cacheRssi;
                }

                // 平均を返す
                return rssiSum / rssiNum;
            }
        }

        /**
         * 同期を行う
         */
        private void sync(BluetoothDevice device, int rssi, byte[] scanRecord) {
            assert device != null;
            assert device.getAddress().equals(address);

            final long currentTime = System.currentTimeMillis();
            // キャッシュを保存する
            synchronized (this) {
                Iterator<RssiCache> iterator = rssiCaches.iterator();
                while (iterator.hasNext()) {
                    RssiCache cache = iterator.next();
                    if ((currentTime - cache.timeMs) > rssiCacheTimeMs) {
                        // 有効なキャッシュ時間を超えたらclean
                        iterator.remove();
                    }
                }

                rssiCaches.add(new RssiCache(this.rssi, updatedDate.getTime()));
            }

            this.device = device;
            this.scanRecord = scanRecord;
            this.rssi = rssi;
            this.updatedDate.setTime(currentTime);

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

    /**
     * ビーコンのみをフィルタリングする
     * <br>
     * 事前に BluetoothDeviceCache.parseBeaconを呼んでおく必要がある
     */
    public static List<BluetoothDeviceCache> filterBeacons(List<BluetoothDeviceCache> list) {
        Iterator<BluetoothDeviceCache> iterator = list.iterator();
        while (iterator.hasNext()) {
            BluetoothDeviceCache cache = iterator.next();
            if (cache.getBeacon() == null) {
                iterator.remove();
            }
        }
        return list;
    }

    /**
     * BLEデバイスへの距離を計算する。
     * <br>
     * この距離は揺らぎが大きいため、参考値程度に考える
     *
     * @param rssi    電波強度
     * @param txPower BLEデバイス電波出力
     *
     * @return 距離(メートル)
     */
    public static double calcDeviceDistance(int rssi, int txPower) {
        try {
            // 距離をチェック
            double distance = 0;
            double ratio = (double) rssi / (double) txPower;
            if (ratio < 1.0) {
                distance = Math.pow(ratio, 10);
            } else {
                distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            }

            return distance;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 最も近い位置にあるデバイスを取得する
     * <br>
     * 精度を上げるため、平均RSSIを使用してチェックする。
     *
     * @param devices 検索対象のデバイス一覧
     *
     * @return デバイス
     */
    public static BluetoothDeviceCache pickNearDevice(List<BluetoothDeviceCache> devices) {
        BluetoothDeviceCache result = null;
        double resultDeviceDistance = 99999;

        for (BluetoothDeviceCache cache : devices) {
            final double cacheDeviceDistance = cache.calcDeviceDistanceMeter(true);

            if (result == null || cacheDeviceDistance < resultDeviceDistance) {
                // 戻りが指定されていないか、 新たなデバイスのほうが近い
                result = cache;
                resultDeviceDistance = cacheDeviceDistance;
            }
        }

        return result;
    }

    /**
     * 距離が近い順番にソートする
     * <br>
     * typoのため非推奨
     *
     * @param devices
     *
     * @return
     */
    @Deprecated
    public static List<BluetoothDeviceCache> softNearDevices(List<BluetoothDeviceCache> devices) {
        return sortNearDevices(devices);
    }

    /**
     * 距離が近い順番にソートする
     *
     * @param devices
     *
     * @return
     */
    public static List<BluetoothDeviceCache> sortNearDevices(List<BluetoothDeviceCache> devices) {

        Collections.sort(devices, new Comparator<BluetoothDeviceCache>() {
            @Override
            public int compare(BluetoothDeviceCache lhs, BluetoothDeviceCache rhs) {
                double lDist = lhs.calcDeviceDistanceMeter(true);
                double rDist = rhs.calcDeviceDistanceMeter(true);

                if (lDist < rDist) {
                    return -1;
                } else if (lDist > rDist) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        return devices;
    }
}
