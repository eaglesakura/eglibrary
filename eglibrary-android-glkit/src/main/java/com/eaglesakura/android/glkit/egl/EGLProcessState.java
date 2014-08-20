package com.eaglesakura.android.glkit.egl;

import android.app.Activity;

import com.eaglesakura.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * プロセス単位でEGLの制御を行う
 */
public class EGLProcessState {
    /**
     * 管理しているデバイス数
     * <p/>
     * 0になったら自動的に廃棄させる
     */
    private static int deviceNum = 0;

    private static final Object lock = new Object();

    /**
     * 有効なEGLデバイス数を増加させる
     */
    public static boolean incrementDevice() {
        synchronized (lock) {
            ++deviceNum;

            if (deviceNum == 1) {
                LogUtil.log("EGLDevice == 1, request eglInitialize");
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 有効なEGLデバイス数を減らす
     * <p/>
     * デバイスが0になったらeglTerminateを行うべき
     */
    public static boolean decrementDevice() {
        synchronized (lock) {
            --deviceNum;
            if (deviceNum < 0) {
                throw new IllegalStateException(String.format("DeviceNum(%d) < 0", deviceNum));
            }

            if (deviceNum == 0) {
                LogUtil.log("EGLDevice == 0, request eglTerminate");
                return true;
            } else {
                return false;
            }
        }
    }


//    /**
//     * 初期化済みデバイスを保持しておき、もしオーナーが消えていたら解放を行う
//     */
//    static final List<EGLDeviceHolder> deviceHolders = new ArrayList<EGLDeviceHolder>();
//
//    static class EGLDeviceHolder {
//        String ownerName;
//
//        WeakReference<Object> owner;
//
//        IEGLDevice device;
//
//        boolean exist() {
//            return owner != null && owner.get() != null;
//        }
//    }
//
//    /**
//     * いつ死ぬかわからないデバイスをキャッシュに追加し、任意タイミングで解放するように保険をかける
//     *
//     * @param owner
//     * @param device
//     */
//    public static void addDeviceCache(Object owner, IEGLDevice device) {
//        synchronized (lock) {
//            EGLDeviceHolder holder = new EGLDeviceHolder();
//            holder.owner = new WeakReference<Object>(owner);
//            holder.ownerName = String.format("clz(%s)/ts(%s)", owner.getClass().getName(), owner.toString());
//            holder.device = device;
//        }
//    }
}
