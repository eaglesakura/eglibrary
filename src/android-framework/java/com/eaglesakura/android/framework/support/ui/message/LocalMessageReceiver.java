package com.eaglesakura.android.framework.support.ui.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

public class LocalMessageReceiver extends BroadcastReceiver {
    public static final String ACTION_RUNTIMEPERMISSION_UPDATE = "ACTION_RUNTIMEPERMISSION_UPDATE";

    public static final String RUNTIMEPERMISSION_GRANTED_LIST = "RUNTIMEPERMISSION_GRANTED_LIST";

    public static final String RUNTIMEPERMISSION_DENIED_LIST = "RUNTIMEPERMISSION_DENIED_LIST";

    final LocalBroadcastManager localBroadcastManager;

    public LocalMessageReceiver(Context context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    final public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (StringUtil.isEmpty(action)) {
            return;
        }

        if (ACTION_RUNTIMEPERMISSION_UPDATE.equals(action)) {
            String[] granted = intent.getStringArrayExtra(RUNTIMEPERMISSION_GRANTED_LIST);
            String[] denied = intent.getStringArrayExtra(RUNTIMEPERMISSION_DENIED_LIST);

            if (granted == null) {
                granted = new String[0];
            }
            if (denied == null) {
                denied = new String[0];
            }

            for (String g : granted) {
                LogUtil.log("RuntimePermission Granted / " + g);
            }

            for (String d : denied) {
                LogUtil.log("RuntimePermission Denied / " + d);
            }

            onRuntimePermissionUpdated(granted, denied);
        }
    }

    public void connect() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RUNTIMEPERMISSION_UPDATE);
        localBroadcastManager.registerReceiver(this, filter);
    }

    public void disconnect() {
        localBroadcastManager.unregisterReceiver(this);
    }

    protected void onRuntimePermissionUpdated(String[] granted, String[] denied) {
    }
}
