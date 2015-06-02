package com.eaglesakura.android.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.eaglesakura.android.util.ContextUtil;

/**
 * スクリーンOn/Offの検出を行うUtil
 */
public class ScreenEventReceiver extends BroadcastReceiver {

    private final Context context;

    /**
     * スクリーンONであればtrue
     */
    private boolean screenPowerOn;

    /**
     * @param context
     */
    public ScreenEventReceiver(Context context) {
        this.context = context.getApplicationContext();
        syncScreenMode();
    }

    /**
     * イベント受け取り
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            screenPowerOn = true;
            onScreenPowerOn();
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            screenPowerOn = false;
            onScreenPowerOff();
        }
    }

    /**
     * スクリーンONの状態であればtrue
     *
     * @return
     */
    public boolean isScreenPowerOn() {
        return screenPowerOn;
    }

    public void syncScreenMode() {
        this.screenPowerOn = ContextUtil.isScreenPowerOn(context);
    }

    /**
     * ブロードキャストレシーバを接続する
     */
    public void connect() {
        IntentFilter filter = new IntentFilter();

        // スクリーンON/OFFを検出する
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);

        // 接続する
        context.registerReceiver(this, filter);
    }

    /**
     * ブロードキャストレシーバから切断する
     */
    public void disconnect() {
        context.unregisterReceiver(this);
    }

    /**
     * スクリーン表示がONになった
     */
    protected void onScreenPowerOn() {
    }

    /**
     * スクリーン表示がOFFになった
     */
    protected void onScreenPowerOff() {
    }
}
