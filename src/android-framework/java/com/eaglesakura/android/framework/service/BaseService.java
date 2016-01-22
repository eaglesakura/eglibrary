package com.eaglesakura.android.framework.service;

import com.eaglesakura.android.thread.ui.UIHandler;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

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

    /**
     * CPU稼働保証を行うカウント
     */
    int wakeUpRef;

    AlarmManager alarmManager;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    boolean destroyed = false;

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        this.ACTION_SELF_WAKEUP_BROADCAST = getPackageName() + "/" + getClass().getName() + ".ACTION_SELF_WAKEUP_BROADCAST" + "/" + hashCode();
        registerReceiver(wakeupBroadcastReceiver, new IntentFilter(ACTION_SELF_WAKEUP_BROADCAST));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            // 強制的にCPU稼働を停止させる
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
                wakeUpRef = 0;
            }
        }
        unregisterReceiver(wakeupBroadcastReceiver);
        destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * CPU稼働参照を減らす
     */
    public void popCpuWakeup() {
        synchronized (this) {
            if (wakeUpRef <= 0) {
                // 強制停止がかかっている場合があるため、参照カウント0の場合は何もしない
                return;
            }

            --wakeUpRef;
            if (wakeUpRef == 0) {
                wakeLock.release();
                wakeLock = null;
            }
        }
    }

    /**
     * CPU稼働参照を増やす
     */
    public void pushCpuWakeup() {
        synchronized (this) {
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
                wakeLock.acquire();
            }
            ++wakeUpRef;
        }
    }

    /**
     * 指定したミリ秒後、再度CPUを叩き起こす。
     * <br>
     * 繰り返しには対応しない。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayTimeMs     遅延時間
     */
    protected void requestNextAlarmDelayed(int requestCode, Bundle requestArgments, long delayTimeMs) {
        requestNextAlarmDelayed(requestCode, requestArgments, delayTimeMs, true);
    }

    /**
     * 指定したミリ秒後、再度CPUを叩き起こす。
     * <br>
     * 繰り返しには対応しない。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayTimeMs     遅延時間
     * @param extract         時間保証を有効にする場合はtrue
     */
    @SuppressLint("NewApi")
    protected void requestNextAlarmDelayed(int requestCode, Bundle requestArgments, long delayTimeMs, boolean extract) {
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

        if (extract && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, current + delayTimeMs, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, current + delayTimeMs, pendingIntent);
        }
    }

    /**
     * セットしてあるアラームを解除する
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
     * <br>
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

    protected void toast(final String fmt, final Object... args) {
        UIHandler.postUIorRun(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseService.this, String.format(fmt, args), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
