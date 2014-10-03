package com.eaglesakura.dummybeaconadvertiser;

import android.app.Application;

import com.eaglesakura.android.framework.FrameworkCentral;

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
