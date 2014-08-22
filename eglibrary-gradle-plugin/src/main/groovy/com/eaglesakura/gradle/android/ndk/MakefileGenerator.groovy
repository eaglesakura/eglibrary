package com.eaglesakura.gradle.android.ndk

import com.eaglesakura.tool.generator.CodeWriter
import com.eaglesakura.tool.log.Logger

public class MakefileGenerator {
    /**
     * LOCAL_SRC_FILES
     */
    private List<String> sourceFiles = new ArrayList<String>();

    /**
     * LOCAL_C_INCLUDES
     */
    private List<String> includeDirs = new ArrayList<String>();

    /**
     * LOCAL_STATIC_LIBRARIES
     * *.a
     */
    private List<String> staticModules = new ArrayList<>();

    /**
     * LOCAL_CFLAGS
     */
    private List<String> cFlags = new ArrayList<>();

    /**
     * LOCAL_CPPFLAGS
     */
    private List<String> cppFlags = new ArrayList<>();

    /**
     * LOCAL_LDLIBS
     */
    private List<String> localLdlibs = new ArrayList<>();

    /**
     * LOCAL_MODULE
     */
    String module = "app";

    /**
     *
     */
    ModuleType type = SharedLibrary;

    public enum ModuleType {
        PrebuildStaticLibrary{
            @Override
            String includeModule() {
                return "include \$(PREBUILT_STATIC_LIBRARY"; // *.a
            }
        },

        SharedLibrary{
            @Override
            String includeModule() {
                return "include \$(BUILD_SHARED_LIBRARY)";   // *.so
            }
        };

        public abstract String includeModule();
    }

    public MakefileGenerator() {

    }

    /**
     * 複数ファイルを読み込む
     * @param dir
     */
    public void source(File dir) {
        if (dir.directory) {
            // 再起
            def files = dir.listFiles();
            for (File f : files) {
                source(f);
            }
        } else {
            sourceFiles.add(file.path);
            Logger.out "add source(${file.path})"
        }
    }

    /**
     * ソースコードをリストで追加する
     * @param sources
     */
    public void source(List<File> sources) {
        for (File file : sources) {
            sources(file);
        }
    }

    /**
     * ディレクトリをincludeする
     * @param dir
     */
    public void include(File dir) {
        includeDirs.add(dir.path);
        Logger.out "add source(${file.path})"
    }

    /**
     * 別の成果物をincludeする
     */
    public void include(MakefileGenerator makefile) {
        includeDirs.add(makefile.includeDirs);
        staticModules.add(makefile.module);
    }

    /**
     * GLESv2, EGL等を指定する
     *
     * @param lib ライブラリ名
     */
    public void ldlibs(String lib) {
        localLdlibs.add(lib);
    }

    /**
     * GLESv2, EGL等を指定する
     *
     * @param libs
     */
    public void ldlibs(List<String> libs) {
        localLdlibs.add(libs);
    }

    /**
     * Makefile生成を行う
     *
     * @param writer 書き込み先
     */
    public void generate(CodeWriter writer) {
        writer.writeLine("############  Module : ${module}  ############");
        writer.writeLine("include \$(CLEAR_VARS)");

        // include
        LOCAL_C_INCLUDES:
        {
            writer.writeLine("###### Includes")
            for (def file : sourceFiles) {
                writer.write("LOCAL_SRC_FILES += ${file}")
            }
        }

        // cpp files
        LOCAL_SRC_FILES:
        {
            writer.writeLine("###### Sources")
            for (def file : sourceFiles) {
                writer.write("LOCAL_SRC_FILES += ${file}")
            }
        }
    }
}