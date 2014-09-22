package com.eaglesakura.android.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.math.MathUtil;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    CameraType connectedCameraType = null;

    /**
     * 回転タイプ
     */
    OrientationType orientationType = OrientationType.Rotate0;

    /**
     * オートフォーカス実行モード
     */
    AutofucusMode autofucusMode = AutofucusMode.None;

    /**
     * シーン設定
     */
    SceneMode sceneMode = SceneMode.Auto;

    /**
     * カメラ性能リスト
     */
    CameraSpec specs;

    public enum FlashMode {
        Off,

        Auto,

        On,

        RedEye,

        Torch;

        public String getApiFlashMode() {
            return this.name().toLowerCase();
        }

        public static FlashMode get(String cameraFlashMode) {
            if (cameraFlashMode.equals("red-eye")) {
                return RedEye;
            }

            // 対応しているフラッシュモードIDに変換する
            try {
                FlashMode result = FlashMode.valueOf(cameraFlashMode.substring(0, 1).toUpperCase() + cameraFlashMode.substring(1));
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
            }
            return null;
        }
    }

    /**
     * シーン設定
     */
    public enum SceneMode {

        /**
         * 自動設定
         */
        Auto {
            @Override
            public String settingText() {
                return name().toLowerCase();
            }
        },

        /**
         * 人物撮影
         * ソフトスナップ by XperiaGX
         */
        Persons {
            @Override
            public String settingText() {
                return "portrait";
            }
        },

        /**
         * 風景
         */
        Scenery {
            @Override
            public String settingText() {
                return "landscape";
            }
        },

        /**
         * 夜景
         */
        Night {
            @Override
            public String settingText() {
                return "night";
            }
        },

        /**
         * 夜景人物
         * 夜景＆人物 by XperiaGX
         */
        NightAndPersons {
            @Override
            public String settingText() {
                return "night-portrait";
            }
        },

        /**
         * ビーチ
         * ビーチ & スノー by XperiaGX
         */
        Beach {
            @Override
            public String settingText() {
                return name().toLowerCase();
            }
        },

        /**
         * 雪景色
         * ビーチ & スノー by XperiaGX
         */
        Snow {
            @Override
            public String settingText() {
                return name().toLowerCase();
            }
        },

        /**
         * スポーツ
         */
        Sports {
            @Override
            public String settingText() {
                return name().toLowerCase();
            }
        },

        /**
         * パーティ
         */
        Party {
            @Override
            public String settingText() {
                return name().toLowerCase();
            }
        },

        /**
         * 二値化
         */
        Document {
            @Override
            public String settingText() {
                return "barcode";
            }
        };

        /**
         * CameraParametersに設定する文字列
         *
         * @return
         */
        public abstract String settingText();

        /**
         * 表示用テキストを取得する
         *
         * @param context
         * @return
         */
        public String text(Context context) {
            String result = ContextUtil.getStringFromIdName(context, String.format("Camera.Scene.%s", name()));
            if (StringUtil.isEmpty(result)) {
                return name();
            } else {
                return result;
            }
        }

        /**
         * カメラがサポートしているシーンをピックアップする
         *
         * @param rawSceneModes
         * @return
         */
        public static List<SceneMode> pickUp(List<String> rawSceneModes) {
            List<SceneMode> result = new ArrayList<SceneMode>();
            SceneMode[] allValues = values();
            // シーンが何も無ければカラリスト
            if (rawSceneModes == null) {
                return result;
            }

            for (SceneMode mode : allValues) {
                // モード文字列が含まれていればそれを返す
                if (rawSceneModes.contains(mode.settingText())) {
                    result.add(mode);
                }
            }

            return result;
        }
    }

    public enum CameraType {
        /**
         * メインカメラ
         * <p/>
         * 背面で高画質な場合が多い
         */
        Main,

        /**
         * サブカメラ
         * <p/>
         * 前面で低画質な場合が多い
         */
        Sub,

        /**
         * その他のカメラ
         * <p/>
         * ただし、3カメラ装備していることは恐らく無いと思われる
         */
        Extra,
    }

    public enum OrientationType {
        /**
         * そのまま
         */
        Rotate0 {
            @Override
            int degree() {
                return 0;
            }
        },

        /**
         * 90度傾ける
         */
        Rotate90 {
            @Override
            int degree() {
                return 90;
            }
        },

        /**
         * 180度傾ける
         */
        Rotate180 {
            @Override
            int degree() {
                return 180;
            }
        },

        /**
         * 270度傾ける
         */
        Rotate270 {
            @Override
            int degree() {
                return 270;
            }
        };

        abstract int degree();
    }

    /**
     * オートフォーカス状態
     */
    public enum AutofucusMode {
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
        Completed,
    }

    public CameraManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * カメラの回転角を取得する
     *
     * @param orientationType 回転タイプ
     * @return
     */
    public boolean requestOrientation(OrientationType orientationType) {
        try {
            // 回転角を設定する
            camera.setDisplayOrientation(orientationType.degree());
            this.orientationType = orientationType;
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    /**
     * 手ぶれ補正の有効化を行わせる
     * <p/>
     * この機能はプレビュー時及びビデオ撮影時のみに有効となる
     *
     * @param enable true=有効、false=無効
     * @return 切り替えに成功したらtrue
     */
    public boolean requestStabilization(boolean enable) {
        try {
            if (parameters.isVideoStabilizationSupported()) {
                parameters.setVideoStabilization(enable);
                return true;
            }
            LogUtil.log("not support Stabilization");
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return false;
    }

    /**
     * シーンモードを設定する
     *
     * @param sceneMode
     * @return
     */
    public boolean requestScene(SceneMode sceneMode) {
        try {
            parameters.setSceneMode(sceneMode.settingText());
            this.sceneMode = sceneMode;
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return false;
    }

    private Camera.Size chooseShotSize(List<Camera.Size> targetSizes, int width, int height, int minWidth, int minHeight) {
        final float TARGET_ASPECT = (float) Math.max(1, width) / (float) Math.max(1, height);
        try {
            Camera.Size target = null;
            float current_diff = 999999999;

            for (Camera.Size size : targetSizes) {
                // 最低限のフォーマットは保つ
                if (size.width >= minWidth && size.height >= minHeight) {
                    float aspect_diff = ((float) size.width / (float) size.height) - TARGET_ASPECT;

                    // アスペクト比の差分が小さい＝近い構成をコピーする
                    if (Math.abs(aspect_diff) < current_diff) {
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
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(parameters);
        LogUtil.log("previewSize request(%d x %d) -> set(%d x %d) no-frip", width, height, previewSize.width, previewSize.height);
    }

    /**
     * GPSデータを設定する
     *
     * @param lat 緯度
     * @param lng 経度
     */
    public void setGpsData(double lat, double lng) {
        parameters.setGpsLatitude(lat);
        parameters.setGpsLongitude(lng);
    }

    /**
     * JPEG画質を設定する
     *
     * @param quality 画質(0〜100)
     */
    public void setJpegQuality(int quality) {
        parameters.setJpegQuality(MathUtil.minmax(0, 100, quality));
    }

    /**
     * 撮影時のフラッシュモードを指定する
     *
     * @param mode フラッシュモード
     */
    public boolean requestFlashMode(FlashMode mode) {
        try {
            parameters.setFlashMode(mode.getApiFlashMode());
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
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
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        camera.setParameters(parameters);
        LogUtil.log("previewSize request(%d x %d) -> set(%d x %d) no-frip", width, height, pictureSize.width, pictureSize.height);
    }

    /**
     * 撮影サイズIDを指定する
     *
     * @param pictureSizeId
     */
    public void setPictureSize(String pictureSizeId) {
        Camera.Size pictureSize = specs.getShotSize(pictureSizeId).getCameraSize();
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        camera.setParameters(parameters);
        LogUtil.log("previewSize id(%s) request(%d x %d) no-frip", pictureSizeId, pictureSize.width, pictureSize.height);
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
     * カメラに接続する
     *
     * @param type
     */
    public boolean connect(CameraType type) {
        if (!isConnected()) {
            disconnect();
        }

        synchronized (lock) {
            try {
                final int numberOfCameras = Camera.getNumberOfCameras();
                int openCameraNumber = 0;
                if (type == CameraType.Main || numberOfCameras == 1) {
                    camera = Camera.open(0);
                } else if (numberOfCameras > 1 && type == CameraType.Sub) {
                    camera = Camera.open(1);
                    openCameraNumber = 1;
                } else {
                    camera = Camera.open();
                }

                parameters = camera.getParameters();
                this.connectedCameraType = type;
                if (isConnected()) {
                    // jpeg quality
                    setJpegQuality(100);

                    // スペックを切り出す
                    specs = new CameraSpec(openCameraNumber, camera);
                    return true;
                }
            } catch (Exception e) {
                LogUtil.log(e);
            }
            camera = null;
            parameters = null;
            connectedCameraType = null;
        }
        return false;
    }

    /**
     * 指定サーフェイスに対してプレビューを開始する
     *
     * @param surface
     * @return
     */
    public boolean startPreview(Object surface) {
        try {
            if (surface instanceof TextureView) {
                surface = ((TextureView) surface).getSurfaceTexture();
            }

            if (surface instanceof SurfaceHolder) {
                camera.setPreviewDisplay((SurfaceHolder) surface);
            } else if (surface instanceof SurfaceTexture) {
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
     *
     * @return
     */
    public boolean stopPreview() {
        try {
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
            autofucusMode = AutofucusMode.Completed;
        } else {
            autofucusMode = AutofucusMode.Failed;
        }
    }

    public CameraSpec getSpecs() {
        return specs;
    }

    /**
     * 現在のオートフォーカス状態を取得する
     *
     * @return
     */
    public AutofucusMode getAutofucusMode() {
        return autofucusMode;
    }

    /**
     * オートフォーカスの処理中であればtrue
     *
     * @return
     */
    public boolean isAutofocusProcessing() {
        return autofucusMode == AutofucusMode.Processing;
    }

    /**
     * 同期的にオートフォーカスを行う
     *
     * @return オートフォーカスに成功したらtrue
     */
    public boolean autofocusSync() {
        final Holder<Boolean> holder = new Holder<Boolean>();
        try {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        autofucusMode = AutofucusMode.Completed;
                    } else {
                        autofucusMode = AutofucusMode.Failed;
                    }
                    LogUtil.log("autofocus :: " + autofucusMode);
                    holder.set(success);
                }
            });
            autofucusMode = AutofucusMode.Processing;
        } catch (Exception e) {
            holder.set(false);
        }
        return holder.getWithWait(1000 * 10);
    }

    /**
     * オートフォーカスを開始する
     *
     * @return
     */
    public boolean startAutofocus() {
        synchronized (lock) {
            try {
                if (autofucusMode == AutofucusMode.Processing) {
                    return true;
                }

                autofucusMode = AutofucusMode.Processing;
                camera.autoFocus(this);
                return true;
            } catch (Exception e) {
                LogUtil.log(e);
            }
            return false;
        }
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
     *
     * @return
     */
    public boolean cancelAutofocus() {
        synchronized (lock) {
            if (!isAutofocusProcessing()) {
                return true;
            }

            try {
                autofucusMode = AutofucusMode.None;
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
        switch (orientationType) {
            case Rotate90:
            case Rotate270:
                return true;
        }
        return false;
    }

    /**
     * カメラを水平に持っている場合はtrue
     */
    public boolean isHorizontalMode() {
        switch (orientationType) {
            case Rotate0:
            case Rotate180:
                return true;
        }
        return false;
    }

    /**
     * プレビュー幅を取得する
     *
     * @return
     */
    public int getPreviewWidth() {
        return parameters.getPreviewSize().width;
    }

    /**
     * プレビュー高さを取得する
     *
     * @return
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
         *
         * @param success
         * @param camera
         */
        void onAutoFocus(boolean success, CameraManager camera);
    }

    /**
     * カメラスペックを取得する
     *
     * @param context
     * @param type
     * @return
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
