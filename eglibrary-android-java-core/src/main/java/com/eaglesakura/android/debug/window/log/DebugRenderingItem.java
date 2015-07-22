package com.eaglesakura.android.debug.window.log;

import android.graphics.Rect;

import com.eaglesakura.android.debug.window.RealtimeDebugWindow;
import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.math.Vector2;

/**
 * デバッグレンダリング情報
 * <br>
 * 必ず一度は描画されるように調整される。
 */
public abstract class DebugRenderingItem {
    /**
     * 描画をDropする時刻
     * <br>
     * デフォルトは一度の描画でdrop
     */
    long dropTime = System.currentTimeMillis();

    /**
     * レンダリングフラグ
     */
    int flags = RealtimeDebugWindow.FLAG_RENDERING_POSITION_LEFT;

    /**
     * @return 描画をdropする場合はtrue
     */
    public boolean isDropMessage() {
        return System.currentTimeMillis() >= dropTime;
    }

    /**
     * @param checkFlags チェック対象のフラグ
     * @return 指定された全てのフラグが立っている場合true
     */
    public boolean hasFlags(int checkFlags) {
        return (flags & checkFlags) == checkFlags;
    }

    /**
     * デバッグフラグを指定する
     *
     * @param flags
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * レンダリングする時間を指定する
     *
     * @param timeMs
     */
    public void setRenderingTime(long timeMs) {
        dropTime = System.currentTimeMillis() + timeMs;
    }

    /**
     * レンダリング領域のサイズを取得する
     *
     * @param size
     */
    public abstract void getRenderingSize(Vector2 size);

    /**
     * レンダリングを行う
     *
     * @param graphics render target
     * @param x        描画位置X
     * @param y        描画位置Y
     */
    public abstract void rendering(Graphics graphics, int x, int y);

}
