package com.eaglesakura.android.util;

import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * View系の便利メソッド
 */
public class ViewUtil {

    /**
     * CardView配下のitemを横幅に合わせてマッチングする
     *
     * @param itemView
     */
    public static void matchCardWidth(View itemView) {
        if (itemView.getLayoutParams() == null) {
            itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    /**
     * カーソル位置にテキストを挿入する
     *
     * @param editText
     * @param text
     */
    public static void insertTextInCursor(EditText editText, String text) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Editable editable = editText.getText();
        editable.replace(Math.min(start, end), Math.max(start, end), text);
    }

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
