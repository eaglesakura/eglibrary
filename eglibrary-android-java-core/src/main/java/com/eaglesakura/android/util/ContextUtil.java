package com.eaglesakura.android.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.eaglesakura.math.Vector2;
import com.eaglesakura.util.EncodeUtil;

import java.util.List;
import java.util.UUID;

/**
 * Context関連の便利メソッドを提供する
 */
public class ContextUtil {

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    /**
     * ディスプレイの回転角を取得する
     *
     * @param context
     * @return
     */
    public static int getDeviceRotateDegree(Context context) {
        final int surfaceRotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (surfaceRotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    /**
     * @param context
     * @return
     */
    public static int getOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isOrientationVertical(Context context) {
        return getOrientation(context) == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isOrientationAuto(Context context) {
        return getOrientation(context) == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    /**
     * ディスプレイのWHサイズを取得する。
     *
     * @param context
     * @param result
     * @return
     */
    public static Vector2 getDisplaySize(Context context, Vector2 result) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        @SuppressWarnings("deprecation")
        int displayW = wm.getDefaultDisplay().getWidth(), displayH = wm.getDefaultDisplay().getHeight();
        result.set((float) displayW, (float) displayH);
        return result;
    }

    /**
     * ディスプレイのWHサイズを取得する
     *
     * @param context
     * @return
     */
    public static int[] getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return new int[]{display.getWidth(), display.getHeight()};
    }

    /**
     * 指定方向に端末画面を固定する。
     *
     * @param context
     * @param isVertical
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
     * @param context
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
     * @param context
     * @param is
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
    @SuppressWarnings("deprecation")
    public static void sendStatusBarInfo(Context context, String title, String message, int icon, int statusbarId, Intent intent) {
        try {
            //! 通知作成
            final NotificationManager nfManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            {
                Notification notification = new Notification(icon, message, System.currentTimeMillis());
                notification.setLatestEventInfo(context, title, message, PendingIntent.getBroadcast(context, 0, intent, 0));
                nfManager.notify(statusbarId, notification);
            }
        } catch (Exception e) {
        }
    }

    /**
     * ChildFragmentManagerに対応している場合はtrue
     *
     * @return
     */
    public static boolean supportedChildFragmentManager() {
        return Build.VERSION.SDK_INT >= 17;
    }

    /**
     * Actionバーに対応している場合、trueを返す。
     *
     * @return
     */
    public static boolean supportedActionBar() {
        try {
            return Build.VERSION.SDK_INT >= 11;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 戻るキーイベントであればtrue
     *
     * @param event dispatchに渡されたイベント
     * @return 戻るキーならばtrue
     */
    public static boolean isBackKeyEvent(KeyEvent event) {
        return event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK;
    }

    /**
     * 文字幅を指定した幅に収まるように抑えて取得する。
     *
     * @param text
     * @param fooder
     * @param context
     * @param textSizeDimenId
     * @param textWidthDimenId
     * @return
     */
    public static String getCompactString(String text, String fooder, Context context, int textSizeDimenId, int textWidthDimenId) {
        int textPixelSize = context.getResources().getDimensionPixelSize(textSizeDimenId);
        int textWidthPixelSize = context.getResources().getDimensionPixelSize(textWidthDimenId);

        return getCompactString(text, fooder, Typeface.DEFAULT, textPixelSize, textWidthPixelSize);
    }

    /**
     * 文字列を指定した幅に収まるように抑えて取得する。
     *
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
     *
     * @return
     */
    public static String genUUID() {
        String result = null;
        result = String.format("%s-%s-%s",
                // 通常のUUID
                UUID.randomUUID().toString(),
                // 現在時刻
                EncodeUtil.genSHA1(Long.valueOf(System.currentTimeMillis()).toString().getBytes()),
                // 端末起動からの経過時間
                EncodeUtil.genSHA1(Long.valueOf(SystemClock.elapsedRealtime()).toString().getBytes()));
        return result;
    }

    /**
     * 画面が点灯状態だったらtrueを返す
     *
     * @param context
     * @return
     */
    public static boolean isScreenPowerOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    /**
     * IMEを閉じる
     *
     * @param context 対象のActivity
     */
    public static void closeIME(View focus, Activity context) {
        try {
            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focus.getWindowToken(), 0);

        } catch (Exception e) {

        }
    }

    /**
     * 指定クラスが起動中であればtrueを返す
     *
     * @param context
     * @param clazz
     * @return
     */
    public static boolean isServiceRunning(Context context, Class<? extends Service> clazz) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

            for (ActivityManager.RunningServiceInfo info : services) {
                if (clazz.getName().equals(info.service.getClassName())) {
                    // 一致するクラスが見つかった
                    return true;
                }
            }
            return false;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * string xmlリソース名から文字列リソースを取得する
     *
     * @param context
     * @param resName
     * @return
     */
    public static String getStringFromIdName(Context context, String resName) {
        try {
            int target_string_id = context.getResources().getIdentifier(
                    resName,
                    "string",
                    context.getPackageName()
            );

            return context.getResources().getString(target_string_id);
        } catch (Exception e) {
            return null;
        }
    }
}
