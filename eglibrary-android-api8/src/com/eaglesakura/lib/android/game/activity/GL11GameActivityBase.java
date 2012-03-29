package com.eaglesakura.lib.android.game.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.eaglesakura.lib.android.game.loop.GameLoopManagerBase;
import com.eaglesakura.lib.android.game.loop.GameLoopManagerBase.ILoopParent;
import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.util.ContextUtil;

/**
 * OpenGLES 1.1を利用したゲーム用のActivity 作成後すぐにループを開始する。
 * 
 * @author Takeshi
 * 
 */
public abstract class GL11GameActivityBase extends Activity implements ILoopParent {
    GameLoopManagerBase gameLoopManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameLoopManager = createGameLoopManager();
        {
            Vector2 realDisplaySize = ContextUtil.getDisplaySize(this, new Vector2());
            gameLoopManager.getVirtualDisplay().setRealDisplaySize(realDisplaySize.x, realDisplaySize.y);
        }
        {
            Vector2 virtualDeviceSize = getVirtualDisplaySize();
            gameLoopManager.getVirtualDisplay().setVirtualDisplaySize(virtualDeviceSize.x, virtualDeviceSize.y);
        }
        setContentView(gameLoopManager.getRootView());
    }

    /**
     * GLとゲームサイクル処理を行う。
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (gameLoopManager != null) {
            gameLoopManager.onResume();
        }
    }

    /**
     * GLのサスペンドとゲームサイクル処理を行う。
     */
    @Override
    protected void onPause() {
        if (gameLoopManager != null) {
            gameLoopManager.onPause();
        }
        super.onPause();
    }

    /**
     * Activityを終了する場合はtrueを返す。
     */
    @Override
    public boolean isFinished() {
        return super.isFinishing();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gameLoopManager.getMultiTouchInput().onTouchEvent(event);
    }

    /**
     * ゲームループクラスを作成する。
     * 
     * @return
     */
    protected abstract GameLoopManagerBase createGameLoopManager();

    /**
     * 仮想ディスプレイの幅・高さを取得する。
     * 
     * @return
     */
    protected abstract Vector2 getVirtualDisplaySize();

    /**
     * Activity名称を取得する。<BR>
     * この返却値はサブクラス名がセットされる。
     * @return クラス名（デフォルトの挙動をする場合のみ）
     */
    public String name() {
        String title = "" + this;
        title = title.substring(title.lastIndexOf(".") + 1);
        title = title.substring(0, title.lastIndexOf("@"));
        return title;
    }
}
