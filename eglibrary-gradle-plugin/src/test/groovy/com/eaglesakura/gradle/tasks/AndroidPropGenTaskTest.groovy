package com.eaglesakura.gradle.tasks

import org.gradle.testfixtures.ProjectBuilder

public class AndroidPropGenTaskTest extends GroovyTestCase {
    public void testGenClass() {
        def project = ProjectBuilder.builder().build();
        def task = (AndroidPropGenTask) project.task("genProp", type: AndroidPropGenTask);

        task.stringProperty("stringValue", "nil");
        task.doubleProperty("doubleValue", "1.2345");
        task.doubleProperty("floatValue", "1.23f");
        task.longProperty("longValue", "12345");
        task.intProperty("intValue", "123");
        task.jsonProperty("jsonValue", "com.example.Pojo");
        task.dateProperty("updatedTime");
        task.outDirectory = new File("gen");
        task.execute();
    }
}