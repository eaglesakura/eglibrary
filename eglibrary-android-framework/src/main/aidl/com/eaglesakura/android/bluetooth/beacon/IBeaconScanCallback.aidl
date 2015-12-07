// IBeaconScanCallback.aidl
package com.eaglesakura.android.bluetooth.beacon;

import com.eaglesakura.android.bluetooth.beacon.IBeaconScanService;
import PeripheralBeacon;

interface IBeaconScanCallback {
    /**
     * Beaconを発見した
     */
    void onFoundBeacon(in PeripheralBeacon beacon);

    /**
     * Beaconスキャンの途中経過を取得する
     */
    void onScanUpdated(long scanStartedTime, in List<PeripheralBeacon> beacons);

    /**
     * Beaconのスキャンを終了した
     *
     * 引数には検出したBeaconを近い順にリスト化して設定される
     */
    void onScanFinished(long scanStartedTime, in List<PeripheralBeacon> beacons);

    /**
     * Beaconを無視するのであればtrueを返却する
     */
    boolean isIgnoreBeacon(in PeripheralBeacon beacon);
}
