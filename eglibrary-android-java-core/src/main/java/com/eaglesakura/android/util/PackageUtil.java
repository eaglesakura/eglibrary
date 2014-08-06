package com.eaglesakura.android.util;

import java.io.File;

import android.content.Context;
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

    /**
     * パッケージ固有の情報を特定ディレクトリにdumpする。
     * 既にディレクトリが存在していた場合の挙動は"cp -R"コマンドの挙動に従う。
     * "cp -R context.getFilesDir() dst" を行う
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
