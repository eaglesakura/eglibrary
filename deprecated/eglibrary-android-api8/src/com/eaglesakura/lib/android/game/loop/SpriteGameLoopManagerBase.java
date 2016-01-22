package com.eaglesakura.lib.android.game.loop;

import com.eaglesakura.lib.android.game.graphics.gl11.GPU;
import com.eaglesakura.lib.android.game.graphics.gl11.SpriteManager;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.android.game.util.Timer;

import android.content.Context;

@Deprecated
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
     */
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * 背景色を取得する
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

    /**
     * 定期的なGCを行うためのタイマ。
     */
    private Timer gcTimer = new Timer();

    /**
     * GCを行う間隔。
     */
    private int gcIntervalMs = 1000 * 60;

    /**
     * GCを行う間隔を指定する。
     * このGCにはOpenGL ESの不要リソース廃棄も含まれる。
     * 0以下を設定することで、自動的なgcを行わなくする。
     * デフォルトは1分間隔。
     */
    public void setGcIntervalMs(int gcIntervalMs) {
        this.gcIntervalMs = gcIntervalMs;
    }

    protected void updateGC() {
        // 一定時間以上経過していたらGC
        if (gcIntervalMs > 0 && gcTimer.end() > gcIntervalMs) {
            gcTimer.start();
            int gcItems = egl.getVRAM().gc(); // GLのGCを行う
            LogUtil.log("OpenGL ES Auto GC :: " + gcTimer.end() + " ms = " + gcItems + " resources");
            gcTimer.start(); // タイマーの開始時刻をリセットする
        }
    }

    @Override
    protected void onGameFrame() {
        onGameFrameBegin();

        final GPU gpu = getGLManager();
        gpu.clearColorRGBA(((backgroundColor)) >> 24 & 0xff, ((backgroundColor)) >> 16 & 0xff,
                ((backgroundColor)) >> 8 & 0xff, ((backgroundColor)) >> 0 & 0xff);
        gpu.clear();

        spriteManager.begin();
        {
            onGameFrameDraw();
        }
        spriteManager.end();
        //        glManager.swapBuffers();
        updateGC();
        onGameFrameEnd();
    }
}
