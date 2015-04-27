package com.eaglesakura.gradle.tasks

import com.eaglesakura.tool.log.Logger
import org.gradle.testfixtures.ProjectBuilder

public class AndroidGooglePlayPublishTaskTest extends GroovyTestCase {
    public void testGooglePlayPublish() {
        def project = ProjectBuilder.builder().build();
        def task = (AndroidGooglePlayPublishTask) project.task("googlePlayPublish", type: AndroidGooglePlayPublishTask);

        def rootDir = new File(".").absolutePath;
        if (!new File(rootDir, "testfile").directory) {
            // not setup
            return;
        }

        task.p12 = new File(rootDir, "testfile/test.p12");
        task.apk = new File(rootDir, "testfile/test.apk");
        task.applicationId = new File(rootDir, "testfile/test-appId.txt").text;
        task.serviceAccountEmail = new File(rootDir, "testfile/test-email.txt").text;
        task.track = "alpha";
        task.listings = new File(rootDir, "testfile/listing");
        task.apkListings = new File(rootDir, "testfile/listing");
        Logger.out "PATH = ${new File(".").absolutePath}"

        task.execute();
    }
}