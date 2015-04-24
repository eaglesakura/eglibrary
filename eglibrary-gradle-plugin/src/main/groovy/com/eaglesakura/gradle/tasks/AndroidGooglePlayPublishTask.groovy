package com.eaglesakura.gradle.tasks

import com.eaglesakura.io.IOUtil
import com.eaglesakura.tool.log.Logger
import com.eaglesakura.util.StringUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class AndroidGooglePlayPublishTask extends DefaultTask {

    enum Track {
        alpha,
        beta,
        rollout;
    }


    File p12;

    File apk;

    String applicationId;

    String applicationName;

    String serviceAccountEmail;

    Track track;

    private void validate() {
        if (!IOUtil.isFile(p12)) {
            throw new IllegalStateException("task.p12 File Error")
        }

        if (!IOUtil.isFile(apk)) {
            throw new IllegalStateException("task.apk File Error");
        }

        if (StringUtil.isEmpty(applicationId)) {
            throw new IllegalStateException("task.applicationId Error");
        }

        if (StringUtil.isEmpty(applicationName)) {
            throw new IllegalStateException("task.applicationName Error");
        }

        if (StringUtil.isEmpty(serviceAccountEmail)) {
            throw new IllegalStateException("task.serviceAccountEmail Error");
        }

        if (track == null) {
            throw new IllegalStateException("task.track Error");
        }
    }

    @TaskAction
    def onExecute() {
        validate()

        Logger.out "Deploy        : ${applicationName} / ${applicationId}"
        Logger.out "track         : ${track.name()}"
        Logger.out "apk           : ${apk.absolutePath}"
        Logger.out "account email : ${serviceAccountEmail}"
    }
}