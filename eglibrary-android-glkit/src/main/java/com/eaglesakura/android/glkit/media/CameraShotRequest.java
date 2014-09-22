package com.eaglesakura.android.glkit.media;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.eaglesakura.android.camera.CameraManager;
import com.eaglesakura.android.glkit.egl.EGLSpecRequest;
import com.eaglesakura.android.glkit.egl.GLESVersion;
import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.android.glkit.egl11.EGL11Manager;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.math.Vector2;
import com.eaglesakura.util.LogUtil;

import static android.opengl.GLES20.*;

/**
 * 撮影時の条件を列挙する
 */
public class CameraShotRequest {
    /**
     * JPEG圧縮率
     */
    int jpegQuality = 100;

    /**
     * Flash指定
     */
    CameraManager.FlashMode flashMode = CameraManager.FlashMode.Auto;

    /**
     * オートフォーカス
     */
    boolean autoFocus = true;

    /**
     * カメラの指定
     */
    CameraManager.CameraType cameraType = CameraManager.CameraType.Main;

    /**
     * リクエストする撮影サイズ
     */
    Vector2 pictureSize = new Vector2(19000, 16000);

    /**
     * GPS座標
     */
    double[] gps;

    /**
     * シーン設定
     */
    CameraManager.SceneMode sceneMode = CameraManager.SceneMode.Auto;

    public CameraShotRequest jpegQuality(int jpegQuality) {
        this.jpegQuality = jpegQuality;
        return this;
    }

    public CameraShotRequest flashMode(CameraManager.FlashMode mode) {
        this.flashMode = mode;
        return this;
    }

    public CameraShotRequest autoFocus(boolean enable) {
        this.autoFocus = enable;
        return this;
    }

    public CameraShotRequest cameraType(CameraManager.CameraType type) {
        this.cameraType = type;
        return this;
    }

    public CameraShotRequest pictureSize(int width, int height) {
        pictureSize.set(width, height);
        return this;
    }

    public CameraShotRequest gps(double lat, double lng) {
        gps = new double[]{lat, lng};
        return this;
    }

    /**
     * シーン設定
     *
     * @param sceneMode
     */
    public void sceneMode(CameraManager.SceneMode sceneMode) {
        this.sceneMode = sceneMode;
    }

    private static int genPreviewTexture() {
        int[] temp = new int[1];
        glGenTextures(1, temp, 0);
        int texture = temp[0];
        return texture;
    }

    private static void deletePreviewTexture(int texture) {
        glDeleteTextures(1, new int[]{texture}, 0);
    }

    /**
     * 同期的にカメラで撮影を行う。
     * <p/>
     * UIThreadからは呼び出してはいけない
     *
     * @param request
     * @return
     */
    public static byte[] takePictureSync(Context context, CameraShotRequest request) {
        if (AndroidUtil.isUIThread()) {
            throw new IllegalStateException("call Background!!");
        }

        CameraManager cameraManager = null;
        IEGLDevice eglDevice = null;

        try {
            cameraManager = new CameraManager(context);
            if (cameraManager.connect(request.cameraType)) {
                LogUtil.log("Camera Connect(%s) complete", request.cameraType);
            } else {
                LogUtil.log("Camera Connect(%s) fail", request.cameraType);
                return null;
            }

            cameraManager.setJpegQuality(request.jpegQuality);
            LogUtil.log("set Jpet Quality(%d)", request.jpegQuality);

            if (cameraManager.requestFlashMode(request.flashMode)) {
                LogUtil.log("Flash Mode(%s) complete", request.flashMode);
            } else {
                LogUtil.log("Flash Mode(%s) fail", request.flashMode);
            }

            if (cameraManager.requestScene(request.sceneMode)) {
                LogUtil.log("Scene Mode(%s) complete", request.sceneMode);
            } else {
                LogUtil.log("Scene Mode(%s) fail", request.sceneMode);
            }

            // pic size
            cameraManager.requestPictureSize((int) request.pictureSize.x, (int) request.pictureSize.y, (int) request.pictureSize.x / 2, (int) request.pictureSize.y / 2);

            if (request.gps != null) {
                cameraManager.setGpsData(request.gps[0], request.gps[1]);
            }

            // EGL初期化する
            final EGL11Manager eglManager = new EGL11Manager(context);
            EGLSpecRequest eglSpecRequest = new EGLSpecRequest();
            eglSpecRequest.version = GLESVersion.GLES20;
            eglManager.initialize(eglSpecRequest);
            eglDevice = eglManager.newDevice(null);
            eglDevice.createPBufferSurface(1, 1);
            if (!eglDevice.bind()) {
                throw new IllegalStateException("EGL initialize failed");
            }

            int texture = genPreviewTexture();
            SurfaceTexture surfaceTexture = new SurfaceTexture(texture);

            cameraManager.startPreview(surfaceTexture);
            if (request.autoFocus) {
                boolean completed = cameraManager.autofocusSync();
                LogUtil.log("autofocus :: " + completed);
            }

            // 撮影
            byte[] jpeg = cameraManager.takePictureSync();
            LogUtil.log("capture Jpeg size(%.1f MB)", (float) jpeg.length / 1024.0f / 1024.0f);

            surfaceTexture.release();
            deletePreviewTexture(texture);

            return jpeg;
        } catch (Exception e) {

        } finally {
            if (cameraManager != null) {
                cameraManager.disconnect();
            }
            if (eglDevice != null) {
                eglDevice.unbind();
                eglDevice.dispose();
            }
        }
        return null;
    }
}
