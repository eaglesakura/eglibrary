package com.eaglesakura.android.glkit;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.android.glkit.egl.IEGLManager;
import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCMethod;
import com.eaglesakura.util.LogUtil;

import javax.microedition.khronos.egl.EGL10;

/**
 * GLKit系Util
 */
@JCClass(cppNamespace = "es.glkit")
public class GLKitUtil {

    /**
     * EGL14に対応している場合はtrue
     */
    public static boolean supportEGL14() {
        return Build.VERSION.SDK_INT >= 17; // for 4.2
    }

    /**
     * OpenGL ES 3.0対応ならばtrue
     */
    public static boolean supportGLES30() {
        return Build.VERSION.SDK_INT >= 18; // for 4.3
    }

    /**
     * OpenGL ES 3.1対応ならばtrue
     */
    public static boolean supportGLES31() {
        return Build.VERSION.SDK_INT >= 20; // for L
    }

    /**
     * EGLのエラーを出力する
     */
    public static void printEglError(int error) {
        if (error == EGL10.EGL_SUCCESS) {
            return;
        }

        String message = String.format("EGLError(%x)", error);
        switch (error) {
            case EGL10.EGL_BAD_ALLOC:
                message = "EGL_BAD_ALLOC";
                break;
            case EGL10.EGL_BAD_CONFIG:
                message = "EGL_BAD_CONFIG";
                break;
            case EGL10.EGL_BAD_ACCESS:
                message = "EGL_BAD_ACCESS";
                break;
            case EGL10.EGL_BAD_ATTRIBUTE:
                message = "EGL_BAD_ATTRIBUTE";
                break;
            case EGL10.EGL_BAD_DISPLAY:
                message = "EGL_BAD_DISPLAY";
                break;
        }

        LogUtil.log(message);
    }

    /**
     * 引数のオブジェクトからEGLSurfaceを取得可能なNativeWindowオブジェクトを取り出す
     *
     * @param surface
     * @return
     */
    public static Object getEglNativeWindow(Object surface) {
        if (surface instanceof SurfaceView) {
            return ((SurfaceView) surface).getHolder();
        } else if (surface instanceof SurfaceHolder) {
            return surface;
        } else if (surface instanceof TextureView) {
            return ((TextureView) surface).getSurfaceTexture();
        } else if (surface instanceof SurfaceTexture) {
            return surface;
        }

        // unsupported
        LogUtil.log("Unsupported Class(%s)", surface.getClass().getName());
        return null;
    }

    /**
     * デバイスにレンダリングされている内容をキャプチャする
     * <p/>
     * 呼び出し時点でdeviceがこのThreadにバインドされていなければならない。
     * <p/>
     * キャプチャは必ずARGB8888で行われる。
     *
     * @param device キャプチャ対象のデバイス
     * @return キャプチャしたBitmap
     */
    public static Bitmap captureDevice(IEGLDevice device) {
        return captureDevice(device, Bitmap.Config.ARGB_8888);
    }

    /**
     * デバイスにレンダリングされている内容をキャプチャする
     * <p/>
     * 呼び出し時点でdeviceがこのThreadにバインドされていなければならない。
     * <p/>
     * 新たにBitmapを生成するため処理は重い。
     *
     * @param device キャプチャ対象のデバイス
     * @return キャプチャしたBitmap
     */
    public static Bitmap captureDevice(IEGLDevice device, Bitmap.Config config) {
        Bitmap screen = Bitmap.createBitmap(device.getSurfaceWidth(), device.getSurfaceHeight(), config);
        return captureDevice(device, screen);
    }

    /**
     * デバイスにレンダリングされている内容をdstキャプチャする。
     *
     * @param device キャプチャ対象デバイス
     * @param dst    キャプチャしたピクセル内容の格納先
     * @return
     */
    public static Bitmap captureDevice(IEGLDevice device, Bitmap dst) {
        if (!device.isBindedThread()) {
            throw new IllegalStateException("!device.isBindedThread()");
        }

        if (!nativeCaptureDevice(device, dst)) {
            // 何らかの原因でキャプチャが失敗した
            throw new IllegalStateException("nativeCaptureDevice(device, dst)");
        }

        return dst;
    }

    /**
     * 速度優先のため、Nativeでキャプチャ操作を行う
     *
     * @param device キャプチャ対象デバイス
     * @param dst    格納先
     * @return 成功したらtrue
     */
    @JCMethod
    static native boolean nativeCaptureDevice(IEGLDevice device, Bitmap dst);
}
