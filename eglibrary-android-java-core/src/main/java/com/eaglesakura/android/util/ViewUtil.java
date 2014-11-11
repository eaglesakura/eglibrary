package com.eaglesakura.android.util;

import android.text.InputType;
import android.widget.EditText;

/**
 * View系の便利メソッド
 */
public class ViewUtil {

    /**
     * 入力を整数に限るようにする
     *
     * @param editText
     */
    public static void setInputIntegerOnly(EditText editText) {
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    /**
     * 入力を数値に限るようにする
     *
     * @param editText
     */
    public static void setInputDecimal(EditText editText) {
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    /**
     * 実数を取得する
     *
     * @param text
     * @param defValue
     * @return
     */
    public static double getDoubleValue(EditText text, double defValue) {
        try {
            return Double.valueOf(text.getText().toString());
        } catch (Exception e) {
            return defValue;
        }
    }

    /**
     * 整数を取得する
     *
     * @param text
     * @param defValue
     * @return
     */
    public static long getLongValue(EditText text, long defValue) {
        try {
            return Long.valueOf(text.getText().toString());
        } catch (Exception e) {
            return defValue;
        }
    }
}
