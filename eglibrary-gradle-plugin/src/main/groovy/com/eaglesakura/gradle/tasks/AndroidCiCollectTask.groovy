package com.eaglesakura.gradle.tasks

import com.eaglesakura.tool.log.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class AndroidCiCollectTask extends DefaultTask {

    @TaskAction
    def generate() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        File dstDirectory = project.eglibrary.ci.releaseDir;
        File srcDirectory = project.file("build/outputs");

        println("collect src(${srcDirectory.absolutePath}) -> dst(${dstDirectory.absolutePath})")
        dstDirectory.mkdirs();
        srcDirectory.renameTo(dstDirectory);
    }
}