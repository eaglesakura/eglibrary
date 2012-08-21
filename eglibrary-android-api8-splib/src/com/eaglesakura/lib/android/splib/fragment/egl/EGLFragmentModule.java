package com.eaglesakura.lib.android.splib.fragment.egl;

import javax.microedition.khronos.opengles.GL11;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.SurfaceView;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.GLRenderer;
import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;

/**
 * {@link GL11Fragment}内で利用する、更に細かいモジュールを定義する
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class EGLFragmentModule extends DisposableResource {
    EGLFragment fragment;

    /**
     * Fragmentと関連付けられた
     * @param fragment
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
     * @return
     */
    public boolean isAttached() {
        return fragment != null;
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
     * @return
     */
    public EGLFragment getFragment() {
        return fragment;
    }

    /**
     * EGL管理クラスを取得する
     * @return
     */
    public EGLManager getEGL() {
        return fragment.getEGL();
    }

    /**
     * GLインターフェースを取得する
     * @return
     */
    public GL11 getGL() {
        return fragment.getEGL().getGL();
    }

    /**
     * GLを関連付けたViewを取得する
     * @return
     */
    public SurfaceView getView() {
        return fragment.getSurfaceView();
    }

    /**
     * 関連付けられたActivityを取得する
     * @return
     */
    public Activity getActivity() {
        return fragment.getActivity();
    }

    /**
     * レンダリングエリアの幅を取得する
     * @return
     */
    public int getRenderAreaWidth() {
        return fragment.getRenderAreaWidth();
    }

    /**
     * レンダリングエリアの高さを取得する
     * @return
     */
    public int getRenderAreaHeight() {
        return fragment.getRenderAreaHeight();
    }

    /**
     * UIスレッドで動作していたらtrueを返す
     * @return
     */
    public boolean isUIThread() {
        return GameUtil.isUIThread();
    }

    /**
     * リソースリストを取得する
     * @return
     */
    public Resources getResources() {
        return fragment.getResources();
    }

    /**
     * アセットアクセスを行う。
     * @return
     */
    public AssetManager getAssets() {
        return fragment.getActivity().getAssets();
    }

    /**
     * GLスレッドで実行を行う
     * @param runnable
     */
    public void post(GLRenderer runnable) {
        fragment.eglWork(runnable);
    }

    /**
     * GLスレッドで実行を行う
     * @param runnable
     * @param delay
     */
    public void postDelayed(GLRenderer runnable, long delay) {
        fragment.eglWorkDelayed(runnable, delay);
    }

    /**
     * GLスレッドで実行を行う
     * @param runnable
     * @param uptimeMS
     */
    public void postAtTime(GLRenderer runnable, long uptimeMS) {
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
     * @param width
     * @param height
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
     * Fragment自体がレジュームした
     */
    public void onFragmentResume() {

    }

    /**
     * キーイベントが呼ばれると必ず呼び出される
     * @param event
     */
    public void onKeyEvent(KeyEvent event) {

    }

    /**
     * キーボードが押された
     * @param keyCode
     * @param event
     */
    public void onKeyDown(int keyCode, KeyEvent event) {

    }

    /**
     * キーボードが離された
     * @param keyCode
     * @param event
     */
    public void onKeyUp(int keyCode, KeyEvent event) {

    }

    /**
     * メモリの解放を行う
     */
    @Override
    public void dispose() {
    }

    /**
     * VRAMのGCを行わせる。
     */
    public void gc() {
        getEGL().getVRAM().gc();
    }
}
