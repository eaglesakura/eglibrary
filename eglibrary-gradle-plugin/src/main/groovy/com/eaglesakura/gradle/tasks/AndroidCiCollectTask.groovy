package com.eaglesakura.gradle.tasks

import com.eaglesakura.tool.log.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class AndroidCiCollectTask extends DefaultTask {

    @TaskAction
    def generate() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        File dstDirectory = new File(project.eglibrary.ci.releaseDir, project.file(".").name);
        File srcDirectory = project.file("build/outputs");

        // Android Project以外では何もしない
        if (!(new File(srcDirectory, "apk").directory)) {
            Logger.out "is not Android Project -> ${project.file(".").absolutePath}";
            return;
        }

        println("collect src(${srcDirectory.absolutePath}) -> dst(${dstDirectory.absolutePath})")
        dstDirectory.mkdirs();
        srcDirectory.renameTo(dstDirectory);
    }
}