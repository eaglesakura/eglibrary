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
    private final int number;

    /**
     * カメラのプレビューサイズ
     */
    private List<PictureSize> previewSizes = new ArrayList<PictureSize>();

    /**
     * カメラの撮影サイズ
     */
    private List<PictureSize> shotSizes = new ArrayList<PictureSize>();

    /**
     * オートフォーカスサポートの場合はtrue
     */
    private List<CameraManager.FlashMode> flashModes = new ArrayList<CameraManager.FlashMode>();

    /**
     * カメラの種類
     */
    private CameraManager.CameraType type;

    public CameraSpec(int num, Camera camera) {
        this.number = num;
        this.type = CameraManager.CameraType.values()[num];

        // スペックを取得する
        Camera.Parameters parameters = camera.getParameters();
        // 解像度
        {
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
            for (Camera.Size size : sizeList) {
                previewSizes.add(new PictureSize(size));
            }
        }
        {
            List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
            for (Camera.Size size : sizeList) {
                shotSizes.add(new PictureSize(size));
            }
        }

        // フラッシュ
        {
            List<String> supportedFlashModeList = parameters.getSupportedFlashModes();
            if (supportedFlashModeList != null) {
                for (String mode : supportedFlashModeList) {
                    CameraManager.FlashMode supportedFlashMode = CameraManager.FlashMode.get(mode);
                    if (supportedFlashMode != null) {
                        flashModes.add(supportedFlashMode);
                    } else {
                        LogUtil.log("unknown flash mode(%s)", mode);
                    }
                }
            }
        }
    }

    /**
     * プレビューサイズ一覧を取得
     *
     * @return
     */
    public List<PictureSize> getPreviewSizes() {
        return previewSizes;
    }

    /**
     * 撮影サイズ一覧を取得
     *
     * @return
     */
    public List<PictureSize> getShotSizes() {
        return shotSizes;
    }

    public int getNumber() {
        return number;
    }
}
