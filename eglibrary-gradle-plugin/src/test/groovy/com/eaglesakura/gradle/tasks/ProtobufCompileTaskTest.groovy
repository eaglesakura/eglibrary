package com.eaglesakura.gradle.tasks

import org.gradle.testfixtures.ProjectBuilder

public class ProtobufCompileTaskTest extends GroovyTestCase {

    public void testCompile() {
        def project = ProjectBuilder.builder().build();
        def task = (ProtobufCompileTask) project.task("protobufCompile", type: ProtobufCompileTask);

        task.src = new File(".");
        task.javaOutput = new File("gen-protobuf");
        task.cppOutput = new File("gen-protobuf-cpp");

        task.execute()
    }
}
