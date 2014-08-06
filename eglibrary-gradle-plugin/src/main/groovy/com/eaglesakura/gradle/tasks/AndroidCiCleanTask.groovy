package com.eaglesakura.gradle.tasks

import com.eaglesakura.tool.log.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class AndroidCiCleanTask extends DefaultTask {

    @TaskAction
    def onExecute() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        File dstDirectory = project.eglibrary.ci.releaseDir;

        Logger.out("rm dst(${dstDirectory.absolutePath})")
        dstDirectory.deleteDir();
    }
}