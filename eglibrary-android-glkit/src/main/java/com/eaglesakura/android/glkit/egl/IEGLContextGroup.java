package com.eaglesakura.android.glkit.egl;

import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCMethod;

/**
 * EGLグループ
 */
@JCClass(cppNamespace = "es.glkit")
public interface IEGLContextGroup {
    /**
     * 管理しているデバイス数
     */
    @JCMethod
    int getDeviceNum();
}
