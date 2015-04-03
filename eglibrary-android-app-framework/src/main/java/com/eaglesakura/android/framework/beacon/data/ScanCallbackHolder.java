package com.eaglesakura.android.framework.beacon.data;

import android.os.RemoteException;

import com.eaglesakura.android.bluetooth.BluetoothDeviceScanner;
import com.eaglesakura.android.framework.beacon.IBeaconScanCallback;
import com.eaglesakura.util.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * スキャンコールバック対象を指定する
 */
public class ScanCallbackHolder {

    /**
     * スキャンされたBeacon一覧
     */
    List<BluetoothDeviceScanner.BluetoothDeviceCache> beacons = new ArrayList<>();

    /**
     * コールバッククラス
     */
    final IBeaconScanCallback callback;


    public ScanCallbackHolder(IBeaconScanCallback callbacks) {
        this.callback = callbacks;
    }

    PeripheralBeacon createIdlBeacon(BluetoothDeviceScanner.BluetoothDeviceCache cache) {
        return new PeripheralBeacon(
                cache.getDevice().getName(), cache.getAddress(),
                cache.getScanRecord(),
                cache.calcDeviceDistanceMeter(true));
    }

    /**
     * Beacon一覧を生成する
     *
     * @return
     */
    List<PeripheralBeacon> createIdlBeaconList() {
        // 不要なBeaconを削除
        Iterator<BluetoothDeviceScanner.BluetoothDeviceCache> iterator = beacons.iterator();
        while (iterator.hasNext()) {
            BluetoothDeviceScanner.BluetoothDeviceCache cache = iterator.next();
            if (!cache.exist()) {
                iterator.remove();
            }
        }

        // 近い順にソート
        List<PeripheralBeacon> callBeacons = new ArrayList<>();
        BluetoothDeviceScanner.sortNearDevices(beacons);
        for (BluetoothDeviceScanner.BluetoothDeviceCache cache : beacons) {
            callBeacons.add(createIdlBeacon(cache));
        }

        return callBeacons;
    }

    /**
     * Beaconを見つけた
     *
     * @param cache
     */
    public void onBeaconFound(BluetoothDeviceScanner.BluetoothDeviceCache cache) {
        PeripheralBeacon beacon = createIdlBeacon(cache);
        try {
            if (!callback.isIgnoreBeacon(beacon)) {
                beacons.add(cache);
            }
        } catch (RemoteException re) {
            LogUtil.log(re);
        }
    }

    /**
     * スキャンが正常完了した
     */
    public void onScanUpdated(long scanStartedTime) {
//        try {
//            callback.on scanStartedTime, createIdlBeaconList());
//        } catch (RemoteException e) {
//            LogUtil.log(e);
//        }
    }

    /**
     * スキャンが正常完了した
     */
    public void onScanFinished(long scanStartedTime) {
        try {
            callback.onScanFinished(scanStartedTime, createIdlBeaconList());
        } catch (RemoteException e) {
            LogUtil.log(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScanCallbackHolder that = (ScanCallbackHolder) o;

        return !(callback != null ? !callback.equals(that.callback) : that.callback != null);

    }

    @Override
    public int hashCode() {
        return callback != null ? callback.hashCode() : 0;
    }

    public IBeaconScanCallback getCallback() {
        return callback;
    }
}
