package com.eaglesakura.gradle.tasks

import org.gradle.testfixtures.ProjectBuilder

public class AndroidPropGenTaskTest extends GroovyTestCase {

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
        SettingClass.bitmapProperty("bitmapValue");

        task.execute();
    }
}