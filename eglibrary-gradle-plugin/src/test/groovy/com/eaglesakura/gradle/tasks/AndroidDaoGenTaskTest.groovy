package com.eaglesakura.gradle.tasks

import org.gradle.testfixtures.ProjectBuilder

public class AndroidDaoGenTaskTest extends GroovyTestCase {
    public void testGenClass() {
        def project = ProjectBuilder.builder().build();
        def task = (AndroidDaoGenTask) project.task("genDao", type: AndroidDaoGenTask);

        task.outDirectory = new File("gen").absoluteFile;
        task.classPackageBase = "com.eaglesakura.test.db"

        TEMP_DB:
        {
            def schema = task.newSchema(0x01, "temp");
            def DbTempEntity = schema.addEntity("DbTempEntity");

            DbTempEntity.addStringProperty("uniqueId").primaryKey().index().unique();
            DbTempEntity.addStringProperty("value").notNull();
        }

        TEMP_DB2:
        {
            def schema = task.newSchema(0x01, "test");
            def DbTempEntity = schema.addEntity("DbTestEntity");

            DbTempEntity.addStringProperty("uniqueId").primaryKey().index().unique();
            DbTempEntity.addDoubleProperty("value").notNull();
        }

        task.execute();
    }
}