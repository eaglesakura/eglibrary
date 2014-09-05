package com.eaglesakura.android.framework;

import android.app.Application;

import com.eaglesakura.android.framework.db.BasicSettings;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;

/**
 *
 */
public class FrameworkCentral {
    private static Application application;
    private static FrameworkApplication frameworkApplication;

    /**
     * 基本設定
     */
    private static BasicSettings settings;

    /**
     * Application#onCreateで呼び出す
     *
     * @param application
     */
    public static void onApplicationCreate(Application application) {
        FrameworkCentral.application = application;

        if (application instanceof FrameworkApplication) {
            FrameworkCentral.frameworkApplication = (FrameworkApplication) application;
        }

        settings = new BasicSettings(application);
        // 設定を読み出す
        {
            final String oldVersionName = settings.getLastBootedAppVersionName();
            final String versionName = ContextUtil.getVersionName(application);

            final int oldVersionCode = (int) settings.getLastBootedAppVersionCode();
            final int versionCode = ContextUtil.getVersionCode(application);

            LogUtil.log("VersionCode [%d] -> [%d]", oldVersionCode, versionCode);
            LogUtil.log("VersionName [%s] -> [%s]", oldVersionName, versionName);

            settings.setLastBootedAppVersionCode(versionCode);
            settings.setLastBootedAppVersionName(versionName);

            // バージョンコードかバージョン名が変わったら通知を行う
            if (frameworkApplication != null && ((versionCode != oldVersionCode) || (!oldVersionName.equals(versionName)))) {
                frameworkApplication.onApplicationUpdated(oldVersionCode, versionCode, oldVersionName, versionName);
            }
        }

        // 設定をコミットする
        settings.commitAsync();
    }

    /**
     * Frameworkの設定クラスを取得する
     *
     * @return
     */
    public static BasicSettings getSettings() {
        return settings;
    }

    public static Application getApplication() {
        return application;
    }

    /**
     * impl Application
     */
    public interface FrameworkApplication {
        /**
         * Applicationが更新された際に呼び出される
         *
         * @param oldVersionCode 前回のバージョンコード
         * @param newVersionCode アップデート後のバージョンコード
         * @param oldVersionName 前回のバージョン名
         * @param newVersionName アップデート後のバージョン名
         */
        void onApplicationUpdated(int oldVersionCode, int newVersionCode, String oldVersionName, String newVersionName);
    }
}
