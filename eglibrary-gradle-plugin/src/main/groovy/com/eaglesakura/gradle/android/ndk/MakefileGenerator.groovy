package com.eaglesakura.gradle.android.ndk

import com.eaglesakura.tool.generator.CodeWriter
import com.eaglesakura.tool.log.Logger

public class MakefileGenerator {
    /**
     * LOCAL_SRC_FILES
     */
    private List<String> sourceFiles = new ArrayList<>();

    /**
     * LOCAL_C_INCLUDES
     */
    private List<String> includeDirs = new ArrayList<>();

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
     * Extra
     */
    final List<String> extraLines = new ArrayList<>();

    /**
     * LOCAL_MODULE
     */
    String module = "app";

    /**
     *
     */
    ModuleType type = ModuleType.SharedLibrary;

    /**
     * 出力ディレクトリ
     */
    final File outDirectory;

    public MakefileGenerator(File outDirectory) {
        this.outDirectory = outDirectory;
    }

    /**
     * ソースコードを指定パスから探索する
     *
     * @param findPath 探索パス
     */
    public void source(String findPath) {
        def path = new File(outDirectory, findPath);

        if (path.directory) {
            // 再起
            def files = path.listFiles();
            for (File f : files) {
                source("${findPath}/${f.name}");
            }
        } else if (path.file) {
            def name = path.name;

            if (name.endsWith(".cpp") || name.endsWith(".c")) {
                sourceFiles.add(findPath);
                Logger.out "add source(${findPath})"
            }
        }
    }
    /**
     * includeディレクトリの相対パスを指定する
     *
     * @param findPath 探索パス
     */
    public void include(String findPath) {
        File dir = new File(outDirectory, findPath);
        if (dir.directory) {
            includeDirs.add(findPath);
            Logger.out "add source(${findPath})"
        }
    }

    /**
     * 別の成果物をincludeする
     */
    public void include(MakefileGenerator makefile) {
        includeDirs.add(makefile.includeDirs);
        staticModules.add(makefile.module);
    }

    /**
     * LOCAL_CPPFLAGSに直接フラグを指定する。
     *
     * @param flag
     */
    public void cppFlag(String flag) {
        cppFlags.add(flag);
    }

    /**
     * LOCAL_CFLAGSに直接フラグを指定する
     * @param flag
     */
    public void cFlag(String flag) {
        cFlags.add(flag);
    }

    /**
     * C/CPPフラグを同時に指定する
     * @param flag
     */
    public void flags(String flag) {
        cppFlags.add(flag);
        cFlags.add(flag);
    }

    /**
     * #define定義を行う
     *
     * @param name define名. "__ARM_V7__"等、-Dを付与せずに指定する
     */
    public void define(String name) {
        name = "-D${name}";

        cFlag name;
        cppFlag name;
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
        localLdlibs.addAll(libs);
    }

    public static final def LDLIB_EGL = "EGL";

    public static final def LDLIB_GLES11 = "GLESv1_CM";

    public static final def LDLIB_GLES20 = "GLESv2";

    public static final def LDLIB_GLES30 = "GLESv3";

    public static final def LDLIB_GLES31 = "GLESv3";

    public static final def LDLIB_ANDROID_LOG = "log";

    public static final def LDLIB_ANDROID = "android";

    public static final def LBLIB_ANDROID_JNIGRAPHICS = "jnigraphics";

    /**
     * C++11としてビルドする
     */
    public void cpp11() {
        cppFlag "-std=c++0x"
    }

    /**
     * Makefile生成を行う
     *
     * @param writer 書き込み先
     */
    public void generate(CodeWriter writer) {
        writer.writeLine("############  Module : ${module}  ############");
        writer.writeLine("include \$(CLEAR_VARS)");

        // module name
        LOCAL_MODULE:
        {
            writer.writeLine("###### Includes")
            writer.writeLine("LOCAL_MODULE := ${module}");
            writer.newLine();
        }

        // include
        LOCAL_C_INCLUDES:
        {
            writer.writeLine("###### Includes")
            for (def file : includeDirs) {
                writer.writeLine("LOCAL_C_INCLUDES += ${file}")
            }
            writer.newLine();
        }

        // cpp files
        LOCAL_SRC_FILES:
        {
            writer.writeLine("###### Sources")
            for (def file : sourceFiles) {
                writer.writeLine("LOCAL_SRC_FILES += ${file}")
            }
            writer.newLine();
        }

        LOCAL_FLAGS:
        {
            writer.writeLine("###### Flags")
            for (def flag : cFlags) {
                writer.writeLine("LOCAL_CFLAGS += ${flag}")
            }
            for (def flag : cppFlags) {
                writer.writeLine("LOCAL_CPPFLAGS += ${flag}")
            }
            writer.newLine();
        }

        LOCAL_LDLIBS:
        {
            writer.writeLine("###### Libs")
            for (def lib : localLdlibs) {
                writer.writeLine("LOCAL_LDLIBS += -l${lib}")
            }
            writer.newLine();
        }

        // EXTRA
        if (!extraLines.empty) {
            writer.writeLine("###### EXTRA");
            for (def line : extraLines) {
                writer.writeLine(line);
            }
            writer.newLine();
        }

        // BUILD
        BUILD:
        {
            writer.writeLine("###### Build")
            writer.writeLine(type.buildLine());
            writer.newLine();
        }
    }
}