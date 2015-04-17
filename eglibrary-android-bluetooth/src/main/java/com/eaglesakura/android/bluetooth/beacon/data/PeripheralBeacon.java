package com.eaglesakura.android.bluetooth.beacon.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.eaglesakura.android.bluetooth.beacon.BeaconData;
import com.eaglesakura.util.StringUtil;

/**
 * Serviceが検知したBeacon情報
 */
public class PeripheralBeacon implements Parcelable {
    /**
     * スキャン結果
     */
    private byte[] record;


    /**
     * デバイス名
     */
    private String name;

    /**
     * Beaconまでの距離（メートル単位）
     */
    private double distanceMeter;

    /**
     * MacAddress
     */
    private String macAddress;

    private BeaconData beacon;

    public PeripheralBeacon(String name, String macAddress, byte[] record, double distanceMeter) {
        this.name = StringUtil.isEmpty(name) ? "BeaconStub" : name;
        this.macAddress = macAddress.toUpperCase();
        this.record = record;
        this.distanceMeter = distanceMeter;

        if (this.record == null || this.name == null || StringUtil.isEmpty(this.macAddress)) {
            throw new IllegalStateException();
        }
    }

    PeripheralBeacon(Parcel in) {
        this.readFromParcel(in);
    }

    /**
     * Beaconを一意に識別可能なIDを生成する
     *
     * @return
     */
    public String getUniqueId() {
        BeaconData data = getBeaconData();
        return BeaconData.createUniqueID(data.getUuid().toString(), data.getMajor(), data.getMinor(), this.macAddress);
    }

    public BeaconData getBeaconData() {
        if (beacon == null) {
            try {
                beacon = BeaconData.createInstance(record);
            } catch (Exception e) {
                throw new IllegalStateException();
            }
        }
        return beacon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistanceMeter() {
        return distanceMeter;
    }

    public void setDistanceMeter(double distanceMeter) {
        this.distanceMeter = distanceMeter;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        this.name = in.readString();
        this.macAddress = in.readString();
        {
            int recordSize = in.readInt();
            this.record = new byte[recordSize];
            in.readByteArray(record);
        }
        this.distanceMeter = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(macAddress);
        {
            dest.writeInt(record.length);
            dest.writeByteArray(record);
        }
        dest.writeDouble(distanceMeter);
    }

    public static final Creator<PeripheralBeacon> CREATOR
            = new Creator<PeripheralBeacon>() {
        public PeripheralBeacon createFromParcel(Parcel in) {
            return new PeripheralBeacon(in);
        }

        public PeripheralBeacon[] newArray(int size) {
            return new PeripheralBeacon[size];
        }
    };

}
