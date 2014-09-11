package com.eaglesakura.android.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.eaglesakura.util.LogUtil;

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
     * 指定したアスペクト比に近いプレビューサイズを選択する
     *
     * @param width     リクエストする幅
     * @param height    リクエストする高さ
     * @param minWidth  最低限持っていて欲しい幅
     * @param minHeight 最低限持っていていて欲しい高さ
     */
    public void requestPreviewSize(int width, int height, int minWidth, int minHeight) {
        LogUtil.log("request preview(%d, %d)", width, height);
        final float TARGET_ASPECT = (float) Math.max(1, width) / (float) Math.max(1, height);

        try {
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size target = null;
            float current_diff = 999999999;

            for (Camera.Size size : previewSizes) {
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

                LogUtil.log("change preview size(%d x %d) no-frip", target.width, target.height);
                parameters.setPreviewSize(target.width, target.height);
                camera.setParameters(parameters);

                parameters = camera.getParameters();
            }
        } catch (Exception e) {
            LogUtil.log(e);
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

                if (type == CameraType.Main || numberOfCameras == 1) {
                    camera = Camera.open(0);
                } else if (numberOfCameras > 1 && type == CameraType.Sub) {
                    camera = Camera.open(1);
                } else {
                    camera = Camera.open();
                }

                parameters = camera.getParameters();
                this.connectedCameraType = type;
                if (isConnected()) {
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
}
