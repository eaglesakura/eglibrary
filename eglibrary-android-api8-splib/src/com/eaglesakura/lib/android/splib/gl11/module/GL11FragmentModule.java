package com.eaglesakura.lib.android.splib.gl11.module;

import javax.microedition.khronos.opengles.GL11;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.SurfaceView;

import com.eaglesakura.lib.android.game.graphics.gl11.OpenGLManager;
import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;
import com.eaglesakura.lib.android.splib.fragment.gl11.GLRunnable;

/**
 * {@link GL11Fragment}内で利用する、更に細かいモジュールを定義する
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class GL11FragmentModule extends DisposableResource {
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
            fragment.removeModule(getTag().toString());
        }
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

    /**
     * GLを関連付けたViewを取得する
     * @return
     */
    public SurfaceView getView() {
        return fragment.getGLView();
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
     * GLスレッドで動作していたらtrueを返す
     * @return
     */
    public boolean isGLThread() {
        return fragment.isGLThread();
    }

    /**
     * UIスレッドで動作していたらtrueを返す
     * @return
     */
    public boolean isUIThread() {
        return GameUtil.isUIThread();
    }

    /**
     * ハンドラを取得する
     * @return
     */
    public Handler getGLHandler() {
        //        return getGLManager().getHandler();
        return null;
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
    public void onGLPause() {

    }

    /**
     * GLの復帰を行った
     */
    public void onGLResume() {

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
     * OpenGLのGCを行わせる。
     */
    public void gc() {
        //        getGLManager().gc();
    }
}
