package com.eaglesakura.gradle.tasks

import com.eaglesakura.gradle.android.googleplay.GooglePlayConsoleManager
import com.eaglesakura.io.IOUtil
import com.eaglesakura.tool.log.Logger
import com.eaglesakura.util.StringUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Google PlayへのアップロードをサポートするServiceを提供する
 *
 * アップロードは必ずServiceAccountを通して行う。
 */
public class AndroidGooglePlayPublishTask extends DefaultTask {

    File p12;

    File apk;

    String applicationId;

    String serviceAccountEmail;

    String track;

    /**
     * listings
     *   * ja-JP/title.txt
     *   * ja-JP/fullDescription.txt
     *   * ja-JP/shortDescription.txt
     */
    File listings;

    /**
     * apk update Listings
     *  * ja-JP/apk.txt
     */
    File apkListings;


    protected GooglePlayConsoleManager googlePlayConsoleManager;

    protected void onGooglePayTask() {
        googlePlayConsoleManager.autholize();

        if (!StringUtil.isEmpty(track)) {
            googlePlayConsoleManager.uploadApk(track, apkListings);
        }

        if (IOUtil.isDirectory(listings)) {
            googlePlayConsoleManager.updateListings(listings);
        }
    }

    @TaskAction
    def onExecute() {
        Logger.out "applicationId       : ${applicationId}"
        Logger.out "track               : ${track}"
        Logger.out "listings            : ${listings}"
        Logger.out "apk                 : ${apk}"
        Logger.out "apklistings         : ${apkListings}"
        Logger.out "serviceAccountEmail : ${serviceAccountEmail}"

        googlePlayConsoleManager = new GooglePlayConsoleManager();
        googlePlayConsoleManager.p12 = p12;
        googlePlayConsoleManager.apk = apk;
        googlePlayConsoleManager.applicationId = applicationId;
        googlePlayConsoleManager.serviceAccountEmail = serviceAccountEmail;

        onGooglePayTask();
    }
}