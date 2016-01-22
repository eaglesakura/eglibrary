package com.eaglesakura.lib.android.game.graphics.gl11.hw;

import com.eaglesakura.lib.android.game.util.LogUtil;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * 指定したspecを満たすEGLConfigを取得する
 *
 * @author TAKESHI YAMASHITA
 */
@SuppressLint("DefaultLocale")
public class EGLConfigSelector {

    /**
     * 赤ビット深度
     */
    private int pixelSizeR = 5;

    /**
     * 緑ビット深度
     */
    private int pixelSizeG = 6;

    /**
     * 青ビット深度
     */
    private int pixelSizeB = 5;

    /**
     * 透過ビット深度
     */
    private int pixelSizeA = 0;

    /**
     * 深度ビット
     */
    private int pixelSizeD = 16;

    /**
     * ステンシルビット数
     */
    private int pixelSizeS = 8;

    /**
     * 初期化時のコンフィグスペック。
     */
    private int[] configSpec = {
            EGL10.EGL_NONE
            //! 終端にはEGL_NONEを入れる
    };

    /**
     * RGBAをそれぞれ指定して作成する
     */
    public EGLConfigSelector(int rSize, int gSize, int bSize, int aSize, int dSize, int sSize) {

        this.pixelSizeR = rSize;
        this.pixelSizeG = gSize;
        this.pixelSizeB = bSize;
        this.pixelSizeA = aSize;
        this.pixelSizeD = dSize;
        this.pixelSizeS = sSize;

        makeSpecs();
    }

    /**
     * 現在の情報からスペックを作成する
     */
    private void makeSpecs() {
        List<Integer> specs = new ArrayList<Integer>();

        specs.add(EGL10.EGL_RED_SIZE);
        specs.add(pixelSizeR);
        specs.add(EGL10.EGL_GREEN_SIZE);
        specs.add(pixelSizeG);
        specs.add(EGL10.EGL_BLUE_SIZE);
        specs.add(pixelSizeB);

        if (pixelSizeA > 0) {
            specs.add(EGL10.EGL_ALPHA_SIZE);
            specs.add(pixelSizeA);
        }

        if (pixelSizeD > 0) {
            specs.add(EGL10.EGL_DEPTH_SIZE);
            specs.add(pixelSizeD);
        }

        // 8bit stencilを作成する
        if (pixelSizeS > 0) {
            specs.add(EGL10.EGL_STENCIL_SIZE);
            specs.add(8);
        }

        //        specs.add(EGL10.EGL_SURFACE_TYPE);
        //        specs.add(EGL10.EGL_WINDOW_BIT);

        {
            // ES2
            specs.add(EGL10.EGL_RENDERABLE_TYPE);
            specs.add(4);
        }
        specs.add(EGL10.EGL_NONE);

        configSpec = new int[specs.size()];
        for (int i = 0; i < configSpec.length; ++i) {
            configSpec[i] = specs.get(i);
        }
    }

    public EGLConfig choose(EGL10 egl, EGLDisplay display) {
        // コンフィグ設定
        final int CONFIG_MAX = 100;
        EGLConfig[] configs = new EGLConfig[CONFIG_MAX];
        int[] numConfigs = new int[1];

        //! コンフィグを全て取得する
        if (!egl.eglChooseConfig(display, configSpec, configs, CONFIG_MAX, numConfigs)) {
            throw new RuntimeException("eglChooseConfig() failed...");
        }

        return chooseConfig(egl, display, configs);
    }

    private EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
        LogUtil.log(String.format("request EGL Pixel Format(R(%d), G(%d), B(%d), A(%d), D(%d), S(%d)", pixelSizeR,
                pixelSizeG, pixelSizeB, pixelSizeA, pixelSizeD, pixelSizeS));
        int index = 0;
        for (EGLConfig config : configs) {
            if (config == null) {
                ++index;
                continue;
            }

            int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
            int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
            int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
            int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
            int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

            LogUtil.log(String.format("check EGL Pixel Format(R(%d), G(%d), B(%d), A(%d), D(%d), S(%d)", r, g, b, a, d,
                    s));
            // まずはジャストフィットのconfigを探す
            if (d == pixelSizeD && s == pixelSizeS && r == pixelSizeR && g == pixelSizeG && b == pixelSizeB
                    && a == pixelSizeA) {
                LogUtil.log(String.format("just config index :: %d", index));

                LogUtil.log(String.format("result EGL Pixel Format(R(%d), G(%d), B(%d), A(%d), D(%d), S(%d)", r, g, b,
                        a, d, s));
                return config;
            }
            ++index;
        }

        index = 0;
        for (EGLConfig config : configs) {
            if (config == null) {
                ++index;
                continue;
            }

            int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
            int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
            int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
            int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
            int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

            // スペックを満たせば、それ以上のconfigが設置されても文句は言わない
            if (d >= pixelSizeD && s >= pixelSizeS && r >= pixelSizeR && g >= pixelSizeG && b >= pixelSizeB
                    && a >= pixelSizeA) {
                LogUtil.log(String.format("config index :: %d", index));

                LogUtil.log(String.format("result EGL Pixel Format(R(%d), G(%d), B(%d), A(%d), D(%d), S(%d)", r, g, b,
                        a, d, s));
                return config;
            }
            ++index;
        }
        return null;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
        int[] mValue = new int[1];
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

}
