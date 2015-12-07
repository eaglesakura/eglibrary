package com.eaglesakura.android.annotations;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.view.View;

import com.eaglesakura.util.LogUtil;

/**
 * AndroidAnnotations系Util
 */
public class AnnotationUtil {
    /**
     * Annotationのコンバートクラスに変換する
     *
     * @param clazz
     *
     * @return
     */
    public static <T> Class<? extends T> annotation(Class<T> clazz) {
        try {
            // 最後が"_"で終わっていればAnnotation化する必要はない
            if (clazz.getName().endsWith("_")) {
                return clazz;
            }

            return (Class<? extends T>) Class.forName(clazz.getName() + "_");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Annotation付きのフラグメントへ変換する
     *
     * @param clazz
     *
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
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("NewApi")
    public static <T extends Fragment> T newFragment(Class<T> clazz) {
        try {
            return (annotation(clazz).newInstance());
        } catch (Exception e) {
            LogUtil.d(e);
            return null;
        }
    }


    /**
     * Annotation付きのViewへ変換する
     *
     * @param clazz
     * @param <T>
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T newView(Class<T> clazz, Context context) {
        try {

            return (T) (annotation(clazz).getConstructor(Context.class).newInstance(context));
        } catch (Exception e) {
            LogUtil.d(e);
            return null;
        }
    }
}
