package com.eaglesakura.lib.android.splib.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.thread.UIHandler;

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
                        onEglInitialized(egl);
                    }

                    onEglResume(egl);
                }
            });
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            onEglPause(egl);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        surfaceView = new SurfaceView(getActivity());
        FrameLayout layout = new FrameLayout(getActivity());
        layout.addView(surfaceView);

        egl.setSurfaceHolder(surfaceView.getHolder());
        surfaceView.getHolder().addCallback(renderCallback);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity().isFinishing()) {
            egl.dispose();
        }
    }

    /**
     * EGL管理クラスを取得する
     * @return
     */
    public EGLManager getEgl() {
        return egl;
    }

    /**
     * EGLの初期化を完了した
     * @param egl
     */
    protected void onEglInitialized(EGLManager egl) {

    }

    /**
     * EGLを一時的に停止させた
     * @param egl
     */
    protected void onEglPause(EGLManager egl) {

    }

    /**
     * EGLを復旧させた
     * @param egl
     */
    protected void onEglResume(EGLManager egl) {

    }
}
