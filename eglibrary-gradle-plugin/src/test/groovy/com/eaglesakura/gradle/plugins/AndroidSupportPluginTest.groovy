package com.eaglesakura.gradle.plugins

import org.gradle.testfixtures.ProjectBuilder

public class AndroidSupportPluginTest extends GroovyTestCase {

    public void testPluginApply() {
        def project = ProjectBuilder.builder().build();
        project.apply plugin: AndroidSupportPlugin

        project.tasks.genLocalProperties.execute();
        project.tasks.ciCollect.execute();
    }
}