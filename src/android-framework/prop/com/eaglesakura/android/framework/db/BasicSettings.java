package com.eaglesakura.android.framework.db;

import com.eaglesakura.android.db.BasePropertiesDatabase;

import android.content.Context;

public class BasicSettings extends BasePropertiesDatabase {
    public BasicSettings(Context context) {
        super(context, "appfw.db");
        _initialize();
    }

    public BasicSettings(Context context, String dbFileName) {
        super(context, dbFileName);
        _initialize();
    }

    protected void _initialize() {

        addProperty("BasicSettings.gcmToken", "");
        addProperty("BasicSettings.lastBootedAppVersionCode", "0");
        addProperty("BasicSettings.lastBootedAppVersionName", "");
        addProperty("BasicSettings.loginGoogleClientApi", "false");
        addProperty("BasicSettings.loginGoogleAccount", "");

        load();

    }

    public void setGcmToken(String set) {
        setProperty("BasicSettings.gcmToken", set);
    }

    public String getGcmToken() {
        return getStringProperty("BasicSettings.gcmToken");
    }

    public void setLastBootedAppVersionCode(int set) {
        setProperty("BasicSettings.lastBootedAppVersionCode", set);
    }

    public int getLastBootedAppVersionCode() {
        return getIntProperty("BasicSettings.lastBootedAppVersionCode");
    }

    public void setLastBootedAppVersionName(String set) {
        setProperty("BasicSettings.lastBootedAppVersionName", set);
    }

    public String getLastBootedAppVersionName() {
        return getStringProperty("BasicSettings.lastBootedAppVersionName");
    }

    public void setLoginGoogleClientApi(boolean set) {
        setProperty("BasicSettings.loginGoogleClientApi", set);
    }

    public boolean getLoginGoogleClientApi() {
        return getBooleanProperty("BasicSettings.loginGoogleClientApi");
    }

    public void setLoginGoogleAccount(String set) {
        setProperty("BasicSettings.loginGoogleAccount", set);
    }

    public String getLoginGoogleAccount() {
        return getStringProperty("BasicSettings.loginGoogleAccount");
    }

}
