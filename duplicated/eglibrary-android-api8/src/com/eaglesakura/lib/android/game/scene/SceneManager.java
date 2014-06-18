package com.eaglesakura.lib.android.game.scene;

/**
 * シーン遷移を管理する。
 * @author TAKESHI YAMASHITA
 *
 */
public class SceneManager {
    SceneBase current = null;
    SceneBase next = null;

    public SceneManager(SceneBase startScene) {
        next = startScene;
    }

    /**
     * 次のフレームに起動するSceneを設定する。
     * 各ゲームフレーム終了時にチェックされ、nextが指定されている場合そのsceneへシーン切り替えを行う。
     * @param next
     */
    public void setNextScene(SceneBase next) {
        this.next = next;
    }

    void changeScene() {
        if (next != null) {
            //! 次のシーンが設定されていれば変更を行う
            if (current != null) {
                current.onSceneExit(this, next);
            }

            next.onSceneStart(this, current);
            current = next;
            next = null;
        }

    }

    /**
     * シーンの更新を行わせる。
     */
    public void onFrameBegin() {
        changeScene();
        current.onFrameBegin(this);
    }

    /**
     * シーンの描画を行わせる。
     */
    public void onFrameDraw() {
        current.onFrameDraw(this);
    }

    /**
     * フレームの終了時、必要であればシーンの変更を行う。
     */
    public void onFrameEnd() {
        current.onFrameEnd(this);
        changeScene();
    }

    public void onGamePause() {
        current.onGamePause(this);
    }

    public void onGameResume() {
        current.onGameResume(this);
    }
}
