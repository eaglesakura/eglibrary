package com.eaglesakura.android.glkit.egl;

/**
 * Surface/Contextを1グループ化したクラス
 */
public interface IEGLDevice {

    /**
     * GL処理対象としてバインドする
     *
     * @return バインドに成功したらtrue
     */
    boolean bind();

    /**
     * GL処理対象からアンバインドする
     */
    void unbind();

    /**
     * サーフェイスの大きさをピクセル単位で取得する
     */
    int getSurfaceWidth();

    /**
     * サーフェイスの大きさをピクセル単位で取得する
     */
    int getSurfaceHeight();

    /**
     * 廃棄処理を行う
     */
    void dispose();

    /**
     * ウィンドウデバイスであればtrue
     */
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
    boolean isBinded();

    /**
     * 実行しているスレッドがbindを行ったthreadであればtrueを返す
     * <br>
     * bindされていない、もしくは別スレッドでbindされていたらfalseを返す。
     */
    boolean isBindedThread();

    /**
     * オフスクリーンレンダリング用のサーフェイスを生成する。
     * 既にサーフェイスが存在する場合は開放を行う
     *
     * @param width  サーフェイスの幅ピクセル数
     * @param height サーフェイスの高さピクセル数
     */
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
    void swapBuffers();

    /**
     * サーフェイスの廃棄リクエストがあるならばtrue
     *
     * @return
     */
    boolean hasSurfaceDestroyRequest();

    /**
     * レンダリング対象として有効なEGLSurfaceを持っていればtrue
     */
    boolean hasSurface();

    /**
     * レンダリング可能なEGLContextを持っていたらtrue
     *
     * @return
     */
    boolean hasContext();
}
