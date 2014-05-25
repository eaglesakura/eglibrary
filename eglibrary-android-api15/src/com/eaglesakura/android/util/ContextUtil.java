package com.eaglesakura.android.util;

import java.util.UUID;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.WindowManager;

import com.eaglesakura.math.Vector2;
import com.eaglesakura.util.Util;

/**
 * Context関連の便利メソッドを提供する
 * 
 * @author TAKESHI YAMASHITA
 *
 */
public class ContextUtil {
    /**
     *
     * 
     * @param context
     * @return
     */
    public static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    /**
     *
     * 
     * @param context
     * @return
     */
    public static boolean isOrientationVertical(Context context) {
        return getOrientation(context) == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     *
     * 
     * @param context
     * @return
     */
    public static boolean isOrientationAuto(Context context) {
        return getOrientation(context) == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    /**
     * ディスプレイのXYサイズを取得する。
     * 
     * 
     * @param context
     * @param result
     * @return
     * 
     */
    public static Vector2 getDisplaySize(Context context, Vector2 result) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int displayW = wm.getDefaultDisplay().getWidth(), displayH = wm.getDefaultDisplay().getHeight();
        result.set((float) displayW, (float) displayH);
        return result;
    }

    /**
     * 指定方向に端末画面を固定する。
     * 
     * 
     * @param context
     * @param isVertical
     * 
     */
    public static void setOrientation(Context context, boolean isVertical) {
        try {
            Activity activity = (Activity) context;
            if (isVertical) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (ClassCastException cce) {
            return;
        }
    }

    /**
     * 設定を反転する。
     * 
     * 
     * @param context
     * 
     */
    public static void toggleOrientationFixed(Context context) {
        try {
            Activity activity = (Activity) context;
            int ori = context.getResources().getConfiguration().orientation;
            if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (ClassCastException cce) {
            return;
        }
    }

    /**
     * 
     * 
     * @param context
     * @param is
     * 
     */
    public static void setOrientationFixed(Context context, boolean is) {
        try {
            Activity activity = (Activity) context;
            if (is) {
                int ori = context.getResources().getConfiguration().orientation;
                if (ori == Configuration.ORIENTATION_LANDSCAPE) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            } else {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        } catch (ClassCastException cce) {
            return;
        }
    }

    /**
     * DP->Pixを変換して返す。
     * 
     * @param dp
     * @param context
     * @return 画面上のピクセル数。ただし、1ピクセル単位に四捨五入されている。
     */
    public static int dpToPix(float dp, Context context) {
        float tmpDensity = context.getResources().getDisplayMetrics().density;
        // ピクセル値を求める
        int tmpPx = (int) (dp * tmpDensity + 0.5f);
        return tmpPx;
    }

    /**
     * デバッグモードならtrueを返す。
     * 
     * @return
     */
    public static boolean isDebug(Context context) {
        PackageManager manager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
            return true;
        }
        return false;
    }

    /**
     * ステータスバーに簡単なメッセージを表示する。
     * 
     * @param message
     * @param icon
     * @param statusbarId
     * @param intent
     */
    public static void sendStatusBarInfo(Context context, String title, String message, int icon, int statusbarId,
            Intent intent) {
        try {
            //! 通知作成
            final NotificationManager nfManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            {
                Notification notification = new Notification(icon, message, System.currentTimeMillis());
                notification.setLatestEventInfo(context, title, message,
                        PendingIntent.getBroadcast(context, 0, intent, 0));
                nfManager.notify(statusbarId, notification);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Actionバーに対応している場合、trueを返す。
     * @param context
     * @return
     */
    public static boolean isActionBarEnable() {
        try {
            return Build.VERSION.SDK_INT >= 11;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 文字幅を指定した幅に収まるように抑えて取得する。
     * @param text
     * @param fooder
     * @param context
     * @param textSizeDimenId
     * @param textWidthDimenId
     * @return
     */
    public static String getCompactString(String text, String fooder, Context context, int textSizeDimenId,
            int textWidthDimenId) {
        int textPixelSize = context.getResources().getDimensionPixelSize(textSizeDimenId);
        int textWidthPixelSize = context.getResources().getDimensionPixelSize(textWidthDimenId);

        return getCompactString(text, fooder, Typeface.DEFAULT, textPixelSize, textWidthPixelSize);
    }

    /**
     * 文字列を指定した幅に収まるように抑えて取得する。
     * @param origin
     * @return
     */
    public static String getCompactString(String origin, String fooder, Typeface type, int textSize, int maxWidth) {
        Paint paint = new Paint();
        paint.setTypeface(type);
        paint.setTextSize(textSize);
        Rect area = new Rect();

        paint.getTextBounds(origin, 0, origin.length(), area);

        //! 通常状態で指定フォント幅よりも狭いから大丈夫
        if (area.width() <= maxWidth) {
            return origin;
        }

        paint.getTextBounds(fooder, 0, fooder.length(), area);
        //! ケツにつける"..."の幅を事前計算
        final int FOODER_WIDTH = area.width();

        int length = origin.length();
        do {
            --length;
            paint.getTextBounds(origin, 0, length, area);
        } while (area.width() > (maxWidth - FOODER_WIDTH));

        origin = origin.substring(0, length);
        origin += "...";
        return origin;
    }

    public static boolean isAndroid3_xOrLater() {
        return Build.VERSION.SDK_INT >= 12;
    }

    /**
     * おそらく重複することが無いであろうユニークな文字列を吐き出す。
     * @return
     */
    public static String genUUID() {
        String result = null;
        result = String.format("%s-%s-%s",
        // 通常のUUID
                UUID.randomUUID().toString(),
                // 現在時刻
                Util.genSHA1(Long.valueOf(System.currentTimeMillis()).toString().getBytes()),
                // 端末起動からの経過時間
                Util.genSHA1(Long.valueOf(SystemClock.elapsedRealtime()).toString().getBytes()));
        return result;
    }

    /**
     * Handlerに関連付けられていたThreadで動作している場合はtrueを返す。
     * @param handler
     * @return
     */
    public static boolean isHandlerThread(Handler handler) {
        return Thread.currentThread().equals(handler.getLooper().getThread());
    }
}
