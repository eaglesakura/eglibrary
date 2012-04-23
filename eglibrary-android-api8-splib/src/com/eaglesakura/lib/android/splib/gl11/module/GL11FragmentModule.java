package com.eaglesakura.lib.android.splib.gl11.module;

import javax.microedition.khronos.opengles.GL11;

import android.app.Activity;
import android.os.Handler;
import android.view.SurfaceView;

import com.eaglesakura.lib.android.game.graphics.gl11.OpenGLManager;
import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;
import com.eaglesakura.lib.android.splib.fragment.GL11Fragment.GLRunnable;

/**
 * {@link GL11Fragment}内で利用する、更に細かいモジュールを定義する
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class GL11FragmentModule {
    GL11Fragment fragment;

    /**
     * Fragmentと関連付けられた
     * @param fragment
     */
    public void onAttach(GL11Fragment fragment) {
        this.fragment = fragment;
    }

    /**
     * Fragmentと切り離された
     */
    public void onDetatch() {
        this.fragment = null;
    }

    /**
     * レンダリングが行われた
     */
    public void onRendering() {

    }

    /**
     * モジュールをFragmentから切り離す
     */
    public void unbind() {

    }

    /**
     * 自身を管理しているFragmentを取得する
     * @return
     */
    public GL11Fragment getFragment() {
        return fragment;
    }

    /**
     * GL管理クラスを取得する
     * @return
     */
    public OpenGLManager getGLManager() {
        return fragment.getGLManager();
    }

    /**
     * GLインターフェースを取得する
     * @return
     */
    public GL11 getGL() {
        return getGLManager().getGL();
    }

    public SurfaceView getView() {
        return fragment.getGLView();
    }

    public Activity getActivity() {
        return fragment.getActivity();
    }

    public int getRenderAreaWidth() {
        return fragment.getRenderAreaWidth();
    }

    public int getRenderAreaHeight() {
        return fragment.getRenderAreaHeight();
    }

    /**
     * ハンドラを取得する
     * @return
     */
    public Handler getGLHandler() {
        return getGLManager().getHandler();
    }

    /**
     * GLスレッドで実行を行う
     * @param runnable
     */
    public void post(GLRunnable runnable) {
        fragment.post(runnable);
    }

    /**
     * GLスレッドで実行を行う
     * @param runnable
     * @param delay
     */
    public void postDelayed(GLRunnable runnable, long delay) {
        fragment.postDelayed(runnable, delay);
    }

    /**
     * GLスレッドで実行を行う
     * @param runnable
     * @param uptimeMS
     */
    public void postAtTime(GLRunnable runnable, long uptimeMS) {
        fragment.postAtTime(runnable, uptimeMS);
    }

    /**
     * GLの初期化を行った
     * @param width
     * @param height
     */
    public void onGLInitialized(int width, int height) {

    }

    /**
     * サーフェイスサイズが変更された
     * @param width
     * @param height
     */
    public void onGLSurfaceChanged(int width, int height) {

    }

    /**
     * GLの一時停止を行った
     */
    public void onGLSuspend() {

    }

    /**
     * GLの復帰を行った
     */
    public void onGLResume(int width, int height) {

    }

    /**
     * GLの解放を行った
     */
    public void onGLDispose() {

    }

    /**
     * Fragment自体が停止した
     */
    public void onFragmentSuspend() {

    }

    /**
     * Fragment自体がレジュームした
     */
    public void onFragmentResume() {

    }

    /**
     * OpenGLのGCを行わせる。
     */
    public void glgc() {
        getGLManager().gc();
    }
}
