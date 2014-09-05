package com.eaglesakura.android.glkit.egl;

import android.content.Context;
import android.content.res.AssetManager;

import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCMethod;

/**
 * Surface/Contextを1グループ化したクラス
 */
@JCClass(cppNamespace = "es.glkit")
public interface IEGLDevice {

    /**
     * GL処理対象としてバインドする
     *
     * @return バインドに成功したらtrue
     */
    @JCMethod
    boolean bind();

    /**
     * GL処理対象からアンバインドする
     */
    @JCMethod
    void unbind();

    /**
     * サーフェイスの大きさをピクセル単位で取得する
     */
    @JCMethod
    int getSurfaceWidth();

    /**
     * サーフェイスの大きさをピクセル単位で取得する
     */
    @JCMethod
    int getSurfaceHeight();

    /**
     * 廃棄処理を行う
     */
    @JCMethod
    void dispose();

    /**
     * ウィンドウデバイスであればtrue
     */
    @JCMethod
    boolean isWindowDevice();

    /**
     * Context管理グループを取得する
     */
    IEGLContextGroup getContextGroup();

    /**
     * いずれかのスレッドにバインドされていたらtrue
     *
     * @return
     */
    @JCMethod
    boolean isBinded();

    /**
     * 実行しているスレッドがbindを行ったthreadであればtrueを返す
     * <p/>
     * bindされていない、もしくは別スレッドでbindされていたらfalseを返す。
     */
    @JCMethod
    boolean isBindedThread();

    /**
     * オフスクリーンレンダリング用のサーフェイスを生成する。
     * 既にサーフェイスが存在する場合は開放を行う
     *
     * @param width  サーフェイスの幅ピクセル数
     * @param height サーフェイスの高さピクセル数
     */
    @JCMethod
    void createPBufferSurface(int width, int height);

    /**
     * サーフェイスサイズが変更された
     *
     * @param nativeWindow
     * @param newWidth
     * @param newHeight
     */
    void onSurfaceChanged(Object nativeWindow, int newWidth, int newHeight);

    /**
     * サーフェイスが廃棄された
     */
    void onSurfaceDestroyed();

    /**
     * 画面内に情報を反映する
     */
    @JCMethod
    void swapBuffers();

    /**
     * サーフェイスの廃棄リクエストがあるならばtrue
     *
     * @return
     */
    @JCMethod
    boolean hasSurfaceDestroyRequest();

    /**
     * レンダリング対象として有効なEGLSurfaceを持っていればtrue
     */
    @JCMethod
    public boolean hasSurface();

    /**
     * レンダリング可能なEGLContextを持っていたらtrue
     *
     * @return
     */
    @JCMethod
    public boolean hasContext();

    /**
     * ApplicationContextを取得する
     *
     * @return
     */
    @JCMethod
    public Context getApplicationContext();

    /**
     * AssetManagerを取得する
     *
     * @return
     */
    @JCMethod
    public AssetManager getAssetManager();
}
