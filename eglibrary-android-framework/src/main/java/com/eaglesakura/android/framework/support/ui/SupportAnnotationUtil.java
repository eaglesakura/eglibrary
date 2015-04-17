package com.eaglesakura.android.framework.support.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.eaglesakura.android.annotations.AnnotationUtil;
import com.eaglesakura.util.LogUtil;

/**
 * Annotation Utils for v4
 */
public class SupportAnnotationUtil {

    /**
     * Annotation付きのフラグメントへ変換する
     *
     * @param clazz
     * @return
     */
    public static Fragment newFragment(String clazz) {
        try {
            if (!clazz.endsWith("_")) {
                clazz += "_";
            }

            return (Fragment) Class.forName(clazz).newInstance();
        } catch (Exception e) {
            LogUtil.d(e);
            return null;
        }
    }

    /**
     * Annotation付きのフラグメントへ変換する
     *
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Fragment> T newFragment(Class<T> clazz) {
        try {
            return (T) (AnnotationUtil.annotation(clazz).newInstance());
        } catch (Exception e) {
            LogUtil.d(e);
            return null;
        }
    }

    /**
     * Fragmentの生成とargment設定を行う
     *
     * @param clazz
     * @param argments
     * @param <T>
     * @return
     */
    public static <T extends Fragment> T newFragment(Class<T> clazz, Bundle argments) {
        T result = newFragment(clazz);
        if (result != null && argments != null) {
            result.setArguments(argments);
        }
        return result;
    }

}
