package com.eaglesakura.gradle.tasks

import com.eaglesakura.tool.generator.CodeWriter
import com.eaglesakura.tool.log.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class AndroidPropGenTask extends DefaultTask {
    def classPackageName = "com.example";
    def className = "SampleSettingClass";
    def superClass = "com.eaglesakura.android.db.BasePropertiesDatabase"
    def outDirectory = new File("gen-eglib").absoluteFile;

    /**
     * 頭の１文字目を大文字にする
     */
    private static String toCamelCaseUpper(String base) {
        return "${base.substring(0, 1).toUpperCase()}${base.substring(1)}";
    }

    /**
     * 保持しているプロパティ一覧
     */
    private List<Property> properties = new ArrayList<Property>();

    public AndroidPropGenTask() {
    }

    public void floatProperty(final String name, final String defaultValue) {
        properties.add(new Property(name, defaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(float set){ setProperty(\"${name}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public double get${toCamelCaseUpper(name)}(){ return getFloatProperty(\"${name}\"); }";
            }
        })
    }

    public void doubleProperty(final String name, final String defaultValue) {
        properties.add(new Property(name, defaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(double set){ setProperty(\"${name}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public double get${toCamelCaseUpper(name)}(){ return getDoubleProperty(\"${name}\"); }";
            }
        })
    }

    public void intProperty(final String name, final String defaultValue) {
        properties.add(new Property(name, defaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(int set){ setProperty(\"${name}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public long get${toCamelCaseUpper(name)}(){ return getIntProperty(\"${name}\"); }";
            }
        })
    }

    public void longProperty(final String name, final String defaultValue) {
        properties.add(new Property(name, defaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(long set){ setProperty(\"${name}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public long get${toCamelCaseUpper(name)}(){ return getLongProperty(\"${name}\"); }";
            }
        })
    }

    public void dateProperty(final String name) {
        properties.add(new Property(name, "0") {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(java.util.Date set){ setProperty(\"${name}\", set != null ? set.getTime() : 0); }";
            }

            @Override
            String generateGetter() {
                return "public java.util.Date get${toCamelCaseUpper(name)}(){ return new java.util.Date(getLongProperty(\"${name}\")); }";
            }
        })
    }

    public void stringProperty(final String name, final String defaultValue) {
        properties.add(new Property(name, defaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(String set){ setProperty(\"${name}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public String get${toCamelCaseUpper(name)}(){ return getStringProperty(\"${name}\"); }";
            }
        })
    }

    /**
     * JSON型のPropertiesを生成する
     * @param name プロパティ名
     * @param pojoFullName JSONの
     */
    public void jsonProperty(final String name, final String pojoFullName) {
        properties.add(new Property(name, "{}") {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(${pojoFullName} set){ setProperty(\"${name}\", com.eaglesakura.json.JSON.encodeOrNull(set)); }";
            }

            @Override
            String generateGetter() {
                return "public ${pojoFullName} get${toCamelCaseUpper(name)}(){ return com.eaglesakura.json.JSON.decodeOrNull(getStringProperty(\"${name}\"), ${pojoFullName}.class); }";
            }
        })
    }

    void build(File srcRootDirectory) {

        FILE_CHECK:
        {
            // 規定の経路を生成する
            String[] dirs = classPackageName.split("\\.");
            for (String s : dirs) {
                srcRootDirectory = new File(srcRootDirectory, s);
            }
            srcRootDirectory.mkdirs();

            // ファイル名指定
            srcRootDirectory = new File(srcRootDirectory, "${className}.java");
        }

        CodeWriter writer = new CodeWriter(srcRootDirectory);

        // packagename
        writer.writeLine("package ${classPackageName};").newLine();

        // import
        writer.writeLine("import android.content.Context;")
//        writer.writeLine("import java.io.File;");
        writer.newLine();

        // class name
        writer.writeLine("public class ${className} extends ${superClass} {").pushIndent(true);

        // コンストラクタと初期化
        INIT:
        {
            writer.writeLine("public ${className}(Context context){ super(context, \"${className.toLowerCase()}.db\"); _initialize(); }");
            writer.writeLine("public ${className}(Context context, String dbFileName){ super(context, dbFileName); _initialize(); }")

            // 初期化メソッド
            writer.writeLine("protected void _initialize() {").pushIndent(true);
            writer.newLine();
            // Propertiesを出力する
            for (Property prop : properties) {
                writer.writeLine("addProperty(\"${prop.name}\", \"${prop.defaultValue}\");");
            }

            // 初期値のロードを行う
            writer.newLine().writeLine("load();");

            // メソッドを閉じる
            writer.popIndent(true).writeLine("}");
        }

        // アクセサメソッドを生成する
        Accr:
        {
            for (Property prop : properties) {
                writer.writeLine(prop.generateSetter());
                writer.writeLine(prop.generateGetter());
            }
        }
        writer.popIndent(true).writeLine("}");

        // 生成完了
        writer.commit();
    }

    @TaskAction
    def generate() {
        Logger.initialize();
        Logger.outLogLevel = 0;

        Logger.out "classPackageName(${classPackageName})"
        Logger.out "className(${className})"
        Logger.out "superClass(${superClass})"
        Logger.out "outDirectory(${outDirectory.absolutePath})"


        build(outDirectory);
    }

    /**
     * 設定項目を指定する
     */
    static abstract class Property {
        final String defaultValue;

        final String name;

        Property(String name, String defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        /**
         * setter用コードを生成する
         */
        abstract String generateSetter();

        /**
         * getter用コードを生成する
         */
        abstract String generateGetter();
    }
}