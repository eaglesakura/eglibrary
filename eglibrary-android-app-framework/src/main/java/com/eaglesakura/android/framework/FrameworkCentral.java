package com.eaglesakura.android.framework;

import android.app.Application;

import com.eaglesakura.android.framework.db.BasicSettings;

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

        // 設定を読み出す
        settings = new BasicSettings(application);
    }

    /**
     * Application#onTerminateで呼び出す
     *
     * @param application
     */
    public static void onApplicationTerminate(Application application) {
        // 必要な項目を保存する
        settings.commit();
    }

    /**
     * Frameworkの設定クラスを取得する
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
