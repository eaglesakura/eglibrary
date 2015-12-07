package com.eaglesakura.android.glkit;

import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.eaglesakura.util.LogUtil;

import javax.microedition.khronos.egl.EGL10;

/**
 * GLKit系Util
 */
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
        } else if (Build.VERSION.SDK_INT >= 14 && surface instanceof TextureView) {
            return new Surface(((TextureView) surface).getSurfaceTexture());
//            return surface;
        } else if (Build.VERSION.SDK_INT >= 14 && surface instanceof SurfaceTexture) {
            return new Surface((SurfaceTexture) surface);
//            return surface;
        } else if (surface instanceof Surface) {
            return surface;
        }

        // unsupported
        LogUtil.log("Unsupported Class(%s)", surface.getClass().getName());
        return null;
    }
}
