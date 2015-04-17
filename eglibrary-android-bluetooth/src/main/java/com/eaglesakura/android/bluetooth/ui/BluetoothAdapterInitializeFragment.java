package com.eaglesakura.android.bluetooth.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.eaglesakura.android.bluetooth.BluetoothUtil;
import com.eaglesakura.android.bluetooth.R;
import com.eaglesakura.android.util.FragmentUtil;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;

/**
 * BluetoothがONであることを保証するFragment
 */
@SuppressLint("NewApi")
@EFragment
public class BluetoothAdapterInitializeFragment extends Fragment {


    @InstanceState
    boolean userIntentStarted = false;

    BluetoothAdapterInitializeListener listener;

    @SuppressLint("NewApi")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof BluetoothAdapterInitializeListener) {
            listener = (BluetoothAdapterInitializeListener) activity;
        } else if (FragmentUtil.isSupportChildFragment() && getParentFragment() instanceof BluetoothAdapterInitializeListener) {
            listener = (BluetoothAdapterInitializeListener) getParentFragment();
        } else {
            throw new IllegalArgumentException("BluetoothAdapterInitializeListener not impl...");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isAdapterEnable()) {
            listener.onBluetoothAdapterInitialized(this);
            FragmentUtil.detatch(this);
        } else {
            if (!userIntentStarted) {
                startBluetoothEnable();
            }
        }
    }

    static final int REQUEST_BLUETOOTH_ENABLE = 0x03103;

    /**
     * bluetoothアダプターが有効になっていたらtrue
     *
     * @return
     */
    @SuppressLint("NewApi")
    boolean isAdapterEnable() {
        BluetoothAdapter bluetoothAdapter;

        if (BluetoothUtil.isSupportedBluetoothLE(getActivity())) {
            BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        // アダプタが無効だったら再設定を行う
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * bluetoothをONに切り替えさせる
     */
    void startBluetoothEnable() {
        userIntentStarted = true;
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
    }

    /**
     * bluetoothチェックの戻り
     *
     * @param result
     * @param data
     */
    @OnActivityResult(REQUEST_BLUETOOTH_ENABLE)
    void resultBluetoothEnable(int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            // チェック
            showErrorDialog();
        }
    }

    void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.Common_Dialog_Title_Error);
        builder.setMessage(R.string.DeviceSetting_Dialog_DeviceDiable_Message);
        builder.setPositiveButton(R.string.DeviceSetting_Dialog_DeviceDiable_Retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startBluetoothEnable();
            }
        });
        builder.setNegativeButton(R.string.Common_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onBluetoothAdapterInitializeFailed(BluetoothAdapterInitializeFragment.this);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public interface BluetoothAdapterInitializeListener {
        /**
         * 初期化に成功した
         *
         * @param self
         */
        void onBluetoothAdapterInitialized(BluetoothAdapterInitializeFragment self);

        void onBluetoothAdapterInitializeFailed(BluetoothAdapterInitializeFragment self);
    }
}
