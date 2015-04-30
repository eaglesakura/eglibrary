package com.eaglesakura.gradle.plugins

import com.eaglesakura.gradle.tasks.AndroidCiCleanTask
import com.eaglesakura.gradle.tasks.AndroidCiCollectTask
import com.eaglesakura.gradle.tasks.AndroidLocalPropertiesGenTask
import com.eaglesakura.util.StringUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Androidアプリ開発用のサポートタスク
 */
public class AndroidSupportPlugin implements Plugin<Project> {


    void apply(Project target) {
//        println("AndroidSupportPlugin apply!")
        target.extensions.create("eglibrary", ExtensionEglibrary);

        CI_SETUP:
        {
            // Jenkins以外から実行されている場合、適当な設定を行う
            if (!StringUtil.isEmpty(System.getenv("BUILD_NUMBER")) {
                println("Build Jenkins");
                target.eglibrary.ci.ciRunning = true;
                target.eglibrary.ci.buildVersionCode = System.getenv("BUILD_NUMBER");
                target.eglibrary.ci.buildDate = System.getenv("BUILD_ID");
                target.eglibrary.ci.buildVersionName = "ci.${target.eglibrary.ci.buildVersionCode}";
            } else if (!StringUtil.isEmpty(StringUtil.isEmpty(System.getenv("BUILD_NUMBER"))) {
                println("Build CircleCI");
                target.eglibrary.ci.ciRunning = true;
                target.eglibrary.ci.buildVersionCode = System.getenv("CIRCLE_BUILD_NUM");
                target.eglibrary.ci.buildDate = new Date().toLocaleString();
                target.eglibrary.ci.buildVersionName = "ci.${target.eglibrary.ci.buildVersionCode}";
            } else {
                println("not CI");
                target.eglibrary.ci.ciRunning = false;
                target.eglibrary.ci.buildVersionCode = "1";
                target.eglibrary.ci.buildDate = new Date().toLocaleString();
                target.eglibrary.ci.buildVersionName = "build.${System.getenv("USER")}.${target.eglibrary.ci.buildDate}";

            }
        }

//        println("target.eglibrary.ci.releaseDir(${target.eglibrary.ci.releaseDir})")
//        println("target.eglibrary.ci.ciRunning(${target.eglibrary.ci.ciRunning})")
//        println("target.eglibrary.ci.buildVersionCode(${target.eglibrary.ci.buildVersionCode})")
//        println("target.eglibrary.ci.buildDate(${target.eglibrary.ci.buildDate})")
//        println("target.eglibrary.ci.buildVersionName(${target.eglibrary.ci.buildVersionName})")

        // 規定のタスクを追加
        target.task('genLocalProperties', type: AndroidLocalPropertiesGenTask)
        target.task('ciCollect', type: AndroidCiCollectTask)
        target.task('ciClean', type: AndroidCiCleanTask);

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
         * CI(Jenkins)上で実行されていればtrue
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