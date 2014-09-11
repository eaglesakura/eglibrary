package com.eaglesakura.android.glkit.res;

import android.content.Context;

import com.eaglesakura.android.camera.CameraManager;
import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCField;
import com.eaglesakura.jc.annotation.JCMethod;
import com.eaglesakura.util.LogUtil;

/**
 * カメラプレビューをテクスチャに焼きこむクラス
 */
@JCClass(cppNamespace = "es.glkit")
public class CameraTextureRenderer extends CameraManager {

    /**
     * メインカメラを起動する
     */
    @JCField
    public static final int CAMERAMODE_MAIN = 0;

    /**
     * サブカメラを起動する
     */
    @JCField
    public static final int CAMERAMODE_SUB = 1;

    /**
     * 0度傾ける
     */
    @JCField
    public static final int ORIENTATION_0 = 0;

    /**
     * 90度傾ける
     */
    @JCField
    public static final int ORIENTATION_90 = 1;

    /**
     * 180度傾ける
     */
    @JCField
    public static final int ORIENTATION_180 = 2;

    /**
     * 270度傾ける
     */
    @JCField
    public static final int ORIENTATION_270 = 3;

    /**
     * オートフォーカス未実行
     */
    @JCField
    public static final int AUTOFOCUS_NONE = 0;

    /**
     * オートフォーカス実行中
     */
    @JCField
    public static final int AUTOFOCUS_PROCESSING = 1;

    /**
     * オートフォーカス失敗
     */
    @JCField
    public static final int AUTOFOCUS_FAILED = 2;

    /**
     * オートフォーカス成功
     */
    @JCField
    public static final int AUTOFOCUS_COMPLETED = 3;

    /**
     * プレビュー対象のサーフェイス
     */
    PreviewSurfaceTexture previewSurface;

    public CameraTextureRenderer(Context context) {
        super(context);
    }

    @JCMethod
    public boolean requestOrientation(int ORIENTATION) {
        return requestOrientation(OrientationType.values()[ORIENTATION]);
    }

    @JCMethod
    @Override
    public boolean startAutofocus() {
        return super.startAutofocus();
    }

    @JCMethod
    @Override
    public boolean isAutofocusProcessing() {
        return super.isAutofocusProcessing();
    }

    @JCMethod
    public int getAutofucusModeI() {
        return getAutofucusMode().ordinal();
    }

    @JCMethod
    @Override
    public int getPreviewHeight() {
        return super.getPreviewHeight();
    }

    @JCMethod
    @Override
    public int getPreviewWidth() {
        return super.getPreviewWidth();
    }

    /**
     * カメラに接続する
     *
     * @param CAMERAMODE
     * @return
     */
    @JCMethod
    public boolean connect(int CAMERAMODE) {
        return super.connect(CameraType.values()[CAMERAMODE]);
    }

    @JCMethod
    @Override
    public void disconnect() {
        super.disconnect();
    }

    /**
     * プレビューを開始する
     *
     * @param textureName 焼きこみ対象のteture / glGenTexturesしたばかりのオブジェクトが必要
     * @return
     */
    @JCMethod
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
     *
     * @return
     */
    @JCMethod
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
     *
     * @return
     */
    @JCMethod
    public PreviewSurfaceTexture getPreviewSurface() {
        return previewSurface;
    }
}
