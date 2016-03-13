package com.eaglesakura.gradle.tasks

import com.eaglesakura.gradle.android.ndk.MakefileGenerator
import com.eaglesakura.gradle.android.ndk.ModuleType
import com.eaglesakura.tool.generator.CodeWriter
import com.eaglesakura.tool.log.Logger
import com.eaglesakura.util.StringUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Makefileを出力するためのクラス
 */
public class AndroidNdkMakefileGenTask extends DefaultTask {
    private List<MakefileGenerator> modules = new ArrayList<>();

    public static final String ABI_ARM = "armeabi";

    public static final String ABI_ARMv7a = "armeabi-v7a";

    public static final String ABI_X86 = "x86";

    public static final String ABI_MIPS = "mips";

    public static final String ABI_ALL = "all";

    /**
     * armeabi-v7a等を指定する
     */
    private List<String> abis = new ArrayList<>();

    /**
     * 拡張ライン
     */
    final List<String> extraLines = new ArrayList<>();

    /**
     * APP_STL
     */
    def appStl = "gnustl_static";

    /**
     * ビルド対象のAPIレベルを指定する
     */
    def appPlatform = "android-19";

    /**
     * 出力先
     */
    File outDirectory = new File("jni");

    public void abi(String _abi) {
        abis.add(_abi);
    }

    public void abi(List<String> _abis) {
        abis.addAll(_abis);
    }

    /**
     * 新たにMakefileModuleを生成する
     *
     * @param moduleName モジュール名
     * @param type タイプ
     * @return
     */
    public MakefileGenerator newModule(String moduleName, ModuleType type) {
        MakefileGenerator result = new MakefileGenerator(outDirectory);
        result.module = moduleName;
        result.type = type;

        modules.add(result);

        return result;
    }

    /**
     * Application.mkを出力させる
     */
    private void generateApplicationMk() {
        def makefile = new File(outDirectory, "Application.mk");
        Logger.out "generate ${makefile.absolutePath}"

        CodeWriter writer = new CodeWriter(makefile);

        // APP_PLATFORM
        if (!StringUtil.isEmpty(appPlatform)) {
            writer.writeLine("###### ABI");
            writer.writeLine("APP_PLATFORM := ${appPlatform}");
            writer.newLine();
        }

        // APP_STL
        if (!StringUtil.isEmpty(appStl)) {
            writer.writeLine("###### STL");
            writer.writeLine("APP_STL := ${appStl}");
            writer.newLine();
        }

        APP_ABI:
        {
            if (abis.empty) {
                abis.add(ABI_ARMv7a);
            }

            writer.writeLine("###### ABI");
            writer.write("APP_ABI :=");
            for (def abi : abis) {
                writer.write(" ${abi}");
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

        writer.commit();
    }

    /**
     * Moduleを出力する
     */
    private void generateAndroidMk() {
        def makefile = new File(outDirectory, "Android.mk");
        Logger.out "generate ${makefile.absolutePath}"

        CodeWriter writer = new CodeWriter(makefile);
        // 初期化
        writer.writeLine("LOCAL_PATH := \$(call my-dir)");

        for (def module : modules) {
            module.generate(writer);
        }

        writer.commit();
    }

    @TaskAction
    def generate() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        outDirectory.mkdirs();

        generateApplicationMk();
        generateAndroidMk();
    }
}
