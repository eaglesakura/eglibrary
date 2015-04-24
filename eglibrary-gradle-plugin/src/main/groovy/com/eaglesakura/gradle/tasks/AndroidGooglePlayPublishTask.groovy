package com.eaglesakura.gradle.tasks

import com.eaglesakura.gradle.android.googleplay.GooglePlayConsoleManager
import com.eaglesakura.tool.log.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Google PlayへのアップロードをサポートするServiceを提供する
 *
 * アップロードは必ずServiceAccountを通して行う。
 */
public class AndroidGooglePlayPublishTask extends DefaultTask {

    enum Track {
        alpha,
        beta,
        production,
        rollout;
    }


    File p12;

    File apk;

    String applicationId;

    String serviceAccountEmail;

    Track track;

    /**
     * listings
     *   * jp/title.txt
     *   * jp/fullDescription.txt
     *   * jp/shortDescription.txt
     */
    File listings;

    protected GooglePlayConsoleManager googlePlayConsoleManager;

    protected void onGooglePayTask() {
        googlePlayConsoleManager.autholize();

        googlePlayConsoleManager.updateListings(listings);
//        googlePlayConsoleManager.uploadApk();
    }

    @TaskAction
    def onExecute() {
        Logger.out "applicationId : ${applicationId}"
        Logger.out "track         : ${track.name()}"
        Logger.out "apk           : ${apk.absolutePath}"
        Logger.out "account email : ${serviceAccountEmail}"

        googlePlayConsoleManager = new GooglePlayConsoleManager();
        googlePlayConsoleManager.p12 = p12;
        googlePlayConsoleManager.apk = apk;
        googlePlayConsoleManager.applicationId = applicationId;
        googlePlayConsoleManager.serviceAccountEmail = serviceAccountEmail;

        onGooglePayTask();
    }
}