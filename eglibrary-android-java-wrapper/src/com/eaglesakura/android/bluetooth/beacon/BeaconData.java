package com.eaglesakura.android.bluetooth.beacon;

import android.bluetooth.BluetoothDevice;

import com.eaglesakura.io.DataInputStream;
import com.eaglesakura.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 *
 */
public class BeaconData {
    /**
     * 一意に割り当てられたID
     */
    UUID uuid;

    /**
     * AD type
     */
    byte adType;

    /**
     * 会社識別
     */
    short companyType;

    /**
     * Major値
     */
    short major;

    /**
     * Minor値
     */
    short minor;

    /**
     * 電波強度修正値
     */
    byte txPower;

    private BeaconData() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public byte getAdType() {
        return adType;
    }

    public byte getTxPower() {
        return txPower;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public static BeaconData createInstance(BluetoothDevice device, int rssi, byte[] scanRecord) throws Exception {
        if (scanRecord.length < 30) {
            throw new IllegalArgumentException("scanRecord.length < 30");
        }

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(scanRecord), true);
        dis.readBuffer(4); // 不要データを読み飛ばす

        BeaconData result = new BeaconData();
        result.adType = dis.readS8();
        result.companyType = dis.readS16();
        if (dis.readS8() != 0x02) {
            // is not Beacon!!
            throw new IllegalStateException("this record != beacon");
        }

        // skip
        dis.readS8();

        // UUID
        {
            byte[] uuid = dis.readBuffer(16);
            String uuidText = "";
            int index = 0;
            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);

            uuidText += "-";

            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);

            uuidText += "-";

            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);

            uuidText += "-";

            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);

            uuidText += "-";

            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);
            uuidText += StringUtil.toHexString(uuid[index++]);

            result.uuid = UUID.fromString(uuidText);
        }

        // major/minor
        result.major = dis.readS16();
        result.minor = dis.readS16();

        // tx
        result.txPower = dis.readS8();

        return result;
    }
}
