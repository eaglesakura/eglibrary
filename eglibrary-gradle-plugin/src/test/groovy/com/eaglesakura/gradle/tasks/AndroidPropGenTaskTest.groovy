package com.eaglesakura.gradle.tasks

import org.gradle.testfixtures.ProjectBuilder

public class AndroidPropGenTaskTest extends GroovyTestCase {
    public void testGenClass() {
        def project = ProjectBuilder.builder().build();
        def task = (AndroidPropGenTask) project.task("genProp", type: AndroidPropGenTask);

        task.stringProperty("stringValue", "nil");
        task.doubleProperty("doubleValue", 1.2345);
        task.doubleProperty("floatValue", 1.23f);
        task.longProperty("longValue", 12345);
        task.intProperty("intValue", 123);
        task.jsonProperty("jsonValue", "com.example.Pojo");
        task.dateProperty("updatedTime");
        task.booleanProperty("boolValue", false);
        task.enumProperty("enumValue", TestEnum.class.getName(), TestEnum.Hoge.name());
        task.protobufProperty("protobufValue", "com.example.Protobuf");
        task.outDirectory = new File("gen");
        task.execute();
    }

    public void testGenClasses() {
        def project = ProjectBuilder.builder().build();
        def task = (AndroidPropsGenTask) project.task("genProp", type: AndroidPropsGenTask);
        task.outDirectory = new File("gen");
        task.dbFileName = "def.db"

        def GeneratedProp = task.newProps("com.eaglesakura.GeneratedProp");
        GeneratedProp.stringProperty("stringValue", "nil");
        GeneratedProp.doubleProperty("doubleValue", 1.2345);
        GeneratedProp.doubleProperty("floatValue", 1.23f);
        GeneratedProp.longProperty("longValue", 12345);
        GeneratedProp.intProperty("intValue", 123);

        def SettingClass = task.newProps("com.eaglesakura.db.SettingClass");
        SettingClass.jsonProperty("jsonValue", "com.example.Pojo");
        SettingClass.dateProperty("updatedTime");
        SettingClass.booleanProperty("boolValue", false);
        SettingClass.enumProperty("enumValue", TestEnum.class.getName(), TestEnum.Hoge.name());
        SettingClass.protobufProperty("protobufValue", "com.example.Protobuf");

        task.execute();
    }
}