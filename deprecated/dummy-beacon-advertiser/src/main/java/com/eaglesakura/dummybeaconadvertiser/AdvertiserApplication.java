package com.eaglesakura.dummybeaconadvertiser;

import com.eaglesakura.android.framework.FrameworkCentral;

import android.app.Application;

public class AdvertiserApplication extends Application implements FrameworkCentral.FrameworkApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FrameworkCentral.onApplicationCreate(this);
    }

    @Override
    public void onApplicationUpdated(int oldVersionCode, int newVersionCode, String oldVersionName, String newVersionName) {

    }
}
