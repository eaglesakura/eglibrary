package com.eaglesakura.lib.android.splib.fragment;

import com.eaglesakura.lib.android.game.graphics.gl11.GPU;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.GLRenderer;
import com.eaglesakura.lib.android.game.thread.UIHandler;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.android.splib.fragment.egl.EGLFragmentModule;
import com.eaglesakura.lib.android.splib.fragment.egl.EGLFragmentModuleGroup;
import com.eaglesakura.lib.list.OrderAccessList;
import com.eaglesakura.lib.list.OrderAccessList.Iterator;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * EGL処理を行うFragment
 *
 * @author TAKESHI YAMASHITA
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
     * onResume時に行う処理を保留しておく
     */
    private OrderAccessList<GLRenderer> pendingWorker = new OrderAccessList<GLRenderer>();

    /**
     * レンダリング処理を行うためのコールバック
     */
    private SurfaceHolder.Callback renderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            LogUtil.log(String.format("egl::surfaceChanged fmt(%d) surface %d x %d", format, width, height));
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    egl.working(new GLRenderer() {
                        @Override
                        public void onWorking(EGLManager egl) {
                            if (!egl_initialized) {
                                egl_initialized = true;
                                LogUtil.log("egl::initialize");
                                onEGLInitialized();
                            }

                            onEGLResume();
                        }

                        @Override
                        public void onSurfaceReady(EGLManager egl) {
                        }

                        @Override
                        public void onSurfaceNotReady(EGLManager egl) {
                        }

                        @Override
                        public void onRendering(EGLManager egl) {
                        }
                    });
                }
            });
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            LogUtil.log("egl::surfaceCreated");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            LogUtil.log("egl::surfaceDestroyed");
            onEGLPause();
        }
    };

    public EGLFragment() {
        rootModule.onAttach(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        surfaceView = createSurfaceView();
        FrameLayout layout = new FrameLayout(getActivity());
        layout.addView(surfaceView);

        egl.setSurfaceHolder(surfaceView.getHolder());
        surfaceView.getHolder().addCallback(renderCallback);
        return layout;
    }

    /**
     * SurfaceViewを作成する。
     */
    protected SurfaceView createSurfaceView() {
        return new SurfaceView(getActivity());
    }

    /**
     * ルートモジュールを取得する
     */
    public EGLFragmentModuleGroup getRootModule() {
        return rootModule;
    }

    /**
     * 保留リストに追加する
     */
    public void addPendingRunner(GLRenderer worker) {
        pendingWorker.add(worker);
    }

    /**
     * モジュールを追加する
     */
    public void addModule(EGLFragmentModule module) {
        rootModule.addModule(module);
    }

    /**
     * モジュールを追加する
     */
    public void addModule(EGLFragmentModule module, Object tag) {
        rootModule.addModule(module);
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
    public void onPause() {
        rootModule.onFragmentSuspend();
        super.onPause();
    }

    @Override
    public void onResume() {
        rootModule.onFragmentResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        rootModule.onFragmentDestroy();
        super.onDestroy();
    }

    /**
     * EGL管理クラスを取得する
     */
    public EGLManager getEGL() {
        return egl;
    }

    /**
     * GPUを取得する
     */
    public GPU getGPU() {
        return egl.getGPU();
    }

    /**
     * レンダリング用のハンドラを取得する
     */
    public Handler getRenderingHandler() {
        return renderingHandler;
    }

    /**
     * レンダリング用のハンドラを指定する
     */
    public void setRenderingHandler(Handler renderingHandler) {
        this.renderingHandler = renderingHandler;
    }

    /**
     * レンダリングエリア幅を取得する
     */
    public int getRenderAreaWidth() {
        return surfaceView.getWidth();
    }

    /**
     * レンダリングエリア高さを取得する
     */
    public int getRenderAreaHeight() {
        return surfaceView.getHeight();
    }

    /**
     * 描画用のSurfaceViewを取得する
     */
    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    /**
     * EGLの初期化を完了した
     */
    protected void onEGLInitialized() {
    }

    /**
     * EGLを一時的に停止させた
     */
    protected void onEGLPause() {
        rootModule.onEGLPause();
    }

    /**
     * 保留してあるworkerを全て実行する。
     * {@link #addPendingRunner(GLRenderer)}で追加された全てを実行する。
     */
    public void runPendingWorkers() {
        // 保留リストを実行させる
        egl.working(new GLRenderer() {

            @Override
            public void onWorking(EGLManager egl) {
                Iterator<GLRenderer> iterator = pendingWorker.iterator();
                while (iterator.hasNext()) {
                    GLRenderer glRenderer = iterator.next();
                    egl.working(glRenderer);
                }
                pendingWorker.clear();
            }

            @Override
            public void onSurfaceReady(EGLManager egl) {

            }

            @Override
            public void onSurfaceNotReady(EGLManager egl) {

            }

            @Override
            public void onRendering(EGLManager egl) {

            }
        });
    }

    /**
     * EGLを復旧させた
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
     */
    public void eglWork(final GLRenderer runnable) {
        eglWorkDelayed(runnable, 0);
    }

    /**
     * ワーキングスレッドに登録する
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
     * レンダリングは指定スレッドにpostされるため、すぐに制御が戻る。
     */
    public void rendering() {
        renderingHandler.removeCallbacks(renderRunner);
        renderingHandler.post(renderRunner);
    }

    /**
     * 今すぐにレンダリングを行う。
     * レンダリングが完了するまで、制御は戻らない。
     */
    public void renderingNow() {
        egl.rendering(renderer);
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

    /**
     * イベントを直接指定して送信する
     */
    public void onDispatchKeyEvent(final int keyCode, final int action) {
        egl.working(new GLRenderer() {
            @Override
            public void onWorking(EGLManager egl) {
                // サブモジュールにライフサイクルを伝える
                rootModule.onKeyEvent(null);
                switch (action) {
                    case KeyEvent.ACTION_DOWN:
                        rootModule.onKeyDown(keyCode, null);
                        break;
                    case KeyEvent.ACTION_UP:
                        rootModule.onKeyUp(keyCode, null);
                        break;
                }
            }

            @Override
            public void onSurfaceReady(EGLManager egl) {
            }

            @Override
            public void onSurfaceNotReady(EGLManager egl) {
            }

            @Override
            public void onRendering(EGLManager egl) {
            }
        });
    }

    /**
     * キーイベントを呼び出す。
     * 呼び出さない場合は特に機能しない。
     */
    public void onDispatchKeyEvent(final KeyEvent key) {
        final int keyCode = key.getKeyCode();
        final int action = key.getAction();

        egl.working(new GLRenderer() {

            @Override
            public void onWorking(EGLManager egl) {
                // サブモジュールにライフサイクルを伝える
                rootModule.onKeyEvent(key);
                switch (action) {
                    case KeyEvent.ACTION_DOWN:
                        rootModule.onKeyDown(keyCode, key);
                        break;
                    case KeyEvent.ACTION_UP:
                        rootModule.onKeyUp(keyCode, key);
                        break;
                }
            }

            @Override
            public void onSurfaceReady(EGLManager egl) {
            }

            @Override
            public void onSurfaceNotReady(EGLManager egl) {
            }

            @Override
            public void onRendering(EGLManager egl) {
            }
        });
    }

}
