package com.eaglesakura.android.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

public class PermissionUtil {

    public enum PermissionType {
        SelfLocation {
            @Override
            public String[] getPermissions() {
                return new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                };
            }
        },

        UsageStatus {
            @Override
            public String[] getPermissions() {
                return new String[]{
                        Manifest.permission.PACKAGE_USAGE_STATS,
                };
            }
        },

        GoogleMap {
            @Override
            public String[] getPermissions() {
                return new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                };
            }
        };

        public abstract String[] getPermissions();
    }

    public static boolean isSupportRuntimePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isRuntimePermissionGranted(Context context, PermissionType type) {
        return isRuntimePermissionGranted(context, type.getPermissions());
    }

    public static boolean isRuntimePermissionGranted(Context context, String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Runtime Permission非対応なので常にtrue
            return true;
        }

        for (String permission : permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 指定したPermissionに対応していたらtrue
     *
     * @param packageManager
     * @param info
     * @param permissionName
     * @return
     */
    public static boolean supportedPermission(PackageManager packageManager, PackageInfo info, String permissionName) {
        return packageManager.checkPermission(permissionName, info.packageName) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * システムレイヤーへのオーバーレイが許可されている場合はtrue
     *
     * @param context
     * @return
     */
    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return Settings.canDrawOverlays(context);
    }

    @SuppressLint("NewApi")
    public static boolean isUsageStatsAllowed(Context context) {
        if (Build.VERSION.SDK_INT <= 19) {
            return true;
        }

        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int uid = android.os.Process.myUid();
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

}
