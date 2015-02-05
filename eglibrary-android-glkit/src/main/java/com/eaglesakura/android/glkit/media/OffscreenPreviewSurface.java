package com.eaglesakura.android.glkit.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;

import com.eaglesakura.android.glkit.egl.EGLSpecRequest;
import com.eaglesakura.android.glkit.egl.GLESVersion;
import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.android.glkit.egl11.EGL11Manager;

import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class OffscreenPreviewSurface {

    final Context context;
    final EGL11Manager eglManager;
    IEGLDevice eglDevice = null;

    SurfaceTexture surfaceTexture;

    int previewTexture;

    public OffscreenPreviewSurface(Context context) {
        this.context = context;
        eglManager = new EGL11Manager(context);
    }

    /**
     * 初期化処理を行う
     */
    public SurfaceTexture initialize() {
        // EGL初期化する
        EGLSpecRequest eglSpecRequest = new EGLSpecRequest();
        eglSpecRequest.version = GLESVersion.GLES20;
        eglManager.initialize(eglSpecRequest);
        eglDevice = eglManager.newDevice(null);
        eglDevice.createPBufferSurface(1, 1);
        if (!eglDevice.bind()) {
            throw new IllegalStateException("EGL initialize failed");
        }


        this.previewTexture = genPreviewTexture();
        surfaceTexture = new SurfaceTexture(previewTexture);
        return surfaceTexture;
    }

    private static int genPreviewTexture() {
        int[] temp = new int[1];
        glGenTextures(1, temp, 0);
        int texture = temp[0];
        return texture;
    }

    /**
     * 開放処理を行う
     */
    public void dispose() {
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }

        if (previewTexture != 0) {
            glDeleteTextures(1, new int[]{previewTexture}, 0);
            previewTexture = 0;
        }

        if (eglDevice != null) {
            eglDevice.unbind();
            eglDevice.dispose();
            eglDevice = null;
        }
    }
}
