package com.eaglesakura.android.bluetooth;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

@SuppressLint("NewApi")
public class BluetoothLeUtil {

    /**
     * config
     */
    public static final UUID BLE_UUID_CLIENT_CHARACTERISTIC_CONFIG = createUUIDFromAssignedNumber("0x2902");

    /**
     * developer.bluetooth.orgに示されるAssigned NumberからUUIDを生成する
     * @param an
     * @return
     */
    public static UUID createUUIDFromAssignedNumber(String an) {
        if (an.startsWith("0x")) {
            an = an.substring(2);
        }

        return UUID.fromString(String.format("0000%s-0000-1000-8000-00805f9b34fb", an));
    }

    /**
     * 指定したCharacteristicを取得する
     * @param gatt
     * @param serviceUuid
     * @param characteristicUuid
     * @return
     */
    public static BluetoothGattCharacteristic findBluetoothGattCharacteristic(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid) {
        if (gatt == null) {
            return null;
        }
        BluetoothGattService service = gatt.getService(serviceUuid);
        if (service == null) {
            return null;
        }

        return service.getCharacteristic(characteristicUuid);
    }

    /**
     * 通知をONにする
     * @param gatt
     * @param characteristic
     */
    public static void notificationEnable(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, true);

        // notificationを有効化する
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLE_UUID_CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }

    /**
     * 通知をOFFにする
     * @param gatt
     * @param characteristic
     */
    public static void notificationDisable(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, false);

        // notificationを有効化する
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLE_UUID_CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);

    }
}
