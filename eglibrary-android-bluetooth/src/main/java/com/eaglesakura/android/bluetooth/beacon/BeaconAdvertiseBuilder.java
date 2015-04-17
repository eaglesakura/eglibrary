package com.eaglesakura.android.bluetooth.beacon;

import com.eaglesakura.io.DataOutputStream;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * Beacon情報を構築するためのビルダー
 */
public class BeaconAdvertiseBuilder {
    UUID uuid;
    byte[] company = new byte[]{(byte) (0x4C), (byte) 0x00};
    short major;
    short minor;
    byte txPower;

    public BeaconAdvertiseBuilder setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * 企業データを指定する
     * <br>
     * default
     * ** high = 0x4C
     * ** low  = 0x00
     *
     * @param high
     * @param low
     */
    public BeaconAdvertiseBuilder setCompany(byte high, byte low) {
        this.company[0] = high;
        this.company[1] = low;
        return this;
    }

    public BeaconAdvertiseBuilder setMajor(short major) {
        this.major = major;
        return this;
    }

    public BeaconAdvertiseBuilder setMinor(short minor) {
        this.minor = minor;
        return this;
    }

    public BeaconAdvertiseBuilder setTxPower(byte txPower) {
        this.txPower = txPower;
        return this;
    }

    public byte[] build() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(os, true);

        try {
            // 固定ヘッダ
            dos.writeS8(company[0]);
            dos.writeS8(company[1]);
            dos.writeS8((byte) 0x02);
            dos.writeS8((byte) 0x15);

            // UUID
            dos.writeS64(uuid.getMostSignificantBits());
            dos.writeS64(uuid.getLeastSignificantBits());

            // major/minor
            dos.writeS16(major);
            dos.writeS16(minor);

            // txpower
            dos.writeS8(txPower);

            // build!
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

}
