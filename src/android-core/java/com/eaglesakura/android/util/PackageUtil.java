package com.eaglesakura.android.util;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.eaglesakura.util.StringUtil;

public class PackageUtil {

    /**
     * 自分自身がTop Applicationとして起動している場合はtrue
     *
     * @param context
     * @return
     */
    public static boolean isTopApplicationSelf(Context context) {
        return context.getPackageName().equals(getTopApplicationPackage(context));
    }

    /**
     * ランチャー一覧を取得する
     *
     * @param context
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
     * @return
     */
    public static String getTopApplicationPackage(Context context) {

        if (Build.VERSION.SDK_INT >= 22) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            UsageEvents events = usm.queryEvents(time - (1000 * 60 * 60), time);
            if (events != null && events.hasNextEvent()) {
                UsageEvents.Event app = new android.app.usage.UsageEvents.Event();
                long lastAppTime = 0;
                String packageName = null;
                while (events.hasNextEvent()) {
                    events.getNextEvent(app);
                    if (app.getTimeStamp() > lastAppTime && app.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        packageName = app.getPackageName();
                        lastAppTime = app.getTimeStamp();
                    }
                }

                if (!StringUtil.isEmpty(packageName)) {
                    return packageName;
                }
            }
        } else {
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
        }

        return context.getPackageName();
    }


    /**
     * パッケージ固有の情報を特定ディレクトリにdumpする。
     * 既にディレクトリが存在していた場合の挙動は"cp -R"コマンドの挙動に従う。
     * "cp -R context.getFilesDir() dst" を行う
     *
     * @param context
     * @param dst
     */
    public static void dumpPackageDataDirectory(Context context, File dst) {
        try {
            File src = context.getFilesDir().getParentFile();
            dst.mkdirs();
            Runtime.getRuntime().exec(String.format("cp -R %s %s", src.getAbsolutePath(), dst.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
