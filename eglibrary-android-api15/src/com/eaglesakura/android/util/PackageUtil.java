package com.eaglesakura.android.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageUtil {

    /**
     * 指定したPermissionに対応していたらtrue
     * @param packageManager
     * @param info
     * @param permissionName
     * @return
     */
    public static boolean supportedPermission(PackageManager packageManager, PackageInfo info, String permissionName) {
        return packageManager.checkPermission(permissionName, info.packageName) == PackageManager.PERMISSION_GRANTED;
    }
}
