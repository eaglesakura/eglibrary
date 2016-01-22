package com.eaglesakura.android.framework.util;

import com.eaglesakura.android.framework.ui.message.LocalMessageReceiver;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.util.Util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

/**
 * アプリ開発でよく用いるUtil
 */
public class AppSupportUtil {

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean requestRuntimePermissions(Activity activity, String[] permissions) {
        if (!PermissionUtil.isRuntimePermissionGranted(activity, permissions)) {
            activity.requestPermissions(permissions, LocalMessageReceiver.REQUEST_RUNTIMEPERMISSION_UPDATE);
            return true;
        } else {
            return false;
        }
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LocalMessageReceiver.REQUEST_RUNTIMEPERMISSION_UPDATE) {
            Intent intent = new Intent();
            intent.setAction(LocalMessageReceiver.ACTION_RUNTIMEPERMISSION_UPDATE);
            List<String> granted = new ArrayList<>();
            List<String> denied = new ArrayList<>();

            for (int i = 0; i < permissions.length; ++i) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permissions[i]);
                } else {
                    denied.add(permissions[i]);
                }
            }

            intent.putExtra(LocalMessageReceiver.EXTRA_RUNTIMEPERMISSION_GRANTED_LIST, Util.convert(granted, new String[granted.size()]));
            intent.putExtra(LocalMessageReceiver.EXTRA_RUNTIMEPERMISSION_DENIED_LIST, Util.convert(denied, new String[denied.size()]));

            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }
}
