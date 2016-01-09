package com.eaglesakura.android.util;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.TextureView;

public class AndroidUtil {

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

    public static boolean isSupportedTransitionAnimation() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
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

    /**
     * 実行されている端末がデバッグモードになっている場合はtrueを返す
     *
     * @param context
     * @return
     */
    public static boolean isDeviceDebugMode(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0;
    }

}
