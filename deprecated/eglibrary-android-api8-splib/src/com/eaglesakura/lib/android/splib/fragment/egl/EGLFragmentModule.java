package com.eaglesakura.lib.android.splib.fragment.egl;

import com.eaglesakura.lib.android.game.graphics.gl11.GPU;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.GLRenderer;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.VRAM;
import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.SurfaceView;

import javax.microedition.khronos.opengles.GL11;

/**
 * {@link GL11Fragment}内で利用する、更に細かいモジュールを定義する
 *
 * @author TAKESHI YAMASHITA
 */
public abstract class EGLFragmentModule extends DisposableResource {
    private EGLFragment fragment;

    /**
     * Fragmentと関連付けられた
     */
    public void onAttach(EGLFragment fragment) {
        this.fragment = fragment;
    }

    /**
     * Fragmentと切り離された
     */
    public void onDetatch() {
        this.fragment = null;
    }

    /**
     * レンダリング開始を知らせる
     */
    public void onRenderingBegin() {

    }

    /**
     * レンダリングを行わせる
     */
    public void onRendering() {

    }

    /**
     * レンダリング完了を知らせる
     */
    public void onRenderingEnd() {

    }

    /**
     * Fragmentにattach済みだったらtrueを返す
     */
    public boolean isAttached() {
        return fragment != null && fragment.isAdded();
    }

    /**
     * モジュールをFragmentから切り離す
     */
    public void unbind() {
        if (isAttached()) {
            fragment.getRootModule().remove(this);
        }
    }

    /**
     * 自身を管理しているFragmentを取得する
     */
    public EGLFragment getFragment() {
        return fragment;
    }

    /**
     * EGL管理クラスを取得する
     */
    public EGLManager getEGL() {
        return fragment.getEGL();
    }

    /**
     * GPU管理クラスを取得する
     */
    public GPU getGPU() {
        return fragment.getGPU();
    }

    /**
     * VRAMを取得する
     */
    public VRAM getVRAM() {
        return getEGL().getVRAM();
    }

    /**
     * GLインターフェースを取得する
     */
    public GL11 getGL() {
        return fragment.getEGL().getGL();
    }

    /**
     * GLを関連付けたViewを取得する
     */
    public SurfaceView getView() {
        if (fragment == null) {
            return null;
        }
        return fragment.getSurfaceView();
    }

    /**
     * 関連付けられたActivityを取得する
     */
    public Activity getActivity() {
        if (fragment == null) {
            return null;
        }
        return fragment.getActivity();
    }

    /**
     * ApplicationContextを取得する
     */
    public Context getApplicationContext() {
        if (fragment == null) {
            return null;
        }
        return getActivity().getApplicationContext();
    }

    /**
     * レンダリングエリアの幅を取得する
     */
    public int getRenderAreaWidth() {
        return fragment.getRenderAreaWidth();
    }

    /**
     * レンダリングエリアの高さを取得する
     */
    public int getRenderAreaHeight() {
        return fragment.getRenderAreaHeight();
    }

    /**
     * UIスレッドで動作していたらtrueを返す
     */
    public boolean isUIThread() {
        return GameUtil.isUIThread();
    }

    /**
     * リソースリストを取得する
     */
    public Resources getResources() {
        return fragment.getResources();
    }

    /**
     * アセットアクセスを行う。
     */
    public AssetManager getAssets() {
        return fragment.getActivity().getAssets();
    }

    /**
     * GL実行を行い、結果が戻るまで待つ
     */
    public void work(final Runnable runnable) {
        post(new GLRenderer() {
            @Override
            public void onWorking(EGLManager egl) {
                runnable.run();
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
     * postを許可するタイミングである場合はtrue
     */
    protected boolean isPostExist() {
        return true;
    }

    /**
     * GLで実行を行う
     */
    public void post(GLRenderer runnable) {
        if (!isAttached()) {
            return;
        }

        if (!isPostExist()) {
            LogUtil.log("!isPostExist#post");
            fragment.addPendingRunner(runnable);
            return;
        }

        fragment.eglWork(runnable);
    }

    /**
     * GLで実行を行う
     */
    public void postDelayed(final GLRenderer runnable, final long delay) {
        if (!isAttached()) {
            return;
        }

        if (!isPostExist()) {
            LogUtil.log("!isPostExist#postDelayed");
            fragment.addPendingRunner(new GLRenderer() {
                @Override
                public void onWorking(EGLManager egl) {
                    postDelayed(runnable, delay);
                }

                @Override
                public void onSurfaceReady(EGLManager egl) {
                }

                @Override
                public void onSurfaceNotReady(EGLManager egl) {
                    fragment.addPendingRunner(this);
                }

                @Override
                public void onRendering(EGLManager egl) {
                }
            });
            return;
        }
        fragment.eglWorkDelayed(runnable, delay);
    }

    /**
     * GLで実行を行う
     */
    public void postAtTime(final GLRenderer runnable, final long uptimeMS) {
        if (isAttached()) {
            return;
        }
        if (!isPostExist()) {
            LogUtil.log("!isPostExist#postAtTime");
            fragment.addPendingRunner(new GLRenderer() {

                @Override
                public void onWorking(EGLManager egl) {
                    postAtTime(runnable, uptimeMS);
                }

                @Override
                public void onSurfaceReady(EGLManager egl) {

                }

                @Override
                public void onSurfaceNotReady(EGLManager egl) {
                    fragment.addPendingRunner(this);
                }

                @Override
                public void onRendering(EGLManager egl) {

                }
            });
            return;
        }
        fragment.eglWorkAtTime(runnable, uptimeMS);
    }

    /**
     * {@link GL11Fragment}へ、レンダリングを要求する
     */
    public void rendering() {
        if (isAttached()) {
            fragment.rendering();
        }
    }

    /**
     * サーフェイスサイズが変更された
     */
    public void onGLSurfaceChanged(int width, int height) {

    }

    /**
     * GLの一時停止を行った
     */
    public void onEGLPause() {

    }

    /**
     * GLの復帰を行った
     */
    public void onEGLResume() {

    }

    /**
     * GLの解放を行った
     */
    public void onEGLDispose() {

    }

    /**
     * Fragment自体が停止した
     */
    public void onFragmentSuspend() {

    }

    /**
     * Fragment自体が廃棄された
     */
    public void onFragmentDestroy() {

    }

    /**
     * Fragment自体がレジュームした
     */
    public void onFragmentResume() {

    }

    /**
     * キーイベントが呼ばれると必ず呼び出される
     */
    public void onKeyEvent(KeyEvent event) {

    }

    /**
     * キーボードが押された
     */
    public void onKeyDown(int keyCode, KeyEvent event) {

    }

    /**
     * キーボードが離された
     */
    public void onKeyUp(int keyCode, KeyEvent event) {

    }

    /**
     * メモリの解放を行う
     */
    @Override
    public abstract void dispose();

    /**
     * VRAMのGCを行わせる。
     */
    public void gc() {
        if (!isAttached()) {
            return;
        }

        final VRAM vram = getVRAM();
        if (vram != null) {
            work(new Runnable() {

                @Override
                public void run() {
                    vram.gc();
                }
            });
        }
    }

    /**
     * FragmentがResume済みだったらtrue
     */
    public boolean isFragmentResumed() {
        return fragment.isResumed();
    }

    /**
     * アプリ用文字列を取得する。
     */
    public String getString(int string_id) {
        return getResources().getString(string_id);
    }
}
