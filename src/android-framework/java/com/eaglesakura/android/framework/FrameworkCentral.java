package com.eaglesakura.android.framework;

import com.eaglesakura.android.framework.db.BasicSettings;
import com.eaglesakura.android.thread.async.AsyncTaskController;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;

import android.app.Application;

import java.lang.reflect.Method;

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

    private static AsyncTaskController taskController = new AsyncTaskController(5, 1000);

    /**
     * Application#onCreateで呼び出す
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

//    /**
//     * GCMトークンを登録する
//     */
//    public static void registerGcm() throws IOException {
//        if (!StringUtil.isEmpty(getSettings().getGcmToken())) {
//            return;
//        }
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplication());
//        String registerId = gcm.register(getApplication().getString(R.string.eglibrary_Gcm_SenderID));
//
//        if (StringUtil.isEmpty(registerId)) {
//            throw new IllegalStateException("GCM register id == null");
//        }
//        getSettings().setGcmToken(registerId);
//        getSettings().commit();
//    }

//    /**
//     * GCMトークンを無効化する
//     *
//     * @throws IOException
//     */
//    public static void unregisterGcm() throws IOException {
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplication());
//        gcm.unregister();
//        getSettings().setGcmToken("");
//        getSettings().commit();
//    }

    /**
     * Frameworkの設定クラスを取得する
     */
    public static BasicSettings getSettings() {
        return settings;
    }

    public static Application getApplication() {
        return application;
    }

    /**
     * Deploygateのインストールを行う。
     * <br>
     * dependenciesが設定されていない場合、このメソッドはfalseを返す
     * <br>
     * debugCompile 'com.deploygate:sdk:3.1'
     *
     * @return 成功したらtrue
     */
    public static boolean requestDeploygateInstall() {
        try {
            Class<?> clazz = Class.forName("com.deploygate.sdk.DeployGate");
            Method installMethod = clazz.getMethod("install", Application.class);

            installMethod.invoke(clazz, getApplication());
            LogUtil.log("install success Deploygate");
            return true;
        } catch (Exception e) {
            LogUtil.log("not dependencies Deploygate");
            return false;
        }
    }

    public static AsyncTaskController getTaskController() {
        return taskController;
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
