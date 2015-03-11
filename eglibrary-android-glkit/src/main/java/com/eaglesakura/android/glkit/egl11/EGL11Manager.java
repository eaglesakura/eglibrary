package com.eaglesakura.android.glkit.egl11;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.eaglesakura.android.glkit.GLKitUtil;
import com.eaglesakura.android.glkit.egl.EGLProcessState;
import com.eaglesakura.android.glkit.egl.EGLSpecRequest;
import com.eaglesakura.android.glkit.egl.GLESVersion;
import com.eaglesakura.android.glkit.egl.IEGLContextGroup;
import com.eaglesakura.android.glkit.egl.IEGLManager;
import com.eaglesakura.android.glkit.egl.IEGLDevice;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import static javax.microedition.khronos.egl.EGL10.EGL_ALPHA_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_BLUE_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_DEPTH_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_GREEN_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_RED_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_STENCIL_SIZE;

/**
 * EGL1.1相当の機能を許可するコントローラー
 * <p/>
 * 古いバージョンでも動作するが、細かい動作は行えない。
 */
public class EGL11Manager implements IEGLManager {
    /**
     * EGLオブジェクト
     */
    final EGL10 egl;

    /**
     * レンダリング用ディスプレイ
     */
    final EGLDisplay display;

    /**
     * config情報
     */
    EGLConfig config = null;

    /**
     * app context
     */
    final Context context;

    /**
     * EGL version
     * 1.0
     */
    int[] eglVersion = new int[]{
            -1, -1
    };

    /**
     * EGLステータス
     */
    EGLSpecRequest specRequest;


    public EGL11Manager(Context context) {
        this.context = context.getApplicationContext();

        egl = (EGL10) EGLContext.getEGL();
        display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

    }


    public GLESVersion getGLESVersion() {
        return specRequest.version;
    }

    /**
     * 初期化を行う
     */
    @Override
    public synchronized void initialize(EGLSpecRequest request) {
        this.specRequest = request;
    }


    private final EGLConfig chooseConfig(EGLSpecRequest request) {
        //! コンフィグを全て取得する
        EGLConfig[] configs = new EGLConfig[48];
        // コンフィグ数がeglChooseConfigから返される
        int[] config_num = new int[1];
        if (!egl.eglChooseConfig(display, request.createConfigSpecs(), configs, configs.length,
                config_num)) {
            throw new RuntimeException("eglChooseConfig");
        }
        final int CONFIG_NUM = config_num[0];

        int r_bits = 0;
        int g_bits = 0;
        int b_bits = 0;
        int a_bits = 0;

        switch (request.surfaceColor) {
            case RGBA8:
                r_bits = g_bits = b_bits = a_bits = 8;
                break;
            case RGB8:
                r_bits = g_bits = b_bits = 8;
                break;
            case RGB565:
                r_bits = 5;
                g_bits = 6;
                b_bits = 5;
                break;
            default:
                throw new UnsupportedOperationException(request.surfaceColor.toString());
        }

        // 指定したジャストサイズのconfigを探す
        for (int i = 0; i < CONFIG_NUM; ++i) {
            final EGLConfig checkConfig = configs[i];

            final int config_r = getConfigAttrib(checkConfig, EGL_RED_SIZE);
            final int config_g = getConfigAttrib(checkConfig, EGL_GREEN_SIZE);
            final int config_b = getConfigAttrib(checkConfig, EGL_BLUE_SIZE);
            final int config_a = getConfigAttrib(checkConfig, EGL_ALPHA_SIZE);
            final int config_d = getConfigAttrib(checkConfig, EGL_DEPTH_SIZE);
            final int config_s = getConfigAttrib(checkConfig, EGL_STENCIL_SIZE);

            // RGBが指定サイズジャスト、ADSが指定サイズ以上あれば合格とする
            if (config_r == r_bits && config_g == g_bits && config_b == b_bits
                    && config_a >= a_bits // alphaはオプション
                    && config_d >= request.surfaceDepthBits // depthはオプション
                    && config_s >= request.surfaceStencilBits // stencilはオプション
                    ) {

                log("R(%d) G(%d) B(%d) A(%d) D(%d) S(%d)", config_r, config_g, config_b, config_a, config_d, config_s);
                return checkConfig;
            }
        }

        // ジャストサイズが見つからなければ先頭のコンフィグを返す
        return configs[0];
    }

    int getConfigAttrib(EGLConfig eglConfig, int attr) {
        int[] value = new int[1];
        egl.eglGetConfigAttrib(display, eglConfig, attr, value);
        return value[0];
    }

    public EGL10 getEGL() {
        return egl;
    }

    @Override
    public int[] getSupportedEglVersion() {
        return eglVersion;
    }

    @Override
    public IEGLDevice newDevice(IEGLContextGroup contextGroup) {
        // プロセス全体のデバイス数を増加させる
        if (EGLProcessState.incrementDevice()) {
            log("createSurface EGL1.0 / GLES(%s)", getGLESVersion().name());

            if (!egl.eglInitialize(display, eglVersion)) {
                GLKitUtil.printEglError(egl.eglGetError());
                throw new RuntimeException("eglInitialize");
            }

            log("EGL Version(%d.%d)", eglVersion[0], eglVersion[1]);
        }

        // コンフィグが無いならコンフィグを作成する
        if (config == null) {
            // config取得
            config = chooseConfig(specRequest);
        }


        if (contextGroup == null) {
            contextGroup = new EGL11ContextGroup(this);
        }
        return new EGL11Device(this, (EGL11ContextGroup) contextGroup);
    }

    /**
     * デバイスの廃棄を行わせる
     *
     * @param device
     */
    void onDestroyDevice(EGL11Device device) {
        if (EGLProcessState.decrementDevice()) {
            log("terminate EGL1.0");

            // デバイスを持つ必要がなくなったからterminate
            egl.eglTerminate(display);
        }
    }

    void log(String msg, Object... fmt) {
        Log.i("EGL11", String.format(msg, fmt));
    }

    public Context getApplicationContext() {
        return context;
    }

    public AssetManager getAssetManager() {
        return context.getAssets();
    }
}
