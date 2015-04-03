package com.eaglesakura.android.framework.support.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

/**
 * 便利系メソッドを固めたUtilクラス
 */
public abstract class BaseService extends Service {

    /**
     * 呼び出しリクエストを指定する
     */
    private static final String EXTRA_WAKEUP_REQUEST_CODE = "EXTRA_WAKEUP_REQUEST_CODE";

    /**
     * 呼び出しリクエストの引数を指定する
     */
    private static final String EXTRA_WAKEUP_REQUEST_ARGMENTS = "EXTRA_WAKEUP_REQUEST_ARGMENTS";

    /**
     * アラームを作成した時刻
     */
    private static final String EXTRA_WAKEUP_REQUEST_ALARM_TIME = "EXTRA_WAKEUP_REQUEST_ALARM_TIME";

    private String ACTION_SELF_WAKEUP_BROADCAST;

    boolean nonSleepService;

    AlarmManager alarmManager;

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    boolean destroyed = false;

    @Override
    public void onCreate() {
        super.onCreate();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        this.ACTION_SELF_WAKEUP_BROADCAST = getPackageName() + "/" + getClass().getName() + ".ACTION_SELF_WAKEUP_BROADCAST";
        registerReceiver(wakeupBroadcastReceiver, new IntentFilter(ACTION_SELF_WAKEUP_BROADCAST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopNonSleep();
        unregisterReceiver(wakeupBroadcastReceiver);
        destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Serviceの休止を許可する
     */
    public void stopNonSleep() {
        if (nonSleepService) {
            // Serviceの休止を停止させる
            nonSleepService = false;

            wakeLock.release();
            wakeLock = null;
        }
    }

    /**
     * Serviceの休止を許さなくする
     */
    public void requestNonSleep() {
        if (nonSleepService) {
            return;
        }

        nonSleepService = true;
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
        wakeLock.acquire();
    }

    /**
     * 指定したミリ秒後、再度CPUを叩き起こす。
     * <p/>
     * 繰り返しには対応しない。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayTimeMs     遅延時間
     */
    protected void requestNextAlarmDelayed(int requestCode, Bundle requestArgments, long delayTimeMs) {
        Intent intent = new Intent(ACTION_SELF_WAKEUP_BROADCAST);
        intent.putExtra(EXTRA_WAKEUP_REQUEST_CODE, requestCode);
        if (requestArgments != null) {
            intent.putExtra(EXTRA_WAKEUP_REQUEST_ARGMENTS, requestArgments);
        }
        final long current = SystemClock.elapsedRealtime();
        intent.putExtra(EXTRA_WAKEUP_REQUEST_ALARM_TIME, current);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, current + delayTimeMs, pendingIntent);
    }

    /**
     * セットしてあるアラームを解除する
     *
     * @param requestCode
     */
    protected void cancelAlarm(int requestCode) {
        Intent intent = new Intent(ACTION_SELF_WAKEUP_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Systemのアラームに対応する
     */
    private final BroadcastReceiver wakeupBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int requestCode = intent.getIntExtra(EXTRA_WAKEUP_REQUEST_CODE, 0);
            Bundle argments = intent.getBundleExtra(EXTRA_WAKEUP_REQUEST_ARGMENTS);
            long alarmTime = intent.getLongExtra(EXTRA_WAKEUP_REQUEST_ALARM_TIME, 0);


            final long current = SystemClock.elapsedRealtime();
            onAlarmReceived(requestCode, argments, current - alarmTime);
        }
    };

    /**
     * AlarmManagerによってCPUが叩き起こされたタイミングで呼び出される
     * <p/>
     * このメソッドは必ずonReceiveの中で呼び出されることを保証する。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayedTimeMs   設定から実際に遅延した時間
     */
    protected void onAlarmReceived(int requestCode, Bundle requestArgments, long delayedTimeMs) {
    }

    protected void log(String fmt, Object... args) {
        Log.i(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }

    protected void logi(String fmt, Object... args) {
        Log.i(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }

    protected void logd(String fmt, Object... args) {
        Log.d(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }
}
