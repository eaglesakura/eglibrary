package com.eaglesakura.lib.android.game.scene;

public abstract class SceneBase {

    /**
     * シーンを開始したタイミングで呼び出される。
     * @param before
     */
    public abstract void onSceneStart(SceneManager manager, SceneBase before);

    /**
     * シーンが終了したタイミングで呼び出される。
     */
    public abstract void onSceneExit(SceneManager manager, SceneBase next);

    /**
     * フレームの更新を行う。
     * @return
     */
    public abstract void onFrameBegin(SceneManager manager);

    /**
     * フレームの描画を行う。
     */
    public abstract void onFrameDraw(SceneManager manager);

    /**
     * フレームの終了時に呼び出される。
     */
    public abstract void onFrameEnd(SceneManager manager);

    /**
     * Activityが一時停止した
     * @param manager
     */
    public abstract void onGamePause(SceneManager manager);

    /**
     * Activityが再開した。
     * @param manager
     */
    public abstract void onGameResume(SceneManager manager);
}
