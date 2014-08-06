package com.eaglesakura.android.framework.db;

import android.content.Context;

public class BasicSettings extends com.eaglesakura.android.db.BasePropertiesDatabase {
    public BasicSettings(Context context){ super(context, "appfw.db"); _initialize(); }
    public BasicSettings(Context context, String dbFileName){ super(context, dbFileName); _initialize(); }
    protected void _initialize() {
            
        addProperty("BasicSettings.gcmToken", "");
        addProperty("BasicSettings.lastBootedAppVersionCode", "0");
        addProperty("BasicSettings.lastBootedAppVersionName", "");
        
        load();
        
    }
    public void setGcmToken(String set){ setProperty("BasicSettings.gcmToken", set); }
    public String getGcmToken(){ return getStringProperty("BasicSettings.gcmToken"); }
    public void setLastBootedAppVersionCode(long set){ setProperty("BasicSettings.lastBootedAppVersionCode", set); }
    public long getLastBootedAppVersionCode(){ return getLongProperty("BasicSettings.lastBootedAppVersionCode"); }
    public void setLastBootedAppVersionName(String set){ setProperty("BasicSettings.lastBootedAppVersionName", set); }
    public String getLastBootedAppVersionName(){ return getStringProperty("BasicSettings.lastBootedAppVersionName"); }
    
}
