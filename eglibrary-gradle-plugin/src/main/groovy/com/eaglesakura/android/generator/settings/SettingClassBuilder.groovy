package com.eaglesakura.android.generator.settings

/**
 * 設定クラスを自動出力する
 */
public class SettingClassBuilder {
    def classPackageName = "com.example";
    def className = "SampleSettingClass";

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

    public SettingClassBuilder() {
    }

    /**
     * 文字列プロパティを設定する
     * @param name
     */
    public void stringProperty(String name) {
        stringProperty(name, "null");
    }

    public void doubleProperty(final String name, final String defaultValue) {
        properties.add(new Property() {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(String set){ setProperty(${name}, set); }";
            }

            @Override
            String generateGetter() {
                return "public double get${toCamelCaseUpper(name)}(){ return getDoubleProperty(${name}); }";
            }
        })
    }

    public void longProperty(final String name, final String defaultValue) {
        properties.add(new Property() {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(String set){ setProperty(${name}, set); }";
            }

            @Override
            String generateGetter() {
                return "public long get${toCamelCaseUpper(name)}(){ return getLongProperty(${name}); }";
            }
        })
    }

    /**
     * 文字列プロパティを追加する
     */
    public void stringProperty(final String name, final String defaultValue) {
        properties.add(new Property(name, defaultValue) {
            @Override
            String generateSetter() {
                return "public void set${toCamelCaseUpper(name)}(String set){ setProperty(${name}, set); }";
            }

            @Override
            String generateGetter() {
                return "public String get${toCamelCaseUpper(name)}(){ return getStringProperty(${name}); }";
            }
        })
    }
}