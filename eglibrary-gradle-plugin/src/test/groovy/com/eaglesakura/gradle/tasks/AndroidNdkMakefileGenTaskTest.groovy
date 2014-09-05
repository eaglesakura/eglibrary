package com.eaglesakura.gradle.tasks

import com.eaglesakura.gradle.android.ndk.ModuleType
import org.gradle.testfixtures.ProjectBuilder

public class AndroidNdkMakefileGenTaskTest extends GroovyTestCase {

    public void testGenMakefiles() {
        def project = ProjectBuilder.builder().build();
        def task = (AndroidNdkMakefileGenTask) project.task("makefileGen", type: AndroidNdkMakefileGenTask);
        task.outDirectory = new File("eglibrary/eglibrary-android-glkit/jni")

        task.abi([AndroidNdkMakefileGenTask.ABI_ARM, AndroidNdkMakefileGenTask.ABI_ARMv7a]);

        // module
        def module = task.newModule("eglibrary-glkit", ModuleType.SharedLibrary);
        module.include("src")
        module.source("src")

        module.include("jcgen")
        module.source("jcgen")

        module.cppFlag "-fexceptions";
        module.cppFlag "-pthread";
        module.cppFlag "-frtti";
        module.cFlag "-Wno-psabi"

        module.cpp11();

        module.ldlibs(['EGL', 'GLESv1_CM', 'GLESv3', 'android', 'log', 'jnigraphics']);

        task.generate();
//        module.source(new File())
    }
}