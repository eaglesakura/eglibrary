package com.eaglesakura.android.glkit.res;

import com.eaglesakura.android.camera.CameraManager;
import com.eaglesakura.android.camera.OrientationSpec;
import com.eaglesakura.util.LogUtil;

import android.content.Context;

/**
 * カメラプレビューをテクスチャに焼きこむクラス
 */
public class CameraTextureRenderer extends CameraManager {

    /**
     * メインカメラを起動する
     */
    public static final int CAMERAMODE_MAIN = 0;

    /**
     * サブカメラを起動する
     */
    public static final int CAMERAMODE_SUB = 1;

    /**
     * プレビュー対象のサーフェイス
     */
    PreviewSurfaceTexture previewSurface;

    public CameraTextureRenderer(Context context) {
        super(context);
    }

    public boolean requestOrientation(int orientation) {
        return requestOrientation(OrientationSpec.fromDegree(orientation));
    }

    /**
     * プレビューを開始する。開始時点でプレビューサーフェイスを生成する。
     *
     * @param textureName 焼きこみ対象のteture / glGenTexturesしたばかりのオブジェクトが必要
     */
    public boolean startPreview(int textureName) {
        try {
            previewSurface = new PreviewSurfaceTexture(textureName);
            if (super.startPreview(previewSurface)) {
                // 成功
                return true;
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }

        // 何らかの問題が発生したら解放して失敗
        if (previewSurface != null) {
            previewSurface.release();
            previewSurface = null;
        }
        return false;
    }

    /**
     * プレビュー終了とリソース解放を行う
     */
    @Override
    public boolean stopPreview() {
        if (previewSurface == null) {
            return false;
        }

        if (super.stopPreview()) {
            try {
                previewSurface.release();
            } catch (Exception e) {
                LogUtil.log(e);
            }
            previewSurface = null;
            return true;
        }
        return false;
    }

    /**
     * プレビュー対象のサーフェイスを取得する
     */
    public PreviewSurfaceTexture getPreviewSurface() {
        return previewSurface;
    }
}
