package com.eaglesakura.android.util;

import android.app.Fragment;
import android.os.Build;

import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.util.LogUtil;

public class FragmentUtil {

    /**
     * Child Fragmentに対応していればtrue
     * @return
     */
    public static boolean isSupportChildFragment() {
        return Build.VERSION.SDK_INT >= 17;
    }

    /**
     * デタッチする
     * @param fragment
     */
    public static void detatch(final Fragment fragment) {
        if (!AndroidUtil.isUIThread()) {
            UIHandler.postUI(new Runnable() {

                @Override
                public void run() {
                    detatch(fragment);
                }
            });
            return;
        }

        try {
            fragment.getFragmentManager().beginTransaction().remove(fragment).commit();
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }
}
