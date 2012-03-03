package com.eaglesakura.lib.android.game.util;

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
import android.os.Build;
import android.view.WindowManager;

import com.eaglesakura.lib.android.game.math.Vector2;

public class ContextUtil {

    /**
     *
     * @author eagle.sakura
     * @param context
     * @return
     */
    public static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    /**
     *
     * @author eagle.sakura
     * @param context
     * @return
     */
    public static boolean isOrientationVertical(Context context) {
        return getOrientation(context) == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     *
     * @author eagle.sakura
     * @param context
     * @return
     */
    public static boolean isOrientationAuto(Context context) {
        return getOrientation(context) == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    /**
     * ディスプレイのXYサイズを取得する。
     * 
     * @author eagle.sakura
     * @param context
     * @param result
     * @return
     * @version 2010/07/14 : 新規作成
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
     * @author eagle.sakura
     * @param context
     * @param isVertical
     * @version 2010/06/13 : 新規作成
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
     * @author eagle.sakura
     * @param context
     * @version 2010/06/16 : 新規作成
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
     * @author eagle.sakura
     * @param context
     * @param is
     * @version 2010/05/31 : 新規作成
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
    public static boolean isActionBarEnable(Context context) {
        try {
            return Build.VERSION.SDK_INT >= 11;
        } catch (Exception e) {
            return false;
        }
    }
}
