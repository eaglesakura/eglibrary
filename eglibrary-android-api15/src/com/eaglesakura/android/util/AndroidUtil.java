package com.eaglesakura.android.util;

import android.os.Looper;

public class AndroidUtil {

    /**
     * UIスレッドだったらtrueを返す。
     * 
     * @return
     */
    public static boolean isUIThread() {
        return Thread.currentThread().equals(Looper.getMainLooper().getThread());
    }

    /**
     * UIスレッドでなければ例外を投げる。
     */
    public static void assertUIThread() {
        if (!isUIThread()) {
            throw new IllegalStateException("is not ui thread!!");
        }
    }

}
