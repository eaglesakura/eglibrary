package com.eaglesakura.lib.android.game.loop;

import android.content.Context;

import com.eaglesakura.lib.android.game.graphics.gl11.OpenGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.SpriteManager;

public abstract class SpriteGameLoopManagerBase extends GameLoopManagerBase {
    SpriteManager spriteManager;
    int backgroundColor = 0x000000ff;

    /**
     * 背景色黒
     */
    public static final int eColorBlack = 0x000000ff;

    /**
     * 背景白
     */
    public static final int eColorWhite = 0xffffffff;

    /**
     * デバッグ用の適当な色。
     */
    public static final int eColorDEBUG = 0x00fefeff;

    public SpriteGameLoopManagerBase(Context context, ILoopParent loopParent) {
        super(context, loopParent);
    }

    /**
     * 背景色を設定する。
     * 
     * @param backgroundColor
     */
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * 背景色を取得する
     * @return
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    public SpriteManager getSpriteManager() {
        return spriteManager;
    }

    @Override
    protected void onGameInitialize() {
        spriteManager = new SpriteManager(getVirtualDisplay(), getGLManager());
    }

    @Override
    protected void onGameFinalize() {
        spriteManager.dispose();
        spriteManager = null;
    }

    /**
     * 描画前に呼ばれる。
     */
    protected abstract void onGameFrameBegin();

    /**
     * 描画時に呼ばれる。
     */
    protected abstract void onGameFrameDraw();

    /**
     * 描画終了時に呼ばれる。
     */
    protected abstract void onGameFrameEnd();

    @Override
    protected void onGameFrame() {
        onGameFrameBegin();

        final OpenGLManager glManager = getGLManager();
        glManager.clearColorRGBA(((backgroundColor)) >> 24 & 0xff, ((backgroundColor)) >> 16 & 0xff,
                ((backgroundColor)) >> 8 & 0xff, ((backgroundColor)) >> 0 & 0xff);
        glManager.clear();

        spriteManager.begin();
        {
            onGameFrameDraw();
        }
        spriteManager.end();
        glManager.swapBuffers();

        onGameFrameEnd();
    }
}
