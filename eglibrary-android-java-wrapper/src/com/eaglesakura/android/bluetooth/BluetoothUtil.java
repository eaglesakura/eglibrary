package com.eaglesakura.android.bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class BluetoothUtil {

    /**
     * BluetoothLEをサポートしている場合true
     * API18以上ならtrue
     * @return
     */
    public static boolean isSupportedBluetoothLeAPILevel() {
        return Build.VERSION.SDK_INT >= 18;
    }

    /**
     * BluetoothLEに対応しているデバイスの場合trueを返す
     * @param context
     * @return
     */
    @SuppressLint("InlinedApi")
    public static boolean isSupportedBluetoothLE(Context context) {
        if (Build.VERSION.SDK_INT < 18) {
            // 非対応バージョンのため利用できない
            return false;
        }
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Bluetoothに対応しているデバイスの場合trueを返す
     * @param context
     * @return
     */
    public static boolean isSupportedBluetooth(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

}
