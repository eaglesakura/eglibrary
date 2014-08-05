package com.eaglesakura.gradle.tasks

import com.eaglesakura.tool.log.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Androidのlocal.propを出力するためのUtilタスク
 */
public class AndroidLocalPropertiesGenTask extends DefaultTask {

    def ANDROID_SDK_HOME = System.getenv("ANDROID_HOME");
    def ANDROID_NDK_HOME = System.getenv("ANDROID_NDK_HOME");

    @TaskAction
    def generate() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        Logger.out "ANDROID_SDK_HOME(${ANDROID_SDK_HOME})"
        Logger.out "ANDROID_NDK_HOME(${ANDROID_NDK_HOME})"

        File prop = project.file("local.properties");
        Logger.out "generate -> ${prop.absolutePath}"
        prop.write("sdk.dir=${ANDROID_SDK_HOME}\n");
        prop.append("ndk.dir=${ANDROID_NDK_HOME}\n");
    }
}