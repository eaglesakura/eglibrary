package com.eaglesakura.android.camera;

import android.hardware.Camera;

import com.eaglesakura.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * リアカメラ、フロントカメラごとのスペックを示したクラス
 */
public class CameraSpec {
    /**
     * カメラの識別番号
     */
    private final CameraType type;

    /**
     * カメラのプレビューサイズ
     */
    private List<PictureSizeSpec> previewSizes = new ArrayList<PictureSizeSpec>();

    /**
     * カメラの撮影サイズ
     */
    private List<PictureSizeSpec> shotSizes = new ArrayList<PictureSizeSpec>();
    /**
     * サポートしているシーン
     */
    private final List<SceneSpec> sceneSpecs;

    /**
     * ホワイトバランス設定一覧
     */
    private final List<WhiteBaranceSpec> whiteBaranceSpecs;

    /**
     * フォーカスモード一覧
     */
    private final List<FocusModeSpec> focusModeSpecs;

    /**
     * フラッシュモード一覧
     */
    private final List<FlashModeSpec> flashModeSpecs;

    public CameraSpec(CameraType type, Camera camera) {
        this.type = type;

        // スペックを取得する
        Camera.Parameters parameters = camera.getParameters();
        // 解像度
        {
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
            for (Camera.Size size : sizeList) {
                previewSizes.add(new PictureSizeSpec(size));
            }
        }
        {
            List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
            for (Camera.Size size : sizeList) {
                shotSizes.add(new PictureSizeSpec(size));
            }
        }

        sceneSpecs = SceneSpec.list(parameters.getSupportedSceneModes());   // シーンモード
        whiteBaranceSpecs = WhiteBaranceSpec.list(parameters.getSupportedWhiteBalance());   // ホワイトバランス
        focusModeSpecs = FocusModeSpec.list(parameters.getSupportedFocusModes()); // フォーカス設定
        flashModeSpecs = FlashModeSpec.list(parameters.getSupportedFlashModes()); // フラッシュモード一覧
    }

    /**
     * プレビューサイズ一覧を取得
     *
     * @return
     */
    public List<PictureSizeSpec> getPreviewSizes() {
        return previewSizes;
    }

    /**
     * 撮影サイズ一覧を取得
     *
     * @return
     */
    public List<PictureSizeSpec> getShotSizes() {
        return shotSizes;
    }

    /**
     * シーンをサポートしていたらtrue
     *
     * @param scene
     * @return
     */
    public boolean isSupportedScene(SceneSpec scene) {
        return sceneSpecs.contains(scene);
    }

    public List<SceneSpec> getSceneSpecs() {
        return sceneSpecs;
    }

    public List<WhiteBaranceSpec> getWhiteBaranceSpecs() {
        return whiteBaranceSpecs;
    }

    public List<FlashModeSpec> getFlashModeSpecs() {
        return flashModeSpecs;
    }

    public List<FocusModeSpec> getFocusModeSpecs() {
        return focusModeSpecs;
    }

    /**
     * フラッシュモードを持っていたらtrue
     *
     * @return
     */
    public boolean hasFlash() {
        for (FlashModeSpec spec : flashModeSpecs) {
            if (spec.getApiSettingName().equals("on")) {
                return true;
            }
        }
        return false;
    }

    public CameraType getType() {
        return type;
    }

    public int getCameraNumber() {
        return type.getCameraNumber();
    }

    /**
     * IDからプレビューサイズを逆引きする
     *
     * @param id
     * @return
     */
    public PictureSizeSpec getPreviewSize(String id) {
        for (PictureSizeSpec size : previewSizes) {
            if (size.getId().equals(id)) {
                return size;
            }
        }
        return null;
    }


    /**
     * IDから撮影サイズを逆引きする
     *
     * @param id
     * @return
     */
    public PictureSizeSpec getShotSize(String id) {
        for (PictureSizeSpec size : shotSizes) {
            if (size.getId().equals(id)) {
                return size;
            }
        }
        return null;
    }
}
