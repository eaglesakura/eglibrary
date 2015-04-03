package com.eaglesakura.android.framework.beacon;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.eaglesakura.android.bluetooth.BluetoothDeviceScanner;
import com.eaglesakura.android.bluetooth.BluetoothDeviceType;
import com.eaglesakura.android.framework.beacon.data.ScanCallbackHolder;
import com.eaglesakura.android.framework.support.service.BaseService;
import com.eaglesakura.android.thread.HandlerLoopController;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BeaconScanService extends BaseService {
    private static final int ALARM_REQUEST_SCANSTART = 0x110000;

    /**
     * デフォルトのスキャン時間は5秒
     */
    long scanTimeMs = 1000 * 5;

    /**
     * デフォルトのスリープ時間は10秒
     */
    long scanSleepTimeMs = 1000 * 10;

    /**
     * 電波を見失ったあとデバイスを切り捨てる猶予時間
     */
    long deviceLostTimeMs = 1000 * 3;

    /**
     * RSSIをキャッシュして電波の整合性をアップさせる時間
     */
    long rssiCacheTimeMs = scanTimeMs / 2;

    /**
     * スキャンの経過内容を定期的に送信するための間隔
     */
    long updateCallIntervalTimeMs = (scanTimeMs / 2);

    /**
     * 利用するコールバック一覧
     */
    Set<ScanCallbackHolder> callbackHolders = new HashSet<>();

    /**
     * Beacon Scanクラス
     */
    BluetoothDeviceScanner scanner;

    /**
     * スキャン中であればtrue
     */
    boolean scanning = false;

    /**
     * スキャン開始時刻
     */
    long scanStartTime;

    /**
     * 定期更新コールバック用
     */
    HandlerLoopController updateLoopController;


    @Override
    public IBinder onBind(Intent intent) {
        requestNextAlarmDelayed(ALARM_REQUEST_SCANSTART, null, 500);

        return new IBeaconScanService.Stub() {
            @Override
            public void setScanInterval(long newScanTimeMs, long newSleepTimeMs) throws RemoteException {
                scanTimeMs = newScanTimeMs;
                scanSleepTimeMs = newSleepTimeMs;
            }

            @Override
            public void setDeviceLostTime(long newTimeMs) throws RemoteException {
                deviceLostTimeMs = newTimeMs;
            }

            @Override
            public void setRssiCacheTime(long newTimeMs) throws RemoteException {
                rssiCacheTimeMs = newTimeMs;
            }

            @Override
            public void setCallbackIntervalTime(long newTimeMs) throws RemoteException {
                updateCallIntervalTimeMs = newTimeMs;
            }

            @Override
            public long getScanTimeMs() throws RemoteException {
                return scanTimeMs;
            }

            @Override
            public long getScanSleepTimeMs() throws RemoteException {
                return scanSleepTimeMs;
            }

            @Override
            public void registerCallback(IBeaconScanCallback callback) throws RemoteException {
                callbackHolders.add(new ScanCallbackHolder(callback));
            }

            @Override
            public void unregisterCallback(IBeaconScanCallback callback) throws RemoteException {
                Iterator<ScanCallbackHolder> iterator = callbackHolders.iterator();
                while (iterator.hasNext()) {
                    ScanCallbackHolder holder = iterator.next();
                    if (holder.getCallback().equals(callback)) {
                        iterator.remove();
                        return;
                    }
                }
            }
        };
//        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callbackHolders.clear();
        stopScan();
    }

    final BluetoothDeviceScanner.DeviceScanListener scanListener = new BluetoothDeviceScanner.DeviceScanListener() {
        @Override
        public void onDeviceFound(BluetoothDeviceScanner self, BluetoothDeviceScanner.BluetoothDeviceCache device) {
            AndroidUtil.assertUIThread();

            try {
                device.parseBeacon(true);

                // デバイスの内容をコールバックする
                for (ScanCallbackHolder holder : callbackHolders) {
                    holder.onBeaconFound(device);
                }
            } catch (Exception e) {
                // not beacon
            }
        }

        @Override
        public void onDeviceUpdated(BluetoothDeviceScanner self, BluetoothDeviceScanner.BluetoothDeviceCache device) {
            AndroidUtil.assertUIThread();

        }

        @Override
        public void onScanTimeout(BluetoothDeviceScanner self) {
            AndroidUtil.assertUIThread();

            // スキャン結果のコールバックを行う
            for (ScanCallbackHolder holder : callbackHolders) {
                holder.onScanFinished(scanStartTime);
            }

            // スキャンを停止させる
            stopScan();
        }
    };

    @Override
    protected void onAlarmReceived(int requestCode, Bundle requestArgments, long delayedTimeMs) {
        if (requestCode == ALARM_REQUEST_SCANSTART) {
            log("request startScan()");
            // スキャン開始の時刻になった
            startScan();
        }
    }

    private void startScan() {
        AndroidUtil.assertUIThread();
        if (scanning) {
            return;
        }

        // CPUを叩き起こす
        requestNonSleep();

        // スキャナを生成
        if (scanner == null) {
            scanner = new BluetoothDeviceScanner(this, BluetoothDeviceType.BluetoothLE);
            scanner.setListener(scanListener);
        }

        // スキャナの初期化
        scanner.setExistCacheTimeMs(deviceLostTimeMs);
        scanner.setRssiCacheTimeMs(rssiCacheTimeMs);

        // スキャン開始
        scanner.startScan(scanTimeMs);
        updateLoopController = new HandlerLoopController(UIHandler.getInstance()) {
            long nextUpdateTime = System.currentTimeMillis() + updateCallIntervalTimeMs;

            @Override
            protected void onUpdate() {
                if (System.currentTimeMillis() > nextUpdateTime) {
                    // スキャン結果のコールバックを行う
                    for (ScanCallbackHolder holder : callbackHolders) {
                        holder.onScanUpdated(scanStartTime);
                    }

                    nextUpdateTime += updateCallIntervalTimeMs;
                }
            }
        };
        updateLoopController.connect();

        scanStartTime = System.currentTimeMillis();
        scanning = true;
    }

    private void stopScan() {
        AndroidUtil.assertUIThread();
        if (!scanning) {
            return;
        }

        // 更新ループを停止する
        updateLoopController.disconnect();
        updateLoopController.dispose();
        updateLoopController = null;

        // 次のタイミングまでCPUは眠りにつく
        scanner.stopScan();
        scanning = false;
        requestNextAlarmDelayed(ALARM_REQUEST_SCANSTART, null, scanSleepTimeMs);
        stopNonSleep();
    }

}
