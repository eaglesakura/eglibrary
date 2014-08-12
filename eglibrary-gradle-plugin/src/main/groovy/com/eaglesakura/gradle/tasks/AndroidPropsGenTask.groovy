package com.eaglesakura.gradle.tasks

import com.eaglesakura.gradle.android.props.PropClassGenerator
import com.eaglesakura.tool.log.Logger
import com.eaglesakura.util.StringUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * 複数の設定ファイルを生成するためのTask
 */
public class AndroidPropsGenTask extends DefaultTask {
    def superClass = '';
    def dbFileName = '';
    def outDirectory = new File("gen");

    List<PropClassGenerator> props = new ArrayList<>();

    /**
     * 新たなプロパティを生成する
     * @param fullClassName
     * @return
     */
    public PropClassGenerator newProps(String fullClassName) {
        PropClassGenerator result = new PropClassGenerator();

        int lastDotIndex = fullClassName.lastIndexOf('.');
        result.classPackageName = fullClassName.substring(0, lastDotIndex);
        result.className = fullClassName.substring(lastDotIndex + 1);

        // 一覧に登録する
        props.add(result);

        return result;
    }

    @TaskAction
    public void generate() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        for (def prop : props) {
            if (!StringUtil.isEmpty(superClass)) {
                prop.superClass = superClass;
            }

            if (!StringUtil.isEmpty(dbFileName)) {
                prop.dbFileName = dbFileName;
            }

            if (outDirectory != null && prop.outDirectory == null) {
                prop.outDirectory = outDirectory;
            }

            Logger.out "classPackageName(${prop.classPackageName})"
            Logger.out "className(${prop.className})"
            Logger.out "superClass(${prop.superClass})"
            Logger.out "outDirectory(${prop.outDirectory.absolutePath})"

            prop.build();
        }
    }

}