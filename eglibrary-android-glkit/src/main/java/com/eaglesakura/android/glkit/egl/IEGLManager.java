package com.eaglesakura.android.glkit.egl;

import android.content.Context;

import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCMethod;

/**
 * EGL操作を抽象化するクラス
 * <br>
 * EGL操作にはウィンドウサーフェイスが必須ではなく、ウィンドウサーフェイス無しで動作することも許可する。
 */
@JCClass(cppNamespace = "es.glkit")
public interface IEGLManager {

    /**
     * @return サポートしているEGLバージョンを取得する。[0]にメジャー、[1]にマイナーバージョンを格納する
     */
    int[] getSupportedEglVersion();

    /**
     * 初期化を行わせる
     *
     * @param request
     */
    void initialize(EGLSpecRequest request);

    /**
     * 新たにEGL初期化済みデバイスを生成する
     *
     * @return　生成したデバイス
     */
    @JCMethod
    IEGLDevice newDevice(IEGLContextGroup contextGroup);
}
