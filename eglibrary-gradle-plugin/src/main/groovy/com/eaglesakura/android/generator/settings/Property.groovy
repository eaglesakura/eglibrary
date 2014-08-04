package com.eaglesakura.android.generator.settings

/**
 * 設定項目を指定する
 */
public abstract class Property {
    final String defaultValue;

    final String name;

    public Property(String name, String defaultValue) {
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