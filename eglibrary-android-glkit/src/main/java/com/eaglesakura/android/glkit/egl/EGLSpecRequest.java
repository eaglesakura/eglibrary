package com.eaglesakura.android.glkit.egl;

import java.util.ArrayList;
import java.util.List;

import static javax.microedition.khronos.egl.EGL10.EGL_ALPHA_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_BLUE_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_DEPTH_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_GREEN_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_RED_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_RENDERABLE_TYPE;
import static javax.microedition.khronos.egl.EGL10.EGL_STENCIL_SIZE;

/**
 * 要求するEGL設定
 */
public class EGLSpecRequest {
    /**
     * EGLで利用する色バッファ情報
     */
    public enum ColorSpec {
        /**
         * RGBA各8bit
         */
        RGBA8,

        /**
         * RGB各8bit
         */
        RGB8,

        /**
         * RGB各5/6/5bit
         */
        RGB565,
    }

    /**
     * サーフェイスに持たせる色情報
     */
    public ColorSpec surfaceColor = ColorSpec.RGBA8;

    /**
     * 深度バッファのビット数
     */
    public int surfaceDepthBits = 16;

    /**
     * ステンシルバッファのビット数
     */
    public int surfaceStencilBits = 8;

    /**
     * GLバージョンを指定する。
     * <p/>
     * デフォルトは多くの環境で動作可能なES20
     */
    public GLESVersion version;

    public EGLSpecRequest() {
        this.version = GLESVersion.supported(); // デフォルトではサポートしている最大バージョンで初期化する
    }

    /**
     * 各種サーフェイスの色情報を指定する
     *
     * @param r
     * @param g
     * @param b
     * @param a
     * @param d
     * @param s
     */
    public void setSurfaceColorSpec(int r, int g, int b, int a, int d, int s) {
        if (r >= 8 && g >= 8 && b >= 8) {
            // alphaが指定してある
            if (a > 0) {
                surfaceColor = ColorSpec.RGBA8;
            } else {
                surfaceColor = ColorSpec.RGB8;
            }
        } else {
            surfaceColor = ColorSpec.RGB565;
        }

        surfaceDepthBits = d;
        surfaceStencilBits = s;
    }

    /**
     * EGLConfigを取得するためのスペックリクエストを生成する
     */
    public int[] createConfigSpecs() {
        if (version.ordinal() > GLESVersion.supported().ordinal()) {
            // サポートするバージョンを超えていたら例外を投げる
            throw new UnsupportedOperationException(String.format("not support(%s)", version.name()));
        }

        List<Integer> result = new ArrayList<Integer>();
        // レンダラーを指定バージョンに設定
        {
            if (version.ordinal() >= GLESVersion.GLES30.ordinal()) {
                result.add(EGL_RENDERABLE_TYPE);
                result.add(0x0010); /* EGL_OPENGL_ES3_BIT */
            } else if (version == GLESVersion.GLES20) {
                result.add(EGL_RENDERABLE_TYPE);
                result.add(0x0004); /* EGL_OPENGL_ES2_BIT */
            }
        }

        switch (surfaceColor) {
            case RGBA8:
                result.add(EGL_RED_SIZE);
                result.add(8);
                result.add(EGL_GREEN_SIZE);
                result.add(8);
                result.add(EGL_BLUE_SIZE);
                result.add(8);
                result.add(EGL_ALPHA_SIZE);
                result.add(8);
                break;
            case RGB8:
                result.add(EGL_RED_SIZE);
                result.add(8);
                result.add(EGL_GREEN_SIZE);
                result.add(8);
                result.add(EGL_BLUE_SIZE);
                result.add(8);
                break;
            case RGB565:
                result.add(EGL_RED_SIZE);
                result.add(5);
                result.add(EGL_GREEN_SIZE);
                result.add(6);
                result.add(EGL_BLUE_SIZE);
                result.add(5);
                break;
            default:
                throw new UnsupportedOperationException(surfaceColor.toString());
        }

        if (surfaceDepthBits > 0) {
            result.add(EGL_DEPTH_SIZE);
            result.add(surfaceDepthBits);
        }

        if (surfaceStencilBits > 0) {
            result.add(EGL_STENCIL_SIZE);
            result.add(surfaceStencilBits);
        }

        // 終端
        result.add(EGL_NONE);

        int[] result_array = new int[result.size()];
        for (int i = 0; i < result.size(); ++i) {
            result_array[i] = result.get(i);
        }
        return result_array;
    }
}
