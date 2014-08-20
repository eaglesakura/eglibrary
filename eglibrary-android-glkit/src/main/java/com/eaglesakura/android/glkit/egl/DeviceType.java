package com.eaglesakura.android.glkit.egl;

import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCField;

/**
 * EGLのデバイスの種類を確定する
 */
@JCClass(cppNamespace = "es.glkit")
public enum DeviceType {
    /**
     * 描画対象デバイス
     */
    Window,

    /**
     * オフスクリーンデバイス
     */
    Offscreen;

    @JCField
    public static final int DEVICE_TYPE_WINDOW = 0;

    @JCField
    public static final int DEVICE_TYPE_OFFSCREEN = 1;
}
