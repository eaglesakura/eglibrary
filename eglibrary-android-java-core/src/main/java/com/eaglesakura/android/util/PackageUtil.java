package com.eaglesakura.android.util;

import java.io.File;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

public class PackageUtil {

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
