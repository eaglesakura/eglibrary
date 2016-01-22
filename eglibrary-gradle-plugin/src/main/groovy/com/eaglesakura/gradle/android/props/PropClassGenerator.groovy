package com.eaglesakura.gradle.android.props

import com.eaglesakura.tool.generator.CodeWriter

public class PropClassGenerator {
    def classPackageName = "com.example";
    def className = "SampleSettingClass";
    def superClass = "com.eaglesakura.android.db.BasePropertiesDatabase"
    def dbFileName = "props.db"
    File outDirectory = null;

    /**
     * コンストラクタで自動的に既存データを読み込む場合はtrue
     */
    def autoPropLoad = true;

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

    public void floatProperty(String propName, float propDefaultValue) {
        properties.add(new Property("${className}.${propName}", propName, "" + propDefaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(float set){ setProperty(\"${key}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public float get${toCamelCaseUpper(name)}(){ return getFloatProperty(\"${key}\"); }";
            }
        })
    }

    public void doubleProperty(String propName, double propDefaultValue) {
        properties.add(new Property("${className}.${propName}", propName, "" + propDefaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(double set){ setProperty(\"${key}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public double get${toCamelCaseUpper(name)}(){ return getDoubleProperty(\"${key}\"); }";
            }
        })
    }

    public void booleanProperty(String propName, boolean propDefaultValue) {
        properties.add(new Property("${className}.${propName}", propName, "" + propDefaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(boolean set){ setProperty(\"${key}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public boolean get${toCamelCaseUpper(name)}(){ return getBooleanProperty(\"${key}\"); }";
            }
        })
    }

    public void intProperty(String propName, int propDefaultValue) {
        properties.add(new Property("${className}.${propName}", propName, "" + propDefaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(int set){ setProperty(\"${key}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public int get${toCamelCaseUpper(name)}(){ return getIntProperty(\"${key}\"); }";
            }
        })
    }

    public void longProperty(String propName, long propDefaultValue) {
        properties.add(new Property("${className}.${propName}", propName, "" + propDefaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(long set){ setProperty(\"${key}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public long get${toCamelCaseUpper(name)}(){ return getLongProperty(\"${key}\"); }";
            }
        })
    }

    public void dateProperty(String propName) {
        properties.add(new Property("${className}.${propName}", propName, "0") {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(java.util.Date set){ setProperty(\"${key}\", set != null ? set.getTime() : 0); }";
            }

            @Override
            String generateGetter() {
                return "public java.util.Date get${toCamelCaseUpper(name)}(){ return getDateProperty(\"${key}\"); }";
            }
        })
    }

    public void stringProperty(String propName, String propDefaultValue) {
        properties.add(new Property("${className}.${propName}", propName, propDefaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(String set){ setProperty(\"${key}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public String get${toCamelCaseUpper(name)}(){ return getStringProperty(\"${key}\"); }";
            }
        })
    }

    /**
     * enum型のPropertiesを生成する
     * @param propName
     * @param enumFullName
     * @param propDefaultValue
     */
    public void enumProperty(String propName, final String enumFullName, String propDefaultValue) {
        properties.add(new Property("${className}.${propName}", propName, propDefaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(${enumFullName} set){ setProperty(\"${key}\", set != null ? set.name() : \"\"); }";
            }

            @Override
            String generateGetter() {
                return "public ${enumFullName} get${toCamelCaseUpper(name)}(){ try{ return ${enumFullName}.valueOf(getStringProperty(\"${key}\")); }catch(Exception e){ return null; } }";
            }
        })
    }

    /**
     * JSON型のPropertiesを生成する
     * @param name プロパティ名
     * @param pojoFullName JSONの
     */
    public void jsonProperty(String propName, final String pojoFullName) {
        properties.add(new Property("${className}.${propName}", propName, "{}") {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(${pojoFullName} set){ setProperty(\"${key}\", com.eaglesakura.json.JSON.encodeOrNull(set)); }";
            }

            @Override
            String generateGetter() {
                return "public ${pojoFullName} get${toCamelCaseUpper(name)}(){ return getJsonProperty(\"${key}\", ${pojoFullName}.class); }";
            }
        })
    }

    /**
     * protocol buffersエンコードされたプロパティを追加する
     * @param propName
     * @param protobufFullName
     */
    public void protobufProperty(String propName, final String protobufFullName) {
        properties.add(new Property("${className}.${propName}", propName, "") {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(${protobufFullName} set){ setProperty(\"${key}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public ${protobufFullName} get${toCamelCaseUpper(name)}(){ return getProtobufProperty(\"${key}\", ${protobufFullName}.class); }";
            }
        })
    }

    /**
     * Bitmap用Propertyを追加する
     * @param propName
     */
    public void bitmapProperty(String propName) {
        properties.add(new Property("${className}.${propName}", propName, "") {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(android.graphics.Bitmap set){ setProperty(\"${key}\", set); }";
            }

            @Override
            String generateGetter() {
                return "public android.graphics.Bitmap get${toCamelCaseUpper(name)}(){ return getBitmapProperty(\"${key}\"); }";
            }
        })
    }

    public void build() {

        File srcRootDirectory = outDirectory;

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

        // プロパティIDを出力
        PROP_ID:
        {
            writer.newLine();
            for (def prop : properties) {
                writer.writeLine("public static final String ID_${prop.name.toUpperCase()} = \"${prop.key}\";");
            }
            writer.newLine();
        }

        // コンストラクタと初期化
        INIT:
        {
            writer.writeLine("public ${className}(Context context){ super(context, \"${dbFileName}\"); _initialize(); }");
            writer.writeLine("public ${className}(Context context, String dbFileName){ super(context, dbFileName); _initialize(); }")

            // 初期化メソッド
            writer.writeLine("protected void _initialize() {").pushIndent(true);
            writer.newLine();
            // Propertiesを出力する
            for (Property prop : properties) {
                writer.writeLine("addProperty(\"${prop.key}\", \"${prop.defaultValue}\");");
            }

            // 初期値のロードを行う
            if (autoPropLoad) {
                writer.newLine().writeLine("load();");
            }

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

    /**
     * 設定項目を指定する
     */
    static abstract class Property {
        final String defaultValue;

        final String key;

        final String name;

        Property(String key, String name, String defaultValue) {
            this.key = key;
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