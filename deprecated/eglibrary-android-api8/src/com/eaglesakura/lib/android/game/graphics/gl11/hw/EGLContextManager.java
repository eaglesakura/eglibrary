package com.eaglesakura.lib.android.game.graphics.gl11.hw;

import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

import android.graphics.PixelFormat;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

class EGLContextManager extends DisposableResource {
    /**
     * 初期化時のコンフィグスペック。
     */
    private int[] configSpec = {
            EGL10.EGL_NONE
            //! 終端にはEGL_NONEを入れる
    };

    /**
     * コンテキスト本体
     */
    private EGLContext context = null;

    private EGL10 egl;

    private EGLDisplay display;

    /**
     * 選択したconfig
     */
    private EGLConfig config;

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
     *
     * @return
     */
    public EGLContext getContext() {
        return context;
    }

    /**
     * Config IDを取得する
     */
    public EGLConfig getConfig() {
        return config;
    }

    /**
     * contextを作成させる
     */
    public void createContext(EGLManager eglManager) {

        egl = eglManager.egl;
        display = eglManager.display;

        // コンフィグ設定
        final int CONFIG_MAX = 100;
        EGLConfig[] configs = new EGLConfig[CONFIG_MAX];
        int[] numConfigs = new int[1];

        //! コンフィグを全て取得する
        if (!egl.eglChooseConfig(display, configSpec, configs, CONFIG_MAX, numConfigs)) {
            throw new IllegalStateException(eglManager.toEglErrorInfo(egl.eglGetError()));
        }

        //! 必要なものを保存する
        config = chooseConfig(egl, display, configs);
        if (config == null) {
            throw new IllegalStateException("Config not match!!");
        }

        context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, null);
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

    /**
     * 自動でコンフィグを設定する。
     */
    void autoConfigSpec(int pixelFormat, boolean depth) {
        List<Integer> specs = new ArrayList<Integer>();

        if (pixelFormat == PixelFormat.RGB_565) {
            LogUtil.log("EGLContext RGB565");
            specs.add(EGL10.EGL_RED_SIZE);
            specs.add(5);
            specs.add(EGL10.EGL_GREEN_SIZE);
            specs.add(6);
            specs.add(EGL10.EGL_BLUE_SIZE);
            specs.add(5);

            pixelSizeR = 5;
            pixelSizeG = 6;
            pixelSizeB = 5;
            pixelSizeA = 0;
        } else if (pixelFormat == PixelFormat.RGB_888) {
            LogUtil.log("EGLContext RGB888");
            specs.add(EGL10.EGL_RED_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_GREEN_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_BLUE_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_ALPHA_SIZE);
            specs.add(0);

            pixelSizeR = 8;
            pixelSizeG = 8;
            pixelSizeB = 8;
            pixelSizeA = 0;
        } else if (pixelFormat == PixelFormat.RGBA_8888) {
            LogUtil.log("EGLContext RGB8888");
            specs.add(EGL10.EGL_RED_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_GREEN_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_BLUE_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_ALPHA_SIZE);
            specs.add(8);

            pixelSizeR = 8;
            pixelSizeG = 8;
            pixelSizeB = 8;
            pixelSizeA = 8;
        }

        if (depth) {
            specs.add(EGL10.EGL_DEPTH_SIZE);
            specs.add(16);

            pixelSizeD = 16;
        } else {
            pixelSizeD = 0;
        }

        // 8bit stencilを作成する
        {
            specs.add(EGL10.EGL_STENCIL_SIZE);
            specs.add(8);
            pixelSizeS = 8;
        }

        specs.add(EGL10.EGL_SURFACE_TYPE);
        specs.add(EGL10.EGL_WINDOW_BIT);

        specs.add(EGL10.EGL_NONE);

        configSpec = new int[specs.size()];
        for (int i = 0; i < configSpec.length; ++i) {
            configSpec[i] = specs.get(i);
        }
    }

    @Override
    public void dispose() {
        if (context != null) {
            egl.eglDestroyContext(display, context);
            context = null;
        }
    }
}
