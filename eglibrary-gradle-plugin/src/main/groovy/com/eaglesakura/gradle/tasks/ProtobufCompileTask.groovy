package com.eaglesakura.gradle.tasks

import com.eaglesakura.tool.log.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Protocol Buffersのコンパイルを行うタスク
 */
public class ProtobufCompileTask extends DefaultTask {
    def classPath = [''];
    def src = new File("idl");
    def javaOutput = new File("protobuf");
    File cppOutput = null;

    /**
     * compileを行う。
     */
    void compile(List<String> baseCommand, File proto) {
        if (proto.file && proto.name.endsWith(".proto")) {
            // compile
            List<String> command = new ArrayList<>(baseCommand);
            command.add(proto.absolutePath);
            Logger.out "compile ${proto.absolutePath}"
            Logger.out command.execute().text;
        } else if (proto.directory) {
            // 子を実行する
            def files = proto.listFiles();
            if (files != null) {
                for (def next : files) {
                    compile(baseCommand, next);
                }
            }
        }
    }

    @TaskAction
    def generate() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        Logger.out "src(${src.absolutePath})"


        def command = ['protobuf'];

        for (def path : classPath) {
            Logger.out "classpath :: ${path}";
            command.add("--proto_path=${path}");
        }

        if (javaOutput != null) {
            Logger.out "javaOutput :: ${javaOutput.mkdirs()}";

            javaOutput.mkdirs();
            command.add("--java_out");
            command.add("${javaOutput.absolutePath}")
        }

        if (cppOutput != null) {
            cppOutput.mkdirs();
            command.add("--cpp_out");
            command.add("${cppOutput.absolutePath}")
        }

        compile(command, src);
    }
}