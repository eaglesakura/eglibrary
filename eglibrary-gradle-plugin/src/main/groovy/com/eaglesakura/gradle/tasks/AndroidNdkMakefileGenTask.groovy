package com.eaglesakura.gradle.tasks;

import com.eaglesakura.tool.log.Logger;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Makefileを出力するためのクラス
 */
public class AndroidNdkMakefileGenTask extends DefaultTask {




    @TaskAction
    def generate() {
        Logger.initialize();
        Logger.outLo
        Logger.outLogLevel = 0;
    }
}
