package com.eaglesakura.dummybeaconadvertiser;

import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisementData;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.androidquery.AQuery;
import com.eaglesakura.android.bluetooth.beacon.BeaconAdvertiseBuilder;
import com.eaglesakura.android.framework.ui.BaseFragment;
import com.eaglesakura.dummybeaconadvertiser.beacon.DummyBeaconModel;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
@EFragment(R.layout.fragment_dummy_beacon)
public class DummyBeaconAdvertiseFragment extends BaseFragment {
    DummyBeaconModel[] beacons = new DummyBeaconModel[0];
    Map<DummyBeaconModel, AdvertiseCallback> beaconAdvertises = new HashMap<DummyBeaconModel, AdvertiseCallback>();

    @ViewById(R.id.DummyBeacon_List_Root)
    ListView beaconList;

    BluetoothManager bluetoothManager;

    BluetoothLeAdvertiser advertiser;

    @Override
    @AfterViews
    protected void onAfterViews() {
        bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        advertiser = bluetoothManager.getAdapter().getBluetoothLeAdvertiser();
        loadBeacons();
    }

    /**
     * 起動されているBeacon一覧を全て削除する
     */
    void cleanBeacons() {
        // advertise stop
        Iterator<Map.Entry<DummyBeaconModel, AdvertiseCallback>> iterator = beaconAdvertises.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DummyBeaconModel, AdvertiseCallback> next = iterator.next();
            advertiser.stopAdvertising(next.getValue());
        }
        beaconAdvertises.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanBeacons();
    }

    /**
     * Beacon情報を読み込む
     */
    @Background
    void loadBeacons() {
        try {
            pushProgress("load json");
            File json = new File(Environment.getExternalStorageDirectory(), "beacons.json");
            FileInputStream is = new FileInputStream(json);
            DummyBeaconModel[] newBeacons = JSON.decode(is, DummyBeaconModel[].class);

            updateUI(newBeacons);
        } catch (Exception e) {
            LogUtil.log(e);
        } finally {
            popProgress();
        }
    }

    /**
     * Beaconを更新する
     *
     * @param newBeacons
     */
    @UiThread
    void updateUI(DummyBeaconModel[] newBeacons) {
        cleanBeacons();

        this.beacons = newBeacons;
        BaseAdapter adapter = (BaseAdapter) beaconList.getAdapter();

        if (adapter == null) {
            adapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    return beacons.length;
                }

                @Override
                public Object getItem(int position) {
                    return beacons[position];
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = View.inflate(getActivity(), R.layout.card_dummy_beacon, null);
                    }

                    bindView(convertView, beacons[position]);
                    return convertView;
                }
            };
            beaconList.setAdapter(adapter);
            return;
        } else {
            adapter.notifyDataSetChanged();
            beaconList.invalidateViews();
        }
    }

    /**
     * Viewのセットアップを行う
     *
     * @param convertView
     * @param beacon
     */
    void bindView(View convertView, final DummyBeaconModel beacon) {
        final AQuery q = new AQuery(convertView);
        q.id(R.id.DummyBeacon_Card_Enable).checked(beaconAdvertises.containsKey(beacon));
        ((Switch) q.getView()).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean enabled) {
                if (enabled) {
                    // チェックがONになったら、Beaconを登録する
                    advertiser.startAdvertising(createAdvertiseSettings(), createAdvertisementData(beacon), new AdvertiseCallback() {
                        @Override
                        public void onSuccess(AdvertiseSettings advertiseSettings) {
                            logi("success advertise!!");
                            beaconAdvertises.put(beacon, this);
                        }

                        @Override
                        public void onFailure(int i) {
                            logi("failed advertise...");
                            q.id(R.id.DummyBeacon_Card_Enable).checked(false);
                        }
                    });
                } else {
                    // チェックがOFFになったら、Beaconを削除する
                    AdvertiseCallback callback = beaconAdvertises.remove(beacon);
                    if (callback != null) {
                        advertiser.stopAdvertising(callback);
                    }
                }
            }
        });
        q.id(R.id.DummyBeacon_Card_UUID).text(beacon.uuid);
        q.id(R.id.DummyBeacon_Card_Major).text(String.format("Major = %d", beacon.major));
        q.id(R.id.DummyBeacon_Card_Minor).text(String.format("Minor = %d", beacon.minor));
    }

    AdvertiseSettings createAdvertiseSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        builder.setType(AdvertiseSettings.ADVERTISE_TYPE_NON_CONNECTABLE);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        return builder.build();
    }

    AdvertisementData createAdvertisementData(DummyBeaconModel beacon) {
        AdvertisementData.Builder builder = new AdvertisementData.Builder();

        byte[] rawBeacon = DummyBeaconModel.buildBuffer(beacon);
        builder.setManufacturerData(0x3103, rawBeacon);

        return builder.build();
    }
}
