package com.eaglesakura.gradle.plugins

import com.eaglesakura.gradle.tasks.AndroidCiCollectTask
import com.eaglesakura.gradle.tasks.AndroidLocalPropertiesGenTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Androidアプリ開発用のサポートタスク
 */
public class AndroidSupportPlugin implements Plugin<Project> {


    void apply(Project target) {
        println("AndroidSupportPlugin apply!")

        target.extensions.create("eglibrary", ExtensionEglibrary);

        CI_SETUP:
        {
            // build number
            def BUILD_VERSION_CODE = System.getenv("BUILD_NUMBER");
            def BUILD_VERSION_NAME = "1.0.${BUILD_VERSION_CODE}.jks";
            def BUILD_DATE = System.getenv("BUILD_ID");

            // Jenkins以外から実行されている場合、適当な設定を行う
            if (BUILD_VERSION_CODE == null) {
                println("not jenkins");
                target.eglibrary.ci.ciRunning = false;
                target.eglibrary.ci.buildVersionCode = "1";
                target.eglibrary.ci.buildDate = new Date().toLocaleString();
                target.eglibrary.ci.buildVersionName = "build.${System.getenv("USER")}.${target.eglibrary.ci.buildDate}";
            } else {
                target.eglibrary.ci.ciRunning = true;
                target.eglibrary.ci.buildVersionCode = BUILD_VERSION_CODE;
                target.eglibrary.ci.buildDate = BUILD_DATE;
                target.eglibrary.ci.buildVersionName = BUILD_VERSION_NAME;
            }
        }

        println("target.eglibrary.ci.releaseDir(${target.eglibrary.ci.releaseDir})")
        println("target.eglibrary.ci.ciRunning(${target.eglibrary.ci.ciRunning})")
        println("target.eglibrary.ci.buildVersionCode(${target.eglibrary.ci.buildVersionCode})")
        println("target.eglibrary.ci.buildDate(${target.eglibrary.ci.buildDate})")
        println("target.eglibrary.ci.buildVersionName(${target.eglibrary.ci.buildVersionName})")

        target.task('genLocalProperties', type: AndroidLocalPropertiesGenTask)
        target.task('ciCollect', type: AndroidCiCollectTask)
    }

    /**
     * eglibraryのデフォルト設定
     */
    public static class ExtensionEglibrary {
        def ci = new ExtensionCi();
    }

    /**
     * Jenkins等のCI環境を構築する
     */
    public static class ExtensionCi {
        File releaseDir = new File("ci-release").absoluteFile;

        /**
         * CI上で実行されていればtrue
         *
         * それ以外ならばfalseとなる
         */
        boolean ciRunning = true;

        /**
         * ビルド用のバージョンコード
         */
        String buildVersionCode;

        /**
         * ビルド日時
         */
        String buildDate;

        /**
         * バージョン名
         */
        String buildVersionName;
    }
}