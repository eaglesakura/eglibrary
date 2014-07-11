package com.eaglesakura.android.bluetooth.beacon;

import android.bluetooth.BluetoothDevice;

import com.eaglesakura.io.DataInputStream;
import com.eaglesakura.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Beacon仕様に従ったデータを構築する
 */
public class BeaconData {


    /**
     * Beacon仕様に従ったデータにパースする
     *
     * @param device
     * @param rssi
     * @param scanRecord
     * @return
     */
    public static BeaconData createInstance(BluetoothDevice device, int rssi, byte[] scanRecord) throws IOException {
        if (scanRecord.length < 30) {
            // beacon仕様に足りない長さの場合は失敗
            LogUtil.d("scanRecord data error");
            return null;
        }

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(scanRecord), true);

        byte block1bytes = dis.readS8(); // 1byte 1ブロック目の容量(byte
        short flag = dis.readS16(); // 2byte フラグ
        byte block2bytes = dis.readS8(); // 1byte 2ブロック目の容量(byte
        byte adType = dis.readS8(); // 1byte ADタイプ
        short companyType = dis.readS16(); // 2byte 会社識別ID
        byte dataType = dis.readS8(); // 1byte データタイプ
        byte beaconDataSize = dis.readS8(); // 1byte データ容量(byte
        byte[] uid = dis.readBuffer(16); // 16byte UUID
        int major = dis.readS32(); // 4byte major
        int minor = dis.readS32(); // 4byte minor
        byte txPower = dis.readS8(); // 1byte 電波強度修正値

//        LogUtil.log(String.format("adType[%1$x]", adType));
//        LogUtil.log(String.format("companyType[%1$x]", companyType));
//        LogUtil.log(String.format("dataType[%1$x]", dataType));
//        LogUtil.log(String.format("beaconDataSize[%d]", beaconDataSize));
//        LogUtil.log(String.format("major[%1$x] minor[%1$x], txPower[%d]", major, minor, Byte.toString(txPower)));

        txPower = -55;
        // distance check
        double distance = 0;
        double ratio = (double) rssi / (double) txPower;
        if (ratio < 1.0) {
            distance = Math.pow(ratio, 10);
        } else {
            distance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
        }

        LogUtil.log(String.format("class[%d] distance(%.2f m)", device.getBluetoothClass().getDeviceClass(), distance));

        return null;
    }
}
