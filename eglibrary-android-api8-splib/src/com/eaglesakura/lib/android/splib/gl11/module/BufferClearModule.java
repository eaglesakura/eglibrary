package com.eaglesakura.lib.android.splib.gl11.module;

import android.content.Intent;

import com.eaglesakura.lib.android.game.graphics.gl11.OpenGLManager;
import com.eaglesakura.lib.android.game.thread.UIHandler;

/**
 * 背景クリアとswapを行うモジュール
 * @author TAKESHI YAMASHITA
 *
 */
public class BufferClearModule extends GL11FragmentModule {
    int color = 0xFFFFFFFF;

    Intent swapErrorBootIntent = null;

    /**
     * 
     * @param clearColorRGBA
     */
    public BufferClearModule(int clearColorRGBA) {
        this.color = clearColorRGBA;
    }

    @Override
    public void onRenderingBegin() {
        super.onRenderingBegin();
        OpenGLManager glManager = getGLManager();
        glManager.clearColorRGBA(color);
        glManager.clear();
    }

    @Override
    public void onRenderingEnd() {
        super.onRenderingEnd();

        try {
            getGLManager().swapBuffers();
        } catch (Exception e) {
            onSwapError(e);
        }
    }

    /**
     * swap()失敗時に立ち上げるActivityのIntentを設定する
     * @param swapErrorBootIntent
     * @return
     */
    public BufferClearModule setSwapErrorBootIntent(Intent swapErrorBootIntent) {
        this.swapErrorBootIntent = swapErrorBootIntent;
        return this;
    }

    /**
     * 背景のswapに失敗した。
     * 標準の場合、intentからActivityを立ち上げる
     * @param e
     */
    protected void onSwapError(Exception e) {
        if (swapErrorBootIntent == null) {
            return;
        }

        UIHandler.postUI(new Runnable() {
            @Override
            public void run() {
                getActivity().startActivity(swapErrorBootIntent);
            }
        });
    }

    @Override
    public void dispose() {

    }

}
