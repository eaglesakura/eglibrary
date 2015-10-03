package com.eaglesakura.android.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * バッテリー関連のイベントを受け取る
 */
public class BatteryEventReceiver extends BroadcastReceiver {

    final Context context;

    /**
     * 充電状態
     */
    Boolean powerConnected;

    /**
     * バッテリー残量レベル
     */
    Float powerLevel;

    public BatteryEventReceiver(Context context) {
        this.context = context.getApplicationContext();
    }

    public float getPowerLevel() {
        if (powerLevel == null) {
            return 1;
        }
        return powerLevel;
    }

    public boolean isPowerConnected() {
        throw new IllegalStateException("not support getPowerLevel()");
//        if (powerConnected == null) {
//            return false;
//        }
//
//        return powerConnected;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            // バッテリー残量レベルをチェックする
            {
                float currentLevel = (float) intent.getIntExtra("level", 0);
                float scale = (float) intent.getIntExtra("scale", 1);

                final float newScale = currentLevel / scale;
                if (newScale != getPowerLevel()) {
                    this.powerLevel = newScale;
                    onBatteryLevelChanged(newScale);
                }
            }
        }
    }

    public void connect() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
//        filter.addAction(Intent.ACTION_BATTERY_LOW);
//        filter.addAction(Intent.ACTION_BATTERY_OKAY);

        context.registerReceiver(this, filter);
    }

    public void disconnect() {
        context.unregisterReceiver(this);
    }

    /**
     * 接続状態が変化した
     *
     * @param newState
     */
    protected void onPowerConnectStateChanged(boolean newState) {

    }

    /**
     * バッテリー残量が変化した
     *
     * @param newState
     */
    protected void onBatteryLevelChanged(float newState) {

    }
}
