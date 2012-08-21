package com.eaglesakura.lib.android.splib.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.eaglesakura.lib.android.game.graphics.gl11.OpenGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.GLRenderer;
import com.eaglesakura.lib.android.game.thread.UIHandler;
import com.eaglesakura.lib.android.splib.fragment.egl.EGLFragmentModuleGroup;

/**
 * EGL処理を行うFragment
 * @author TAKESHI YAMASHITA
 *
 */
public class EGLFragment extends Fragment {
    /**
     * レンダリング用のView
     */
    private SurfaceView surfaceView = null;

    /**
     * EGL処理クラス
     */
    private EGLManager egl = new EGLManager();

    /**
     * 初期化コールを行なっている場合true
     */
    private boolean egl_initialized = false;

    /**
     * GLレンダリング用のハンドラ。
     * 任意のスレッドに紐付けることができる。
     */
    private Handler renderingHandler = UIHandler.getInstance();

    /**
     * 登録されているモジュールグループ
     */
    private EGLFragmentModuleGroup rootModule = new EGLFragmentModuleGroup();

    /**
     * レンダリング処理を行うためのコールバック
     */
    private SurfaceHolder.Callback renderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    if (!egl_initialized) {
                        egl_initialized = true;
                        onEGLInitialized();
                    }

                    onEGLResume();
                }
            });
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            onEGLPause();
        }
    };

    public EGLFragment() {
        rootModule.onAttach(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        surfaceView = new SurfaceView(getActivity());
        FrameLayout layout = new FrameLayout(getActivity());
        layout.addView(surfaceView);

        egl.setSurfaceHolder(surfaceView.getHolder());
        surfaceView.getHolder().addCallback(renderCallback);
        return layout;
    }

    /**
     * ルートモジュールを取得する
     * @return
     */
    public EGLFragmentModuleGroup getRootModule() {
        return rootModule;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity().isFinishing()) {
            onEGLDestroy();
            egl.dispose();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rootModule.onDetatch();
    }

    /**
     * EGL管理クラスを取得する
     * @return
     */
    public EGLManager getEGL() {
        return egl;
    }

    /**
     * GPUを取得する
     * @return
     */
    public OpenGLManager getGPU() {
        return egl.getGPU();
    }

    /**
     * レンダリング用のハンドラを取得する
     * @return
     */
    public Handler getRenderingHandler() {
        return renderingHandler;
    }

    /**
     * レンダリング用のハンドラを指定する
     * @param renderingHandler
     */
    public void setRenderingHandler(Handler renderingHandler) {
        this.renderingHandler = renderingHandler;
    }

    /**
     * レンダリングエリア幅を取得する
     * @return
     */
    public int getRenderAreaWidth() {
        return surfaceView.getWidth();
    }

    /**
     * レンダリングエリア高さを取得する
     * @return
     */
    public int getRenderAreaHeight() {
        return surfaceView.getHeight();
    }

    /**
     * 描画用のSurfaceViewを取得する
     * @return
     */
    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    /**
     * EGLの初期化を完了した
     * @param egl
     */
    protected void onEGLInitialized() {
    }

    /**
     * EGLを一時的に停止させた
     * @param egl
     */
    protected void onEGLPause() {
        rootModule.onEGLPause();
    }

    /**
     * EGLを復旧させた
     * @param egl
     */
    protected void onEGLResume() {
        rootModule.onEGLResume();
    }

    /**
     * EGLを廃棄した
     */
    protected void onEGLDestroy() {
        rootModule.onEGLDispose();
    }

    /**
     * ワーキングスレッドに登録する
     * @param runnable
     */
    public void eglWork(final GLRenderer runnable) {
        eglWorkDelayed(runnable, 0);
    }

    /**
     * ワーキングスレッドに登録する
     * @param runnable
     */
    public void eglWorkDelayed(final GLRenderer render, long delay) {
        renderingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                egl.working(render);
            }
        }, delay);
    }

    /**
     * 指定時刻に実行を行う
     * @param render
     * @param time
     */
    public void eglWorkAtTime(final GLRenderer render, long uptimeMs) {
        renderingHandler.postAtTime(new Runnable() {
            @Override
            public void run() {
                egl.working(render);
            }
        }, uptimeMs);
    }

    /**
     * レンダリングを行わせる。
     */
    public void rendering() {
        renderingHandler.removeCallbacks(renderRunner);
        renderingHandler.post(renderRunner);
    }

    private Runnable renderRunner = new Runnable() {
        @Override
        public void run() {
            egl.rendering(renderer);
        }
    };

    /**
     * レンダラ
     */
    private GLRenderer renderer = new GLRenderer() {
        @Override
        public void onWorking(EGLManager egl) {
        }

        @Override
        public void onSurfaceReady(EGLManager egl) {
        }

        @Override
        public void onSurfaceNotReady(EGLManager egl) {
        }

        @Override
        public void onRendering(EGLManager egl) {
            onRenderingBegin();
            onRenderingMain();
            onRenderingEnd();
        }
    };

    protected void onRenderingBegin() {
        rootModule.onRenderingBegin();
    }

    protected void onRenderingMain() {
        rootModule.onRendering();
    }

    protected void onRenderingEnd() {
        rootModule.onRenderingEnd();
    }
}
