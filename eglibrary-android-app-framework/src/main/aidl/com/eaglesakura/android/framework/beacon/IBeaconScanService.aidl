// IBeaconScanService.aidl
package com.eaglesakura.android.framework.beacon;

import com.eaglesakura.android.framework.beacon.IBeaconScanCallback;

interface IBeaconScanService {
    /**
     * スキャンと休眠の間隔を設定する
     */
    void setScanInterval(long scanTimeMs, long sleepTimeMs);

    /**
     * Beaconの電波を見失っても、一定時間は検出を続けるようにする
     */
    void setDeviceLostTime(long timeMs);

    /**
     * RSSIのキャッシュ時間を指定する
     */
    void setRssiCacheTime(long timeMs);

    /**
     * スキャン経過の更新を行わせる間隔を指定する
     */
    void setCallbackIntervalTime(long timeMs);

    /**
     * 現在のスキャン時間を取得する
     */
    long    getScanTimeMs();

    /**
     * 現在のスキャン後の休眠時間を取得する
     */
    long    getScanSleepTimeMs();

    /**
     * コールバックの登録を行う
     */
    void registerCallback(IBeaconScanCallback callback);

    /**
     * コールバックの削除を行う
     */
    void unregisterCallback(IBeaconScanCallback callback);
}
