package com.eaglesakura.android.util;

import android.app.Fragment;

import com.eaglesakura.andorid.thread.UIHandler;
import com.eaglesakura.util.LogUtil;

public class FragmentUtil {

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
