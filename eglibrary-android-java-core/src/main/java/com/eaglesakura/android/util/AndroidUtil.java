package com.eaglesakura.android.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.TextureView;

import java.util.List;

public class AndroidUtil {

    /**
     * @return UIスレッドだったらtrue
     */
    public static boolean isUIThread() {
        return Thread.currentThread().equals(Looper.getMainLooper().getThread());
    }

    /**
     * UIスレッドでなければ例外を投げる。
     */
    public static void assertUIThread() {
        if (!isUIThread()) {
            throw new IllegalStateException("is not ui thread!!");
        }
    }

    public static void assertBackgroundThread() {
        if (isUIThread()) {
            throw new IllegalStateException("is not background thread!!");
        }
    }

    /**
     * Handlerに関連付けられていたThreadで動作している場合はtrueを返す。
     *
     * @param handler 確認対象のHandler
     *
     * @return Handlerに関連付けられていたThreadで動作している場合はtrueを返す。
     */
    public static boolean isHandlerThread(Handler handler) {
        return Thread.currentThread().equals(handler.getLooper().getThread());
    }

    public static boolean isSurfaceTexture(Object obj) {
        return Build.VERSION.SDK_INT >= 11 && obj instanceof SurfaceTexture;
    }

    public static boolean isTextureView(Object obj) {
        return Build.VERSION.SDK_INT >= 14 && obj instanceof TextureView;
    }

    public static boolean isSupportedSurfaceTexture() {
        return Build.VERSION.SDK_INT >= 11;
    }

    public static boolean isSupportedTextureView() {
        return Build.VERSION.SDK_INT >= 14;
    }

    /**
     * 自分自身がTop Applicationとして起動している場合はtrue
     *
     * @param context
     *
     * @return
     */
    public static boolean isTopApplicationSelf(Context context) {
        return context.getPackageName().equals(getTopApplicationPackage(context));
    }

    /**
     * ランチャー一覧を取得する
     *
     * @param context
     *
     * @return
     */
    public static List<ResolveInfo> listLauncherApplications(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return pm.queryIntentActivities(intent, 0);
    }

    /**
     * インストールされているアプリのpackage名一覧を取得する
     *
     * @param context
     *
     * @return
     */
    public static List<ApplicationInfo> listInstallApplications(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> infos = pm.getInstalledApplications(0);
        return infos;
    }

    /**
     * トップに起動しているActivityのpackage nameを指定する
     *
     * @param context
     *
     * @return
     */
    public static String getTopApplicationPackage(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (info.importanceReasonComponent != null) {
                    return info.importanceReasonComponent.getPackageName();
                } else {
                    return info.pkgList[0];
                }
            }
        }

        return context.getPackageName();
    }

    /**
     * 指定時間、端末のバイブを振動させる
     *
     * @param context app context
     * @param timeMs  震動時間(ミリ秒)
     */
    public static void vibrate(Context context, long timeMs) {
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(timeMs);
    }

    public static void playDefaultNotification(Context context) {
        playSound(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    /**
     * サウンドを一度だけ鳴らす
     *
     * @param context app context
     * @param uri     URI
     */
    public static void playSound(Context context, Uri uri) {
        final MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(context.getApplicationContext(), uri);
            player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.release();
                }
            });
            player.prepareAsync();
        } catch (Exception e) {
        }
    }

}
