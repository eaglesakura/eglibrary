package com.eaglesakura.android.glkit.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.eaglesakura.android.camera.CameraManager;
import com.eaglesakura.android.camera.CameraSpec;
import com.eaglesakura.android.camera.CameraType;
import com.eaglesakura.android.camera.FlashModeSpec;
import com.eaglesakura.android.camera.FocusModeSpec;
import com.eaglesakura.android.camera.SceneSpec;
import com.eaglesakura.android.camera.WhiteBaranceSpec;
import com.eaglesakura.android.glkit.egl.EGLSpecRequest;
import com.eaglesakura.android.glkit.egl.GLESVersion;
import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.android.glkit.egl11.EGL11Manager;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.math.Vector2;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

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
     * カメラ起動からオートフォーカスまでの猶予時間
     */
    long cameraSleepTimeMs = 300;

    /**
     * フラッシュは自動
     */
    FlashModeSpec flashMode = FlashModeSpec.SETTING_AUTO;

    /**
     * オートフォーカス
     */
    boolean autoFocus = true;

    /**
     * オートフォーカスのリトライ試行回数
     */
    int autoFocusRetry = 10;

    /**
     * カメラの指定
     */
    CameraType cameraType = CameraType.TYPE_MAIN;

    /**
     * 撮影サイズID
     */
    String shotSizeId = null;

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
    SceneSpec sceneSpec = SceneSpec.SETTING_AUTO;

    /**
     * フォーカスモード指定
     */
    FocusModeSpec focusModeSpec = FocusModeSpec.SETTING_AUTO;

    /**
     * ホワイトバランス指定
     */
    WhiteBaranceSpec whiteBaranceSpec = WhiteBaranceSpec.SETTING_AUTO;

    public CameraShotRequest jpegQuality(int jpegQuality) {
        this.jpegQuality = jpegQuality;
        return this;
    }

    public CameraShotRequest flashMode(FlashModeSpec mode) {
        this.flashMode = mode;
        return this;
    }

    public CameraShotRequest autoFocus(boolean enable) {
        this.autoFocus = enable;
        return this;
    }

    public CameraShotRequest cameraType(CameraType type) {
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
    public CameraShotRequest scene(SceneSpec sceneMode) {
        this.sceneSpec = sceneMode;
        return this;
    }

    /**
     * 撮影サイズのIDを設定する
     *
     * @param shotSizeId
     */
    public CameraShotRequest shotSizeId(String shotSizeId) {
        this.shotSizeId = shotSizeId;
        return this;
    }

    /**
     * 同期的にカメラで撮影を行う。
     * <p/>
     * UIThreadからは呼び出してはいけない。
     *
     * @param request
     * @return 撮影されたJpeg画像
     */
    @SuppressLint("NewApi")
    public static byte[] takePictureSync(final Context context, CameraShotRequest request) {
        if (AndroidUtil.isUIThread()) {
            throw new IllegalStateException("call Background!!");
        }

        OffscreenPreviewSurface surface = new OffscreenPreviewSurface(context);
        CameraManager cameraManager = null;

        try {
            cameraManager = new CameraManager(context);
            if (cameraManager.connect(request.cameraType)) {
                LogUtil.log("Camera Connect(%s) complete", request.cameraType);
            } else {
                LogUtil.log("Camera Connect(%s) fail", request.cameraType);
                return null;
            }

            cameraManager.setJpegQuality(request.jpegQuality);
            LogUtil.log("set Jpeg Quality(%d)", request.jpegQuality);

            if (cameraManager.requestScene(request.sceneSpec)) {
                LogUtil.log("Scene Mode(%s) complete", request.sceneSpec.getApiSettingName());
            } else {
                LogUtil.log("Scene Mode(%s) fail", request.sceneSpec.getApiSettingName());
            }

            if (cameraManager.requestFlashMode(request.flashMode)) {
                LogUtil.log("Flash Mode(%s) complete", request.flashMode.getApiSettingName());
            } else {
                LogUtil.log("Flash Mode(%s) fail", request.flashMode.getApiSettingName());
            }

            if (cameraManager.requestFocusMode(request.focusModeSpec)) {
                LogUtil.log("Focus Mode(%s) complete", request.focusModeSpec.getApiSettingName());
            } else {
                LogUtil.log("Focus Mode(%s) fail", request.focusModeSpec.getApiSettingName());
            }

            if (cameraManager.requestWhiteBarance(request.whiteBaranceSpec)) {
                LogUtil.log("WhiteBarance Mode(%s) complete", request.whiteBaranceSpec.getApiSettingName());
            } else {
                LogUtil.log("WhiteBarance Mode(%s) fail", request.whiteBaranceSpec.getApiSettingName());
            }

            // pic size
            if (!StringUtil.isEmpty(request.shotSizeId)) {
                cameraManager.setPictureSize(request.shotSizeId);
            } else {
                cameraManager.requestPictureSize((int) request.pictureSize.x, (int) request.pictureSize.y, (int) request.pictureSize.x / 2, (int) request.pictureSize.y / 2);
            }

            if (request.gps != null) {
                cameraManager.setGpsData(request.gps[0], request.gps[1]);
            }

            Object sur = surface.createSurface();
            cameraManager.startPreview(sur);

            if (request.autoFocus) {
                int autoFocusRetry = request.autoFocusRetry;
                boolean autofucsCompleted = false;
                while (autoFocusRetry > 0 && !autofucsCompleted) {
                    if (request.cameraSleepTimeMs > 0) {
                        Thread.sleep(request.cameraSleepTimeMs);
                    }

                    autofucsCompleted = cameraManager.autofocusSync();
                    LogUtil.log("autofocus :: " + autofucsCompleted);
                    --autoFocusRetry;
                }
                LogUtil.log("autofocus finished :: " + autofucsCompleted);
            }

            // 撮影
            byte[] jpeg = cameraManager.takePictureSync();
            LogUtil.log("capture Jpeg size(%.1f MB)", (float) jpeg.length / 1024.0f / 1024.0f);
            return jpeg;
        } catch (Exception e) {
            LogUtil.log(e);
        } finally {
            if (cameraManager != null) {
                cameraManager.disconnect();
            }
            if (surface != null) {
                surface.dispose();
            }
        }
        return null;
    }
}
