package com.eaglesakura.android.framework.support.ui.butterknife;

import android.content.Intent;
import android.support.v4.app.Fragment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * "@OnActivityResult"アノテーションを付与したメソッドを自動的に呼び出すためのUtil
 */
public class ActivityResult {

    private static boolean invoke(Method[] methods, Object sender, int requestCode, int resultCode, Intent data) {
        if (methods == null || methods.length == 0) {
            return false;
        }

        for (Method m : methods) {
            OnActivityResult onActivityResult = m.getAnnotation(OnActivityResult.class);
            if (onActivityResult != null && onActivityResult.value() == requestCode) {
                // ハンドリング対象のリクエストコードを見つけたら、それを呼び出す
                try {
                    m.setAccessible(true);
                    m.invoke(sender, resultCode, data);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        // Fragmentの場合は、子を巡ってハンドリング対象を探す
        if (sender instanceof Fragment) {
            List<Fragment> fragments = ((Fragment) sender).getChildFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment child : fragments) {
                    if (invoke(child, requestCode, resultCode, data)) {
                        // 子がハンドリングに成功した
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * onActivityResultを呼び出す
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return ハンドリングを行ったらtrue
     */
    public static boolean invoke(Object sender, int requestCode, int resultCode, Intent data) {

        Class clazz = sender.getClass();

        // publicなメソッドでコールできればそれで良い
        if (invoke(clazz.getMethods(), sender, requestCode, resultCode, data)) {
            return true;
        }

        // 定義を遡って適当なメソッドを探す
        while (!clazz.equals(Object.class)) {
            if (invoke(clazz.getDeclaredMethods(), sender, requestCode, resultCode, data)) {
                return true;
            }

            clazz = clazz.getSuperclass();
        }
        return false;
    }

}