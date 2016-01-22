package com.eaglesakura.gradle.tasks

import com.eaglesakura.tool.log.Logger
import de.greenrobot.daogenerator.DaoGenerator
import de.greenrobot.daogenerator.Schema
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Dao出力用のタスク
 */
public class AndroidDaoGenTask extends DefaultTask {
    def outDirectory = new File("dao").absoluteFile;
    def classPackageBase = "com.example.dao";

    /**
     * 出力対象のスキーマ
     */
    private def List<Schema> schemas = new ArrayList<>();

    /**
     * 新たなスキーマを生成する
     * @param version
     * @param packageName
     * @return
     */
    public Schema newSchema(int version, String packageName) {
        Schema result = new Schema(version, "${classPackageBase}.${packageName}");
        schemas.add(result);
        return result;
    }

    @TaskAction
    def generate() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        Logger.out "classPackageBase(${classPackageBase})"
        Logger.out "outDirectory(${outDirectory.absolutePath})"


        outDirectory.mkdirs();
        DaoGenerator gen = new DaoGenerator();
        for (Schema s : schemas) {
            gen.generateAll(s, outDirectory.absolutePath);
        }
    }
}