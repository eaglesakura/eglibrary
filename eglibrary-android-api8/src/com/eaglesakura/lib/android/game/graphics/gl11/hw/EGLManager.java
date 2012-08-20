package com.eaglesakura.lib.android.game.graphics.gl11.hw;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL11;

import android.view.SurfaceHolder;

import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * OpenGL管理を行う。<BR>
 * {@link #gc()}等のリソース管理も行う。
 */
public class EGLManager extends DisposableResource {
    /**
     * EGL初期化状態を取得する
     */
    public enum EGLStatus_e {
        /**
         * 初期化が終わっていない状態
         */
        EGLStatus_NotInitialized {
            @Override
            public boolean isRunning() {
                return false;
            }
        },

        /**
         * どのスレッドにもコンテキストがアタッチされていない状態
         */
        EGLStatus_Ready {
            @Override
            public boolean isRunning() {
                return true;
            }
        },

        /**
         * 現在のスレッドにアタッチされている
         */
        EGLStatus_Attached {
            @Override
            public boolean isRunning() {
                return true;
            }
        },

        /**
         * 別なスレッドにアタッチされている
         */
        EGLStatus_Busy {
            @Override
            public boolean isRunning() {
                return true;
            }
        },

        /**
         * EGLの初期化が終わっているが、描画できない状態にある場合
         */
        EGLStatus_Suspend {
            @Override
            public boolean isRunning() {
                return false;
            }
        };

        /**
         * GLが実行可能な状態にある場合true
         * @return
         */
        public abstract boolean isRunning();
    };

    /**
     * 管理しているサーフェイス。
     */
    SurfaceHolder holder = null;

    /**
     * GL10本体。
     */
    EGL10 egl = null;

    /**
     * GL本体。
     */
    private GL11 gl11 = null;

    /**
     * レンダリング用コンテキスト
     * GLのステートはEGLContextを通じて保存される。
     */
    EGLContextManager context = null;

    /**
     * ディスプレイ用のサーフェイス
     */
    EGLDisplaySurfaceManager displaySurface = null;

    /**
     * ディスプレイ。
     */
    EGLDisplay display = null;

    /**
     * GLを関連付けているスレッド
     */
    Thread glThread = null;

    /**
     * VRAMに確保されたオブジェクトを管理する
     */
    VRAM vram = null;

    /**
     * GL描画用スレッドを作成する。
     * 
     * 
     * @param holder
     */
    public EGLManager() {
    }

    /**
     * GLオブジェクトを取得する
     * @return
     */
    public GL11 getGL() {
        return gl11;
    }

    /**
     * 現在のEGLの状態を取得する
     * @return
     */
    private EGLStatus_e getStatus() {
        // ディスプレイを得ていなければ初期化されていない
        if (display == null) {
            return EGLStatus_e.EGLStatus_NotInitialized;
        }

        // サーフェイスの準備ができていなければレンダリング状態ではない
        if (displaySurface == null) {
            return EGLStatus_e.EGLStatus_Suspend;
        }

        // スレッドIDを得ていなければ、どのスレッドにも属していない
        if (glThread == null) {
            return EGLStatus_e.EGLStatus_Ready;
        }

        // 現在のスレッドIDを取得
        Thread current_id = Thread.currentThread();

        // 同一スレッドだったらAttached、それ以外のスレッドだったらBusy
        return glThread == current_id ? EGLStatus_e.EGLStatus_Attached : EGLStatus_e.EGLStatus_Busy;
    }

    /**
     * EGLとContextの関連付けを行う
     */
    private EGLStatus_e bind() {
        //        egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        EGLStatus_e status = getStatus();

        // 既にアタッチ済みの場合は何もさせない
        if (status == EGLStatus_e.EGLStatus_Attached) {
            return status;
        }

        // ready状態だったらバインドを行う
        if (status == EGLStatus_e.EGLStatus_Ready) {
            if (egl.eglMakeCurrent(display, displaySurface.getSurface(), displaySurface.getSurface(),
                    context.getContext())) {
                // 現在のスレッドIDを指定する
                glThread = Thread.currentThread();
            }
        }

        return getStatus();
    }

    /**
     * EGLとContextの関連付けを解除する
     */
    private void unbind() {
        if (egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)) {
        } else {
            LogUtil.log("context unbind failed = " + context);
        }
        glThread = null;
    }

    /**
     * レンダリングを行わせる
     * @param renderer
     */
    public void rendering(GLRenderer renderer) {
        synchronized (GPU.gpu_lock) {

            // レンダリング開始を行う
            if (this.bind() == EGLStatus_e.EGLStatus_Attached) {
                // サーフェイスの準備ができたことを通知する
                renderer.onSurfaceReady(this);

                // レンダリングを行う
                renderer.onRendering(this);

                // コマンドの終了待ちを行う
                //                glFinish();
                gl11.glFinish();

                // バックバッファからフロントバッファへ転送する
                swapBuffers();

                // バインドを解除
                this.unbind();
                // レンダリング終了
                //                jclog("end rendering");
                return;
            }
        }

        // ここまで流れてきたら、エラー処理を行わせる
        renderer.onSurfaceNotReady(this);
    }

    /**
     * レンダリングを行わせる
     * @param renderer
     */
    public void working(GLRenderer renderer) {
        synchronized (GPU.gpu_lock) {

            // レンダリング開始を行う
            if (this.bind() == EGLStatus_e.EGLStatus_Attached) {
                // サーフェイスの準備ができたことを通知する
                renderer.onSurfaceReady(this);

                // レンダリングを行う
                renderer.onRendering(this);

                // コマンドの終了待ちを行う
                //                glFinish();
                gl11.glFinish();

                // バインドを解除
                this.unbind();
                // レンダリング終了
                //                jclog("end rendering");
                return;
            }
        }

        // ここまで流れてきたら、エラー処理を行わせる
        renderer.onSurfaceNotReady(this);
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
     * バックバッファをフロントバッファに送る。
     */
    private void swapBuffers() {
        // 画面に出力するバッファの切り替え
        egl.eglGetError();
        if (!egl.eglSwapBuffers(display, displaySurface.getSurface())) {
            if (printEglError()) {
                throw new IllegalStateException("egl bad resume");
            }
        }

    }

    /**
     * EGL系処理を自動的に設定する
     */
    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            synchronized (GPU.gpu_lock) {
                if (!isInitialized()) {
                    // 未初期化だから初期化を行う
                    context = new EGLContextManager();
                    context.autoConfigSpec(format, true);

                    // egl初期化を行う
                    initialize();
                } else {
                    // ディスプレイサーフェイスの復旧を行う
                    displaySurface = new EGLDisplaySurfaceManager(EGLManager.this);
                }
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            synchronized (GPU.gpu_lock) {
                if (displaySurface != null) {
                    displaySurface.dispose();
                    displaySurface = null;
                }
            }
        }
    };

    /**
     * 
     * @param holder
     */
    public void setSurfaceHolder(SurfaceHolder holder) {
        LogUtil.log("" + holder);
        LogUtil.log("" + this.holder);
        this.holder = holder;

        // コールバックを登録する
        holder.removeCallback(surfaceCallback);
        holder.addCallback(surfaceCallback);
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
     * GL系を初期化する。
     */
    private void initialize() {
        if (getStatus() != EGLStatus_e.EGLStatus_NotInitialized) {
            return;
        }

        // GL ES操作モジュール取得
        egl = (EGL10) EGLContext.getEGL();
        {
            // ディスプレイコネクション作成
            display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (display == EGL10.EGL_NO_DISPLAY) {
                throw new IllegalStateException(toEglErrorInfo(egl.eglGetError()));
            }

            // ディスプレイコネクション初期化
            if (!egl.eglInitialize(display, new int[2])) {
                throw new IllegalStateException(toEglErrorInfo(egl.eglGetError()));
            }
        }

        // コンテキスト作成
        {
            context.createContext(this);
            gl11 = (GL11) context.getContext().getGL();
        }
        // レンダリングサーフェイス作成
        {
            displaySurface = new EGLDisplaySurfaceManager(this);
        }
        // VRAMの初期化を行う
        {
            vram = new VRAM(this);
        }

        // コンテキストリセット
        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
    }

    /**
     * VRAMを取得する
     * @return
     */
    VRAM getVRAM() {
        return vram;
    }

    /**
     * GLの終了処理を行う。
     */
    @Override
    public void dispose() {

        synchronized (GPU.gpu_lock) {
            if (!isInitialized()) {
                return;
            }

            // 残っている資源を削除する
            if (vram != null) {
                this.bind();
                {
                    vram.dispose();
                    vram = null;
                }
                this.unbind();
            }

            if (displaySurface != null) {
                displaySurface.dispose();
                displaySurface = null;
            }

            if (context != null) {
                context.dispose();
                context = null;
            }

            if (display != null) {
                egl.eglTerminate(display);
                display = null;
            }

            egl = null;
        }
    }

    /**
     * GL用のスレッドかどうかを確認する。
     * @return
     */
    public boolean isGLThread() {
        if (glThread == null) {
            return false;
        }

        return Thread.currentThread().equals(glThread);
    }

    /**
     * 初期化完了していたらtrue
     * @return
     */
    public boolean isInitialized() {
        synchronized (GPU.gpu_lock) {
            return getStatus() != EGLStatus_e.EGLStatus_NotInitialized;
        }
    }

    /**
     * OpenGLが休止状態の場合はtrueを返す。
     * @return
     */
    public boolean isSuspend() {
        synchronized (GPU.gpu_lock) {
            return getStatus() == EGLStatus_e.EGLStatus_Suspend;
        }
    }

    /**
     * OpenGLが活性化されている場合はtrue
     * @return
     */
    public boolean isRunning() {
        synchronized (GPU.gpu_lock) {
            return getStatus().isRunning();
        }
    }

}
