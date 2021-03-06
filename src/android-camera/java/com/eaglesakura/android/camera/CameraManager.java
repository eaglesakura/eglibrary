package com.eaglesakura.android.camera;

import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.math.MathUtil;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import java.util.List;

/**
 * カメラハードウェアの管理クラス
 */
public class CameraManager implements Camera.AutoFocusCallback {
    final Context context;

    final Object lock = new Object();

    /**
     * 接続したカメラハードウェア
     */
    Camera camera;

    /**
     * param
     */
    Camera.Parameters parameters;


    /**
     * 接続されているカメラタイプ
     */
    CameraType connectedCamera;

    /**
     * 回転タイプ
     */
    OrientationSpec orientation = OrientationSpec.ROTATE_0;

    /**
     * オートフォーカス実行モード
     */
    AutofucusState autofucusState = AutofucusState.None;

    /**
     * シーン設定
     */
    SceneSpec scene = SceneSpec.SETTING_AUTO;

    FlashModeSpec flashMode = FlashModeSpec.SETTING_AUTO;

    WhiteBaranceSpec whiteBarance = WhiteBaranceSpec.SETTING_AUTO;

    FocusModeSpec focusMode = FocusModeSpec.SETTING_AUTO;

    /**
     * カメラ性能リスト
     */
    CameraSpec specs;

    /**
     * オートフォーカス状態
     */
    public enum AutofucusState {
        /**
         * 未実行
         */
        None,

        /**
         * オートフォーカス動作中
         */
        Processing,

        /**
         * オートフォーカス失敗
         */
        Failed,

        /**
         * オートフォーカス成功
         */
        Completed;
    }

    public CameraManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * カメラの回転角を取得する
     *
     * @param spec 回転タイプ
     */
    public boolean requestOrientation(OrientationSpec spec) {
        try {
            // 回転角を設定する
            camera.setDisplayOrientation(spec.getDegree());
            this.orientation = spec;
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    /**
     * 手ぶれ補正の有効化を行わせる
     * <br>
     * この機能はプレビュー時及びビデオ撮影時のみに有効となる
     *
     * @param enable true=有効、false=無効
     * @return 切り替えに成功したらtrue
     */
    @SuppressLint("NewApi")
    public boolean requestStabilization(boolean enable) {
        if (Build.VERSION.SDK_INT < 15) {
            return false;
        }

        try {
            if (parameters.isVideoStabilizationSupported()) {
                parameters.setVideoStabilization(enable);
                camera.setParameters(parameters);
                return true;
            }
            LogUtil.log("not support Stabilization");
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
        }
        return false;
    }

    /**
     * シーンモードを設定する
     */
    public boolean requestScene(SceneSpec spec) {
        try {
            parameters.setSceneMode(spec.getApiSettingName());
            camera.setParameters(parameters);
            this.scene = spec;
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
        }
        return false;
    }

    private Camera.Size chooseShotSize(List<Camera.Size> targetSizes, int width, int height, int minWidth, int minHeight) {

        final float reqLargeValue = Math.max(width, height);
        final float reqSmallValue = Math.min(width, height);
        final float lowerSizeLarge = Math.max(minWidth, minHeight);
        final float lowerSizeSmall = Math.min(minWidth, minHeight);

        final float TARGET_ASPECT = Math.max(1, reqLargeValue) / Math.max(1, reqSmallValue);

        try {
            Camera.Size target = null;
            float current_diff = 999999999;

            for (Camera.Size size : targetSizes) {
                final float checkLargeValue = Math.max(size.width, size.height);
                final float checkSmallValue = Math.min(size.width, size.height);

                // 最低限のサイズは保つ
                if (checkLargeValue >= lowerSizeLarge && checkSmallValue >= lowerSizeSmall) {
                    float aspect_diff = (checkLargeValue / checkSmallValue) - TARGET_ASPECT;

                    // アスペクト比の差分が小さい＝近い構成をコピーする
                    // 基本的に奥へ行くほど解像度が低いため、最低限の要求を満たせる解像度を探す
                    if (Math.abs(aspect_diff) <= current_diff) {
                        target = size;
                        current_diff = aspect_diff;
                    }
                }
            }

            if (target != null) {
                return target;
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return targetSizes.get(0);
    }

    /**
     * 指定したアスペクト比に近いプレビューサイズを選択する
     *
     * @param width     リクエストする幅
     * @param height    リクエストする高さ
     * @param minWidth  最低限持っていて欲しい幅
     * @param minHeight 最低限持っていていて欲しい高さ
     */
    public void requestPreviewSize(int width, int height, int minWidth, int minHeight) {
        Camera.Size previewSize = chooseShotSize(parameters.getSupportedPreviewSizes(), width, height, minWidth, minHeight);
        try {
            Camera.Size oldSize = parameters.getPreviewSize();
            if (oldSize.width != previewSize.width || oldSize.height != previewSize.height) {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                camera.setParameters(parameters);
                LogUtil.log("previewSize old(%d x %d) / request(%d x %d) -> set(%d x %d) no-frip", oldSize.width, oldSize.height, width, height, previewSize.width, previewSize.height);
            }
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
        }
    }

    /**
     * GPSデータを設定する
     *
     * @param lat 緯度
     * @param lng 経度
     */
    public void setGpsData(double lat, double lng) {
        try {
            parameters.setGpsLatitude(lat);
            parameters.setGpsLongitude(lng);
            camera.setParameters(parameters);
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
        }
    }

    /**
     * JPEG画質を設定する
     *
     * @param quality 画質(0〜100)
     */
    public void setJpegQuality(int quality) {
        try {
            parameters.setJpegQuality(MathUtil.minmax(0, 100, quality));
            camera.setParameters(parameters);
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
        }
    }

    /**
     * 現在のズーム値を取得する
     */
    public int getZoom() {
        return parameters.getZoom();
    }

    /**
     * ズームの最大値を取得する
     */
    public int getMaxZoom() {
        return parameters.getMaxZoom();
    }

    /**
     * ズームレベルを指定する
     */
    public boolean requestZoom(int zoom) {
        try {
            parameters.setZoom(Math.min(zoom, getMaxZoom()));
            camera.setParameters(parameters);
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
            return false;
        }
    }

    /**
     * 撮影時のフラッシュモードを指定する
     *
     * @param spec フラッシュモード
     */
    public boolean requestFlashMode(FlashModeSpec spec) {
        try {
            parameters.setFlashMode(spec.getApiSettingName());
            camera.setParameters(parameters);
            flashMode = spec;
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
            return false;
        }
    }

    /**
     * フォーカスモード設定
     */
    public boolean requestFocusMode(FocusModeSpec spec) {
        try {
            parameters.setFocusMode(spec.getApiSettingName());
            camera.setParameters(parameters);
            focusMode = spec;
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
            return false;
        }
    }

    /**
     * ホワイトバランス設定
     *
     * @param spec ホワイトバランス設定
     */
    public boolean requestWhiteBarance(WhiteBaranceSpec spec) {
        try {
            parameters.setWhiteBalance(spec.getApiSettingName());
            camera.setParameters(parameters);
            whiteBarance = spec;
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
            return false;
        }
    }

    /**
     * 指定したアスペクト比に近い撮影サイズを選択する
     *
     * @param width     リクエストする幅
     * @param height    リクエストする高さ
     * @param minWidth  最低限持っていて欲しい幅
     * @param minHeight 最低限持っていていて欲しい高さ
     */
    public void requestPictureSize(int width, int height, int minWidth, int minHeight) {
        Camera.Size pictureSize = chooseShotSize(parameters.getSupportedPictureSizes(), width, height, minWidth, minHeight);
        try {
            Camera.Size currentSize = parameters.getPreviewSize();
            if (currentSize.width != pictureSize.width || currentSize.height != pictureSize.height) {
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
                camera.setParameters(parameters);
                LogUtil.log("previewSize request(%d x %d) -> set(%d x %d) no-frip", width, height, pictureSize.width, pictureSize.height);
            } else {
                LogUtil.log("previewSize eq request(%d x %d) -> set(%d x %d) no-frip", width, height, pictureSize.width, pictureSize.height);
            }
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
        }
    }

    /**
     * 撮影サイズIDを指定する
     */
    public void setPictureSize(String pictureSizeId) {
        Camera.Size pictureSize = specs.getShotSize(pictureSizeId).getCameraSize();
        try {
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            camera.setParameters(parameters);
            LogUtil.log("previewSize id(%s) request(%d x %d) no-frip", pictureSizeId, pictureSize.width, pictureSize.height);
        } catch (Exception e) {
            LogUtil.log(e);
            parameters = camera.getParameters();
        }
    }

    /**
     * @return
     */
    public boolean isConnected() {
        synchronized (lock) {
            return camera != null;
        }
    }

    /**
     * デバイスの回転角にプレビュー角度を合わせる
     */
    public void requestPreviewRotateLinkDevice() {
        int deviceRotateDegree = ContextUtil.getDeviceRotateDegree(context);

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(specs.getCameraNumber(), info);
        int cameraDegree = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraDegree = (info.orientation + deviceRotateDegree) % 360;
            cameraDegree = (360 - cameraDegree) % 360;  // compensate the mirror
        } else {  // back-facing
            cameraDegree = (info.orientation - deviceRotateDegree + 360) % 360;
        }
        camera.setDisplayOrientation(cameraDegree);
    }

    /**
     * カメラに接続する
     */
    public boolean connect(CameraType type) {
        if (!isConnected()) {
            disconnect();
        }

        synchronized (lock) {
            try {
                camera = Camera.open(type.getCameraNumber());
                parameters = camera.getParameters();
                this.connectedCamera = type;
                if (isConnected()) {
                    // jpeg quality
                    setJpegQuality(100);

                    // スペックを切り出す
                    specs = new CameraSpec(type, camera);

                    // 回転を設定する
                    requestPreviewRotateLinkDevice();
                    return true;
                }
            } catch (Exception e) {
                LogUtil.log(e);
            }
            camera = null;
            parameters = null;
            connectedCamera = null;
        }
        return false;
    }

    /**
     * 指定サーフェイスに対してプレビューを開始する
     */
    @SuppressLint("NewApi")
    public boolean startPreview(Object surface) {
        try {
            if (AndroidUtil.isTextureView(surface)) {
                surface = ((TextureView) surface).getSurfaceTexture();
            } else if (surface instanceof SurfaceView) {
                surface = ((SurfaceView) surface).getHolder();
            }

            if (surface instanceof SurfaceHolder) {
                camera.setPreviewDisplay((SurfaceHolder) surface);
            } else if (AndroidUtil.isSurfaceTexture(surface)) {
                camera.setPreviewTexture((SurfaceTexture) surface);
            } else {
                LogUtil.log("not support preview(%s)", surface.getClass().getName());
                // not support
                return false;
            }

            camera.startPreview();
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return false;
    }

    /**
     * プレビューを停止する
     */
    public boolean stopPreview() {
        try {
            camera.setPreviewCallback(null);
            camera.setOneShotPreviewCallback(null);
            camera.stopPreview();
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return false;
    }

    /**
     * カメラから切断する
     */
    public void disconnect() {
        if (!isConnected()) {
            return;
        }

        synchronized (lock) {
            stopPreview();
            camera.release();
            parameters = null;
            camera = null;
        }
    }

    /**
     * オートフォーカスが実行された
     *
     * @param success 成功状態
     * @param camera  カメラ
     */
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            autofucusState = AutofucusState.Completed;
        } else {
            autofucusState = AutofucusState.Failed;
        }
    }

    public CameraSpec getSpecs() {
        return specs;
    }

    /**
     * 現在のオートフォーカス状態を取得する
     */
    public AutofucusState getAutofucusState() {
        return autofucusState;
    }

    /**
     * オートフォーカスの処理中であればtrue
     */
    public boolean isAutofocusProcessing() {
        return autofucusState == AutofucusState.Processing;
    }

    /**
     * 同期的にオートフォーカスを行う
     *
     * @return オートフォーカスに成功したらtrue
     */
    public boolean autofocusSync() {
        final Holder<Boolean> holder = new Holder<Boolean>();
        try {
            camera.cancelAutoFocus();
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        autofucusState = AutofucusState.Completed;
                    } else {
                        autofucusState = AutofucusState.Failed;
                    }
                    LogUtil.log("autofocus :: " + autofucusState);
                    holder.set(success);
                }
            });
            autofucusState = AutofucusState.Processing;
        } catch (Exception e) {
            holder.set(false);
        }
        return holder.getWithWait(1000 * 10);
    }

    /**
     * オートフォーカスを開始する
     */
    public boolean startAutofocus() {
        synchronized (lock) {
            try {
                if (autofucusState == AutofucusState.Processing) {
                    return true;
                }

                autofucusState = AutofucusState.Processing;
                camera.autoFocus(this);
                return true;
            } catch (Exception e) {
                LogUtil.log(e);
            }
            return false;
        }
    }

    /**
     * オートフォーカス処理に失敗していたらtrue
     */
    public boolean isAutofocusFailed() {
        return autofucusState == AutofucusState.Failed;
    }

    /**
     * オートフォーカス処理に成功していたらtrue
     */
    public boolean isAutofocusCompleted() {
        return autofucusState == AutofucusState.Completed;
    }

    public Camera getCamera() {
        return camera;
    }


    /**
     * カメラの撮影待ちを行う
     *
     * @return 撮影したJpegデータ
     */
    public byte[] takePictureSync() {
        final Holder<byte[]> pictureHolder = new Holder<byte[]>();
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                pictureHolder.set(data);
            }
        });
        return pictureHolder.getWithWait(1000 * 30);
    }


    /**
     * オートフォーカス処理をキャンセルする
     */
    public boolean cancelAutofocus() {
        synchronized (lock) {
            if (!isAutofocusProcessing()) {
                return true;
            }

            try {
                autofucusState = AutofucusState.None;
                camera.cancelAutoFocus();
                return true;
            } catch (Exception e) {
                LogUtil.log(e);
            }
            return false;
        }
    }

    /**
     * カメラを縦方向に持っている場合はtrue
     */
    public boolean isVerticalMode() {
        return orientation.isVertical();
    }

    /**
     * カメラを水平に持っている場合はtrue
     */
    public boolean isHorizontalMode() {
        return orientation.isHorizontal();
    }

    /**
     * プレビュー幅を取得する
     */
    public int getPreviewWidth() {
        return parameters.getPreviewSize().width;
    }

    /**
     * プレビュー高さを取得する
     */
    public int getPreviewHeight() {
        return parameters.getPreviewSize().height;
    }

    /**
     * 通知一覧
     */
    public interface CameraManagerListener {
        /**
         * オートフォーカス状態が変更された
         */
        void onAutoFocus(boolean success, CameraManager camera);
    }

    /**
     * カメラスペックを取得する
     */
    public static CameraSpec loadCameraSpec(Context context, CameraType type) {
        CameraManager cameraManager = new CameraManager(context);
        try {
            if (cameraManager.connect(type)) {
                return cameraManager.getSpecs();
            }
        } finally {
            cameraManager.disconnect();
        }
        return null;
    }
}
