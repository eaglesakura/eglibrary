package com.eaglesakura.lib.android.splib.fragment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.eaglesakura.lib.android.game.graphics.gl11.OpenGLManager;
import com.eaglesakura.lib.android.game.thread.AsyncHandler;
import com.eaglesakura.lib.android.game.thread.ThreadSyncRunnerBase;
import com.eaglesakura.lib.android.splib.fragment.GL11Fragment.GLRunnable.Error;
import com.eaglesakura.lib.android.splib.gl11.module.GL11FragmentModule;
import com.eaglesakura.lib.android.view.OpenGLView;

/**
 * OpenGL用のFragment。
 * GLは専用スレッドが作られ、Handlerで管理される。
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class GL11Fragment extends IntentFragment {
    private OpenGLView glView = null;
    private AsyncHandler handler = null;

    /**
     * 画面復帰した時に実行させるQueue
     */
    private List<GLRunnable> suspendQueue = new LinkedList<GL11Fragment.GLRunnable>();

    /**
     * 内部で利用しているモジュール一覧
     */
    private List<GL11FragmentModule> modules = new LinkedList<GL11FragmentModule>();

    /**
     * GL管理クラス。
     */
    protected OpenGLManager glManager = null;

    private final Callback surfaceCallback = new Callback() {

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {
            (new ThreadSyncRunnerBase<Void>(handler) {
                @Override
                public Void onOtherThreadRun() throws Exception {
                    onSurfaceDestroyed(holder);
                    return null;
                }
            }).run();
        }

        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            (new ThreadSyncRunnerBase<Void>(handler) {
                @Override
                public Void onOtherThreadRun() throws Exception {
                    onSurfaceCreated(holder);
                    return null;
                }
            }).run();
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            (new ThreadSyncRunnerBase<Void>(handler) {
                @Override
                public Void onOtherThreadRun() throws Exception {
                    onSurfaceChanged(holder, format, width, height);
                    return null;
                }
            }).run();
        }
    };

    public GL11Fragment() {
    }

    public GL11Fragment(Intent intent) {
        super(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = AsyncHandler.createInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        glView = new OpenGLView(getActivity());
        glView.getHolder().addCallback(surfaceCallback);
        glManager = glView.getGLManager();

        FrameLayout layout = new FrameLayout(getActivity());
        layout.addView(glView);
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.dispose();
    }

    /**
     * OpenGL用のViewを取得する。
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}が呼び出されるまではnullを返す。
     * @return
     */
    public SurfaceView getGLView() {
        return glView;
    }

    /**
     * GL管理クラスを取得する。
     * @return
     */
    public OpenGLManager getGLManager() {
        return glManager;
    }

    /**
     * OpenGL用のハンドラを取得する。
     * @return
     */
    public AsyncHandler getHandler() {
        return handler;
    }

    /**
     * サーフェイスが作成された
     * このメソッドはGLスレッドから呼び出される。
     * @param holder
     */
    protected void onSurfaceCreated(SurfaceHolder holder) {
    }

    /**
     * サーフェイスの領域が変更になった。
     * このメソッドはGLスレッドから呼び出される。
     * @param holder
     * @param w
     * @param h
     */
    protected void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!glManager.isInitialized()) {
            glManager.initGL(handler);
            onGLInitialize(width, height);
        } else {
            if (glView.isDestroyed()) {
                glManager.onResume();
                onGLResume();
            }
        }

        // サーフェイスが変更された
        onGLSurfaceChanged(width, height);
        // 復帰用のQueueを実行する
        runSuspendQueue();
    }

    /**
     * サーフェイスが廃棄された
     * このメソッドはGLスレッドから呼び出される。
     * @param holder
     */
    protected void onSurfaceDestroyed(SurfaceHolder holder) {
        if (getActivity() == null || getActivity().isFinishing()) {
            onGLDispose();
            glManager.dispose();
        } else {
            onGLPause();
            glManager.onPause();
        }
    }

    public int getRenderAreaWidth() {
        return getGLView().getWidth();
    }

    public int getRenderAreaHeight() {
        return getGLView().getHeight();
    }

    /**
     * モジュールを追加する
     * @param module
     */
    public void addModule(final GL11FragmentModule module, String tag) {
        synchronized (modules) {
            if (modules.contains(module) || findModuleByTag(tag) != null) {
                return;
            }
            module.setTag(tag);
            modules.add(module);
        }

        post(new AutoRetryableGLRunnler() {
            @Override
            public void run() {
                module.onAttach(GL11Fragment.this);
            }
        });
    }

    /**
     * tagからモジュールを検索する
     * @param tag
     * @return
     */
    public GL11FragmentModule findModuleByTag(String tag) {

        synchronized (modules) {
            Iterator<GL11FragmentModule> iterator = modules.iterator();
            while (iterator.hasNext()) {
                GL11FragmentModule module = iterator.next();
                if (module.getTag().equals(tag)) {
                    return module;
                }
            }
            return null;
        }
    }

    /**
     * モジュールを分離する
     * @param tag
     * @return
     */
    public GL11FragmentModule removeModule(String tag) {

        synchronized (modules) {
            Iterator<GL11FragmentModule> iterator = modules.iterator();
            while (iterator.hasNext()) {
                final GL11FragmentModule module = iterator.next();
                if (module.getTag().equals(tag)) {
                    iterator.remove();

                    // GLスレッドで動作させる
                    post(new AutoRetryableGLRunnler() {
                        @Override
                        public void run() {
                            module.onDetatch();
                            module.dispose();
                        }
                    });
                    return module;
                }
            }
            return null;
        }
    }

    /**
     * レンダリング実行用のランナー
     */
    private final Runnable renderRunner = new Runnable() {
        @Override
        public void run() {
            onRendering();
        }
    };

    /**
     * レンダリングを行う。
     * レンダリングは同期的に行われず、GLスレッドにpostされる。
     */
    public void rendering() {
        if (glView == null) {
            throw new IllegalStateException("is not bind glView");
        }

        handler.removeCallbacks(renderRunner);
        handler.post(renderRunner);
    }

    /**
     * GLRunnableの実行を行わせる。
     * @param runnable
     */
    protected void runGLRunnable(GLRunnable runnable) {
        // 初期化されていない
        if (glManager == null || !glManager.isInitialized()) {
            runnable.onError(Error.NotInitialized, this);
            return;
        }
        // 廃棄済み
        if (getActivity() == null) {
            runnable.onError(Error.Disposed, this);
            return;
        }
        // GL一時停止中
        if (glManager.isPaused()) {
            runnable.onError(Error.Paused, this);
            return;
        }

        // 実行可能なため、実行を行わせる。
        runnable.run();
    }

    /**
     * 直接実行させる。
     * @param runnable
     */
    public void post(final GLRunnable runnable) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                runGLRunnable(runnable);
            }
        });
    }

    /**
     * 時間差で実行を行わせる。
     * @param runnable
     * @param delay
     */
    public void postDelayed(final GLRunnable runnable, long delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runGLRunnable(runnable);
            }
        }, delay);
    }

    /**
     * 指定時刻に動作を行わせる
     * @param runnable
     * @param uptimeMS
     */
    public void postAtTime(final GLRunnable runnable, long uptimeMS) {
        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                runGLRunnable(runnable);
            }
        }, uptimeMS);
    }

    /**
     * 復帰用のQueueへ追加する
     * @param runnable
     */
    public void addSuspendQueue(final GLRunnable runnable) {
        synchronized (suspendQueue) {
            suspendQueue.add(runnable);
        }
    }

    /**
     * 待機列に入れていたキューを実行する。
     */
    public void runSuspendQueue() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                synchronized (suspendQueue) {
                    Iterator<GLRunnable> iterator = suspendQueue.iterator();
                    while (iterator.hasNext()) {
                        GLRunnable glRunnable = iterator.next();
                        runGLRunnable(glRunnable);
                    }
                    suspendQueue.clear();
                }
            }
        });
    }

    /**
     * レンダリング時に呼び出される。
     * このメソッドは必ずGLスレッドから呼び出される。
     */
    protected abstract void onRendering();

    /**
     * GLメモリの廃棄を行わせる。
     */
    protected abstract void onGLDispose();

    /**
     * GLの初期化を行わせる。
     * @param width
     * @param height
     */
    protected abstract void onGLInitialize(int width, int height);

    /**
     * GLの描画サイズが変更になった。
     * @param width
     * @param height
     */
    protected abstract void onGLSurfaceChanged(int width, int height);

    /**
     * GLを休止状態にする。
     */
    protected abstract void onGLPause();

    /**
     * GLの状態を再開させる。
     */
    protected abstract void onGLResume();

    /**
     * GLスレッドでの実行を行わせる。
     * onErrorが呼び出された場合、
     * {@link #run()}は実行されない。
     * @author TAKESHI YAMASHITA
     *
     */
    public interface GLRunnable extends Runnable {
        public enum Error {
            /**
             * 初期化されていない
             */
            NotInitialized,

            /**
             * 既にGLが廃棄されている。
             */
            Disposed,

            /**
             * GLが休止状態にある。
             */
            Paused,
        }

        public void onError(Error error, GL11Fragment fragment);
    }

    /**
     * 自動でリトライを行うGLランナー
     * @author TAKESHI YAMASHITA
     *
     */
    public static abstract class AutoRetryableGLRunnler implements GLRunnable {

        @Override
        public void onError(Error error, GL11Fragment fragment) {
            switch (error) {
                case Disposed:
                    break;
                case NotInitialized:
                    fragment.addSuspendQueue(this);
                    break;
                case Paused:
                    fragment.addSuspendQueue(this);
                    break;
                default:
                    break;
            }
        }
    }
}
