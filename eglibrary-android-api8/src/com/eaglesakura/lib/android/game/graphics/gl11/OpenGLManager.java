/**
 * OpenGL管理を行う。<BR>
 * 将来的にはデバイス非依存とする。
 * @author eagle.sakura
 * @version 2009/11/14 : 新規作成
 */
package com.eaglesakura.lib.android.game.graphics.gl11;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.view.SurfaceHolder;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.Color;
import com.eaglesakura.lib.android.game.graphics.DisposableResource;
import com.eaglesakura.lib.android.game.math.Matrix4x4;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * @author eagle.sakura
 */
public class OpenGLManager extends DisposableResource {
    /**
     * 管理しているサーフェイス。
     */
    private SurfaceHolder holder = null;
    /**
     * GL10本体。
     */
    private EGL10 egl = null;

    /**
     * GL本体。
     */
    private GL11 gl11 = null;

    /**
     * 拡張パック
     */
    private GL11ExtensionPack gl11EP = null;
    /**
     * GLコンテキスト。
     */
    private EGLContext eglContext = null;
    /**
     * ディスプレイ。
     */
    private EGLDisplay eglDisplay = null;
    /**
     * サーフェイス。
     */
    private EGLSurface eglSurface = null;

    /**
     * コンフィグ情報。
     * 実際に利用するコンフィグ
     */
    private EGLConfig eglConfig = null;

    /**
     * デバイスで利用可能なコンフィグ一覧
     */
    private List<EGLConfig> deviceConfigs = new ArrayList<EGLConfig>();

    /**
     * GLを初期化したスレッドのハンドラ
     */
    private Handler glHandler = null;

    /**
     * GL描画用スレッドを作成する。
     * 
     * @author eagle.sakura
     * @param holder
     */
    public OpenGLManager() {
    }

    /**
     * メインで使用するGLインターフェースを取得する。
     * 
     * @author eagle.sakura
     * @return
     */
    public GL11 getGL() {
        return gl11;
    }

    /**
     * 操作対象のハンドラを取得する。
     * @return
     */
    public Handler getHandler() {
        return glHandler;
    }

    /**
     * 通常合成
     */
    public static final int BLEND_ALPHA_NORMAL = 0;

    /**
     * アルファ合成
     */
    public static final int BLEND_ALPHA_ADD = 1;

    /**
     * 減算合成
     */
    public static final int BLEND_ALPHA_SUB = 2;

    /**
     * 乗算合成
     */
    public static final int BLEND_ALPHA_MUL = 3;

    /**
     * 色加算合成
     */
    public static final int BLEND_COLOR_ADD = 4;

    /**
     * 
     * @param mode
     */
    public void setBlendMode(int mode) {
        switch (mode) {
            case BLEND_ALPHA_NORMAL:
                gl11.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case BLEND_ALPHA_ADD:
                gl11.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
                break;
            case BLEND_ALPHA_SUB:
                break;
            case BLEND_ALPHA_MUL:
                gl11.glBlendFunc(GL10.GL_DST_COLOR, GL10.GL_ZERO);
                break;
            case BLEND_COLOR_ADD:
                gl11.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);
                break;
            default:
                break;
        }
    }

    /**
     * バッファの消去を行う。
     * 
     * @author eagle.sakura
     * @param mask
     */
    public void clear(int mask) {
        gl11.glClear(mask);
    }

    /**
     * バッファの消去を行う。<BR>
     * color / depthの両方をクリアする。
     * 
     * @author eagle.sakura
     */
    public void clear() {
        gl11.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * 消去色を設定する。
     * 
     * @author eagle.sakura
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public void clearColorRGBA(float r, float g, float b, float a) {
        gl11.glClearColor(r, g, b, a);
    }

    /**
     * 消去色を指定する。 値は0～255で指定。
     * 
     * @author eagle.sakura
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public void clearColorRGBA(int r, int g, int b, int a) {
        r = (r & 0xff) * 256 / 255;
        g = (g & 0xff) * 256 / 255;
        b = (b & 0xff) * 256 / 255;
        a = (a & 0xff) * 256 / 255;
        gl11.glClearColorx(r << 8, g << 8, b << 8, a << 8);
    }

    /**
     * 消去色を指定する。
     * @param rgba
     */
    public void clearColorRGBA(int rgba) {
        clearColorRGBA(Color.toColorR(rgba), Color.toColorG(rgba), Color.toColorB(rgba), Color.toColorA(rgba));
    }

    /**
     * 指定した４ｘ４行列をプッシュする。
     * 
     * @author eagle.sakura
     * @param trans
     */
    public void pushMatrixF(Matrix4x4 trans) {
        gl11.glPushMatrix();
        gl11.glMultMatrixf(trans.m, 0);
    }

    /**
     * 行列を取り出す。
     * 
     * @author eagle.sakura
     */
    public void popMatrix() {
        gl11.glPopMatrix();
    }

    public String toGlErrorInfo(int error) {
        String info = "glError :: " + error;

        switch (error) {
            case GL10.GL_NO_ERROR:
                info = "GL_NO_ERROR";
                break;
            case GL10.GL_OUT_OF_MEMORY:
                info = "GL_OUT_OF_MEMORY";
                break;
            case GL10.GL_INVALID_ENUM:
                info = "GL_INVALID_ENUM";
                break;
            default:
                break;
        }

        return info;
    }

    String toEglErrorInfo(int error) {
        String info = "eglError :: " + error;
        switch (error) {
            case EGL10.EGL_SUCCESS:
                info = "EGL_SUCCESS";
                break;
            case EGL10.EGL_BAD_SURFACE:
                info = "EGL_SUCCESS";
                break;
            case EGL10.EGL_BAD_ALLOC:
                info = "EGL_BAD_ALLOC";
                break;
            case EGL10.EGL_NOT_INITIALIZED:
                info = "EGL_NOT_INITIALIZED";
                break;
            case EGL10.EGL_BAD_ACCESS:
                info = "EGL_BAD_ACCESS";
                break;
            case EGL10.EGL_BAD_ATTRIBUTE:
                info = "EGL_BAD_ATTRIBUTE";
                break;
            case EGL10.EGL_BAD_CURRENT_SURFACE:
                info = "EGL_BAD_CURRENT_SURFACE";
                break;
            case EGL10.EGL_BAD_CONTEXT:
                info = "EGL_BAD_CONTEXT";
                break;
            case EGL10.EGL_BAD_NATIVE_WINDOW:
                info = "EGL_BAD_NATIVE_WINDOW";
                break;
            case EGL10.EGL_BAD_MATCH:
                info = "EGL_BAD_MATCH";
                break;
        }
        return info;
    }

    /**
     * エラー内容をログ出力し、SUCCESS以外ならtrueを返す。
     * @param error
     * @return
     */
    public boolean printGlError(int error) {
        if (error != GL10.GL_NO_ERROR) {
            LogUtil.log(toGlErrorInfo(error));
        }
        return error != GL10.GL_NO_ERROR;
    }

    /**
     * エラー内容をログ表示し、SUCCESS以外ならtrueを返す。
     * @return
     */
    public boolean printGlError() {
        return printGlError(gl11.glGetError());
    }

    /**
     * エラー内容をログ出力し、SUCCESS以外ならtrueを返す。
     * @param error
     * @return
     */
    boolean printEglError(int error) {
        if (error != EGL10.EGL_SUCCESS) {
            LogUtil.log(toEglErrorInfo(error));
        }
        return error != EGL10.EGL_SUCCESS;
    }

    /**
     * エラー内容をログ表示し、SUCCESS以外ならtrueを返す。
     * @return
     */
    boolean printEglError() {
        return printEglError(egl.eglGetError());
    }

    /**
     * Activity#onResume時に呼び出す。
     */
    public void onResume() {
        if (egl == null) {
            return;
        }

        refreshRenderSurface();
    }

    /**
     * Activity#onPauseで呼び出す。
     */
    public void onPause() {
        LogUtil.log("PauseOpenGL");
        if (eglSurface != null) {
            egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, eglContext);
            egl.eglDestroySurface(eglDisplay, eglSurface);
            eglSurface = null;
        }
    }

    /**
     * バックバッファをフロントバッファに送る。
     * 
     * @author eagle.sakura
     * @version 2009/11/14 : 新規作成
     */
    public void swapBuffers() {
        if (egl == null || eglDisplay == null || eglSurface == null) {
            return;
        }

        // 画面に出力するバッファの切り替え
        egl.eglGetError();
        if (!egl.eglSwapBuffers(eglDisplay, eglSurface)) {
            onResume();
            {
                egl.eglSwapBuffers(eglDisplay, eglSurface);
                if (printEglError()) {
                    throw new IllegalStateException("egl bad resume");
                }
            }
        }

    }

    /**
     * 
     * @param holder
     */
    public void setSurfaceHolder(SurfaceHolder holder) {
        LogUtil.log("" + holder);
        LogUtil.log("" + this.holder);
        this.holder = holder;
    }

    /**
     * サーフェイスを返す。
     * 
     * @returnx
     */
    public SurfaceHolder getSurfaceHolder() {
        return holder;
    }

    /**
     * 初期化時のコンフィグスペック。
     */
    private int[] configSpec = {
        /**
         * 2008/12/1 修正 以下の設定が実機では使えないようなのでカット。
         * この部分をはずすと、サポートされている設定が使われる(明示的に設定しないと機種依存で変わる可能性あり?)。
         * 
         * EGL10.EGL_RED_SIZE, 5, //! 赤要素：8ビット EGL10.EGL_GREEN_SIZE, 6, //!
         * 緑要素：8ビット EGL10.EGL_BLUE_SIZE, 5, //! 青要素：8ビット //
         * EGL10.EGL_ALPHA_SIZE, 8, //! アルファチャンネル：8ビット EGL10.EGL_DEPTH_SIZE, 16,
         * //! 深度バッファ：16ビット
         */
        EGL10.EGL_NONE
    //! 終端にはEGL_NONEを入れる
    };

    /**
     * 自動でコンフィグを設定する。
     * 
     * @param pixelFormat
     * @param depth
     */
    public void autoConfigSpec(int pixelFormat, boolean depth) {
        List<Integer> specs = new ArrayList<Integer>();

        if (pixelFormat == PixelFormat.RGB_565) {
            specs.add(EGL10.EGL_RED_SIZE);
            specs.add(5);
            specs.add(EGL10.EGL_GREEN_SIZE);
            specs.add(6);
            specs.add(EGL10.EGL_BLUE_SIZE);
            specs.add(5);
        } else if (pixelFormat == PixelFormat.RGB_888) {
            specs.add(EGL10.EGL_RED_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_GREEN_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_BLUE_SIZE);
            specs.add(8);
        } else if (pixelFormat == PixelFormat.RGBA_8888) {
            specs.add(EGL10.EGL_RED_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_GREEN_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_BLUE_SIZE);
            specs.add(8);
            specs.add(EGL10.EGL_ALPHA_SIZE);
            specs.add(8);
        }

        if (depth) {
            specs.add(EGL10.EGL_DEPTH_SIZE);
            specs.add(16);
        }

        specs.add(EGL10.EGL_SURFACE_TYPE);
        specs.add(EGL10.EGL_WINDOW_BIT);

        specs.add(EGL10.EGL_NONE);

        configSpec = new int[specs.size()];
        for (int i = 0; i < configSpec.length; ++i) {
            configSpec[i] = specs.get(i);
        }
    }

    /**
     * レンダリング用サーフェイスを新しく生成する。
     * @return
     */
    protected EGLSurface refreshRenderSurface() {

        if (eglSurface != null) {
            egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, eglContext);
            eglSurface = null;
        }

        {
            // コンフィグ設定
            final int CONFIG_MAX = 100;
            EGLConfig[] configs = new EGLConfig[CONFIG_MAX];
            int[] numConfigs = new int[1];

            //! コンフィグを全て取得する
            if (!egl.eglChooseConfig(eglDisplay, configSpec, configs, CONFIG_MAX, numConfigs)) {
                throw new IllegalStateException(toEglErrorInfo(egl.eglGetError()));
            }

            //! 必要なものを保存する
            final int num = numConfigs[0];
            for (int i = 0; i < num; ++i) {
                deviceConfigs.add(configs[i]);
            }
            eglConfig = deviceConfigs.get(0);
        }

        /*
         */
        for (EGLConfig config : deviceConfigs) {
            //            EGLConfig config = eglConfig;
            EGLSurface surface = egl.eglCreateWindowSurface(eglDisplay, config, holder, null);
            if (!printEglError()) {
                LogUtil.log("match surface!! " + (deviceConfigs.indexOf(config) + 1) + " / " + deviceConfigs.size());
                eglSurface = surface;
                eglConfig = config;
                egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
                return eglSurface;
            }
        }
        throw new IllegalStateException("cerate surface error");
    }

    /**
     * GL系を初期化する。
     * 
     * @author eagle.sakura
     * @version 2009/11/14 : 新規作成
     */
    public void initGL(Handler handler) {
        if (egl != null) {
            return;
        }
        this.glHandler = handler;

        // GL ES操作モジュール取得
        egl = (EGL10) EGLContext.getEGL();
        {
            // ディスプレイコネクション作成
            eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new IllegalStateException(toEglErrorInfo(egl.eglGetError()));
            }

            // ディスプレイコネクション初期化
            if (!egl.eglInitialize(eglDisplay, new int[2])) {
                throw new IllegalStateException(toEglErrorInfo(egl.eglGetError()));
            }
        }

        {
            // コンフィグ設定
            final int CONFIG_MAX = 100;
            EGLConfig[] configs = new EGLConfig[CONFIG_MAX];
            int[] numConfigs = new int[1];

            //! コンフィグを全て取得する
            if (!egl.eglChooseConfig(eglDisplay, configSpec, configs, CONFIG_MAX, numConfigs)) {
                throw new IllegalStateException(toEglErrorInfo(egl.eglGetError()));
            }

            //! 必要なものを保存する
            final int num = numConfigs[0];
            for (int i = 0; i < num; ++i) {
                deviceConfigs.add(configs[i]);
            }
            eglConfig = deviceConfigs.get(0);
        }

        {
            // レンダリングコンテキスト作成
            eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, null);
            if (eglContext == EGL10.EGL_NO_CONTEXT) {
                throw new IllegalStateException(toEglErrorInfo(egl.eglGetError()));
            }

            gl11 = (GL11) eglContext.getGL();
            gl11EP = (GL11ExtensionPack) eglContext.getGL();
        }

        {
            refreshRenderSurface();
        }

        //! デフォルトのステータスを設定する
        {
            //! 深度テスト有効
            gl11.glEnable(GL10.GL_DEPTH_TEST);
            gl11.glEnable(GL10.GL_BLEND);
            gl11.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

            //! カリング無効
            gl11.glDisable(GL10.GL_CULL_FACE);

            //! 行列無効化
            gl11.glMatrixMode(GL10.GL_PROJECTION);
            gl11.glLoadIdentity();
            gl11.glMatrixMode(GL10.GL_MODELVIEW);
            gl11.glLoadIdentity();

            //!
            gl11.glDisable(GL10.GL_LIGHTING);
        }

        printSpec();
    }

    private void printSpec() {

        int[] value = {
            -1
        };

        {
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_RED_SIZE, value);
            LogUtil.log("EGL_RED_SIZE : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_GREEN_SIZE, value);
            LogUtil.log("EGL_GREEN_SIZE : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_BLUE_SIZE, value);
            LogUtil.log("EGL_BLUE_SIZE : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_ALPHA_SIZE, value);
            LogUtil.log("EGL_ALPHA_SIZE : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_ALPHA_FORMAT, value);
            LogUtil.log("EGL_ALPHA_FORMAT : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_PIXEL_ASPECT_RATIO, value);
            LogUtil.log("EGL_PIXEL_ASPECT_RATIO : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_ALPHA_MASK_SIZE, value);
            LogUtil.log("EGL_ALPHA_MASK_SIZE : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_BUFFER_SIZE, value);
            LogUtil.log("EGL_BUFFER_SIZE : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_COLOR_BUFFER_TYPE, value);
            LogUtil.log("EGL_COLOR_BUFFER_TYPE : " + value[0]);
            egl.eglGetConfigAttrib(eglDisplay, eglConfig, EGL10.EGL_RENDERABLE_TYPE, value);
            LogUtil.log("EGL_RENDERABLE_TYPE : " + value[0]);

        }
        {
            LogUtil.log("GL_VENDOR : " + gl11.glGetString(GL10.GL_VENDOR));
            LogUtil.log("GL_RENDERER : " + gl11.glGetString(GL10.GL_RENDERER));
            LogUtil.log("GL_VERSION : " + gl11.glGetString(GL10.GL_VERSION));
        }
        //! 性能出力
        {
            gl11.glGetIntegerv(GL11Ext.GL_MAX_PALETTE_MATRICES_OES, value, 0);
            LogUtil.log("GL_MAX_PALETTE_MATRICES_OES : " + value[0]);

            gl11.glGetIntegerv(GL11Ext.GL_MAX_VERTEX_UNITS_OES, value, 0);
            LogUtil.log("GL_MAX_VERTEX_UNITS_OES : " + value[0]);

            gl11.glGetIntegerv(GL10.GL_MAX_MODELVIEW_STACK_DEPTH, value, 0);
            LogUtil.log("GL_MAX_MODELVIEW_STACK_DEPTH : " + value[0]);

            gl11.glGetIntegerv(GL10.GL_MAX_PROJECTION_STACK_DEPTH, value, 0);
            LogUtil.log("GL_MAX_PROJECTION_STACK_DEPTH : " + value[0]);
        }
    }

    /**
     * GLの終了処理を行う。
     * 
     * @author eagle.sakura
     */
    @Override
    public void dispose() {
        if (egl == null) {
            return;
        }

        try {
            // レンダリングコンテキストとの結びつけは解除
            egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            // サーフェイス破棄
            if (eglSurface != null) {
                egl.eglDestroySurface(eglDisplay, eglSurface);
                eglSurface = null;
            }

        } catch (Exception e) {
            LogUtil.log(e);
        }

        try {
            // レンダリングコンテキスト破棄
            if (eglContext != null) {
                egl.eglDestroyContext(eglDisplay, eglContext);
                eglContext = null;
            }

        } catch (Exception e) {
            LogUtil.log(e);
        }
        try {
            // ディスプレイコネクション破棄
            if (eglDisplay != null) {
                egl.eglTerminate(eglDisplay);
                eglDisplay = null;
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }

        egl = null;
    }

    /**
     * カメラ関連の行列をリセットし、単位行列化する。
     * 
     * @author eagle.sakura
     * @version 2010/09/18 : 新規作成
     */
    public void resetCamera() {
        gl11.glMatrixMode(GL10.GL_PROJECTION);
        // gl11.glPopMatrix();
        gl11.glLoadIdentity();
        gl11.glMatrixMode(GL10.GL_MODELVIEW);
    }

    /**
     * ワールド行列を単位行列化する。 このメソッド呼出後、GL_MODELVIEWが設定されている。
     */
    public void resetWorldMatrix() {
        gl11.glMatrixMode(GL10.GL_MODELVIEW);
        gl11.glLoadIdentity();
    }

    /**
     * フレームバッファを生成する。
     * @return
     */
    public int genFrameBufferObject() {
        int[] buffer = new int[1];
        gl11EP.glGenFramebuffersOES(1, buffer, 0);
        if (buffer[0] == 0) {
            printGlError();
            throw new IllegalStateException("buffer not create");
        }
        return buffer[0];
    }

    /**
     * フレームバッファを削除する。
     * 別スレッドから投げられた場合、GLスレッドにpostされる。
     * @param buffer
     */
    public void deleteFrameBufferObject(final int buffer) {
        if (!isGLThread()) {
            post(new Runnable() {
                @Override
                public void run() {
                    deleteFrameBufferObject(buffer);
                }
            });
            return;
        }

        gl11.glGetError();
        gl11EP.glDeleteFramebuffersOES(1, new int[] {
            buffer
        }, 0);
        if (printGlError()) {
            LogUtil.log("Buffer Delete Error :: " + buffer);
        }
    }

    /**
     * レンダリング用バッファを生成する。
     * @return
     */
    public int genRenderBuffer() {
        int[] buffer = new int[1];
        gl11EP.glGenRenderbuffersOES(1, buffer, 0);
        if (buffer[0] == 0) {
            printGlError();
            throw new IllegalStateException("buffer not create");
        }
        return buffer[0];
    }

    /**
     * レンダリングバッファを削除する
     * 別スレッドから投げられた場合、GLスレッドにpostされる。
     * @param buffer
     */
    public void deleteRenderBuffer(final int buffer) {
        if (!isGLThread()) {
            post(new Runnable() {
                @Override
                public void run() {
                    deleteRenderBuffer(buffer);
                }
            });
            return;
        }

        gl11.glGetError();
        gl11EP.glDeleteRenderbuffersOES(1, new int[] {
            buffer
        }, 0);

        if (printGlError()) {
            LogUtil.log("Buffer Delete Error :: " + buffer);
        }

    }

    /**
     * VBOのバッファをひとつ作成する。
     * 
     * @return
     */
    public int genVertexBufferObject() {
        int[] buf = new int[1];
        gl11.glGetError();
        gl11.glGenBuffers(1, buf, 0);
        if (gl11.glGetError() == GL11.GL_OUT_OF_MEMORY) {
            throw new OutOfMemoryError("glGenTexture Error");
        }

        return buf[0];
    }

    /**
     * VBOのバッファをひとつ削除する。
     * 別スレッドから投げられた場合、GLスレッドにpostされる。
     * 
     * @param vbo
     */
    public void deleteVertexBufferObject(final int vbo) {
        if (!isGLThread()) {
            post(new Runnable() {
                @Override
                public void run() {
                    deleteVertexBufferObject(vbo);
                }
            });
            return;
        }

        gl11.glGetError();
        gl11.glDeleteBuffers(1, new int[] {
            vbo
        }, 0);
        if (printGlError()) {
            LogUtil.log("Buffer Delete Error :: " + vbo);
        }
    }

    /**
     * テクスチャバッファをひとつ作成する。
     * 
     * @return
     */
    public int genTexture() {
        gl11.glGetError();
        int[] buf = new int[1];
        gl11.glGetError();
        gl11.glGenTextures(1, buf, 0);
        if (gl11.glGetError() == GL11.GL_OUT_OF_MEMORY) {
            throw new OutOfMemoryError("glGenTexture Error");
        }

        return buf[0];
    }

    /**
     * テクスチャバッファを削除する。
     * 
     * @param tex
     */
    public void deleteTexture(final int tex) {
        if (!isGLThread()) {
            glHandler.post(new Runnable() {
                @Override
                public void run() {
                    deleteTexture(tex);
                }
            });
        }

        gl11.glGetError();
        gl11.glDeleteTextures(1, new int[] {
            tex
        }, 0);
        if (printGlError()) {
            LogUtil.log("Texture Delete Error :: " + tex);
        }
    }

    /**
     * GLスレッドで何らかの処理を行わせる。
     * 主にファイナライザからの連携で利用する。
     * @param runable
     */
    private void post(Runnable runable) {
        if (glHandler != null) {
            glHandler.post(runable);
        } else {
            throw new IllegalArgumentException("GL Handler Not Found...");
        }
    }

    /**
     * GL用のスレッドかどうかを確認する。
     * @return
     */
    public boolean isGLThread() {
        if (glHandler == null) {
            throw new NullPointerException("GL Handler Not Found...");
        }
        return Thread.currentThread().equals(glHandler.getLooper().getThread());
    }

    /**
     * 描画エリアを補正する。
     * 
     * @param correction
     */
    public void updateDrawArea(VirtualDisplay correction) {
        RectF area = correction.getDrawingArea(new RectF());
        gl11.glViewport((int) area.left, (int) area.top, (int) area.width(), (int) area.height());
    }

    /**
     * 初期化完了していたらtrue
     * @return
     */
    public boolean isInitialized() {
        return gl11 != null;
    }

    /**
     * OpenGLが休止状態の場合はtrueを返す。
     * @return
     */
    public boolean isPaused() {
        return egl != null && eglSurface == null;
    }

    /**
     * OpenGLが活性化されている場合はtrue
     * @return
     */
    public boolean isRunning() {
        return egl != null && eglSurface != null && gl11 != null;
    }

    /**
     * 指定した配列をラッピングする。
     * @param buffer
     * @return
     */
    public static IntBuffer wrap(int[] buffer) {
        IntBuffer result = ByteBuffer.allocateDirect(buffer.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
        result.put(buffer).position(0);
        return result;
    }

    /**
     * 指定した配列をラッピングする。
     * @param buffer
     * @return
     */
    public static FloatBuffer wrap(float[] buffer) {
        FloatBuffer result = ByteBuffer.allocateDirect(buffer.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        result.put(buffer).position(0);
        return result;
    }

    /**
     * 指定した配列をラッピングする。
     * @param buffer
     * @return
     */
    public static ByteBuffer wrap(byte[] buffer) {
        ByteBuffer result = ByteBuffer.allocateDirect(buffer.length).order(ByteOrder.nativeOrder());
        result.put(buffer).position(0);
        return result;
    }

    /**
     * 指定した配列を色情報としてラッピングする。
     * 色はRGBAで配列されている必要がある。
     * @param buffer
     * @return
     */
    public static Buffer wrapColor(int[] buffer) {
        IntBuffer result = ByteBuffer.allocateDirect(buffer.length * 4).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        result.put(buffer).position(0);
        return result;
    }

    /**
     * 指定した配列をラッピングする。
     * @param buffer
     * @return
     */
    public static ShortBuffer wrap(short[] buffer) {
        ShortBuffer result = ByteBuffer.allocateDirect(buffer.length * 2).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        result.put(buffer).position(0);
        return result;
    }

    /**
     * GC対象管理クラス
     */
    private GLGarbageCollector garbageCollector = new GLGarbageCollector(this);

    /**
     * GC管理クラスを取得する。
     * @return
     */
    public GLGarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    /**
     * 解放対象のメモリを全て解放する。
     */
    public int gc() {
        return garbageCollector.gc();
    }

    /**
     * バックバッファの内容を撮影し、バッファに収める
     * @return
     */
    public Bitmap captureSurfaceRGB888(Rect area) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * area.width() * area.height());
        gl11.glReadPixels(area.left, area.top, area.width(), area.height(), GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buffer);
        buffer.position(0);

        byte[] source = new byte[4 * area.width() * area.height()];
        buffer.get(source);
        buffer = null;
        System.gc();

        Bitmap bmp = Bitmap.createBitmap(area.width(), area.height(), Config.ARGB_8888);

        for (int y = 0; y < area.height(); ++y) {
            for (int x = 0; x < area.width(); ++x) {
                int head = (bmp.getWidth() * y + x) * 4;
                bmp.setPixel(x, area.height() - y - 1, ((((int) source[head + 3]) & 0xff) << 24)
                        | ((((int) source[head + 0]) & 0xff) << 16) | ((((int) source[head + 1]) & 0xff) << 8)
                        | (((int) source[head + 2]) & 0xff));
            }
        }
        return bmp;
    }
}
