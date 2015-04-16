package com.eaglesakura.dummybeaconadvertiser.beacon;

import com.eaglesakura.android.bluetooth.beacon.BeaconAdvertiseBuilder;
import com.eaglesakura.util.LogUtil;

import java.util.UUID;

/**
 * Beacon構築用Model
 */
public class DummyBeaconModel {
    public String uuid;
    public short major;
    public short minor;
    public byte txPower = -64;

    /**
     * advertise用のデータを生成する
     *
     * @param model
     * @return
     */
    public static byte[] buildBuffer(DummyBeaconModel model) {
        LogUtil.log("beacon(%s-%d-%d) txPower(%d)", model.uuid, model.major, model.minor, model.txPower);
        BeaconAdvertiseBuilder builder = new BeaconAdvertiseBuilder();
        return builder.setUuid(UUID.fromString(model.uuid)).setMajor(model.major).setMinor(model.minor).setTxPower(model.txPower).build();
    }
}
