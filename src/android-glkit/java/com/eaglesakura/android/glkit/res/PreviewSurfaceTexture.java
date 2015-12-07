package com.eaglesakura.android.glkit.res;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * MediaPlayerやCameraのキャプチャ対象テクスチャとして管理される
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class PreviewSurfaceTexture extends SurfaceTexture implements SurfaceTexture.OnFrameAvailableListener {
    /**
     * キャプチャの準備が出来たらtrue
     */
    boolean captured = false;

    /**
     * テクスチャ描画用マトリクス
     */
    FloatBuffer textureMatrix;

    public PreviewSurfaceTexture(int texName) {
        super(texName);
        setOnFrameAvailableListener(this);
        textureMatrix = ByteBuffer.allocateDirect(4 * 16).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    /**
     * キャプチャコールバックを受け取る
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            captured = true;
        }
    }

    /**
     * テクスチャに対して画像を焼きこむ
     *
     * @return
     */
    public boolean renderingToTexture() {
        synchronized (this) {
            // キャプチャ準備ができていたら焼きこむ
            if (captured) {
                updateTexImage();
                captured = false;
                return true;
            }
        }
        return false;
    }

    /**
     * テクスチャ行列を取得する。
     * <br>
     * 行列が取得できていない状態の場合、nullptrを返す。
     *
     * @return
     */
    public FloatBuffer getTextureMatrix() {
        // 行列のキャプチャを試みる
//        if (!matrixCompleted)
        {
            boolean matrixCompleted = false;
            float[] temp = new float[4 * 4];
            getTransformMatrix(temp);
            for (float f : temp) {
                if (f != 0) {
                    matrixCompleted = true;
                }
            }

            if (!matrixCompleted) {
                // 行列が構築されていないため何もしない
                return null;
            }

            textureMatrix.put(temp).position(0);
        }

        return textureMatrix;
    }
}
