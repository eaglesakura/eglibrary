package com.eaglesakura.lib.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * 
 * 
 */
public class LooperSurfaceView extends SurfaceView implements SurfaceHolder.Callback, ILooperSurface {
    /**
     * サーフェイスの作成が完了したらtrue。
     */
    private boolean created = false;

    /**
     * ループを行うサーフェイスを扱う。
     * 
     * 
     * @param context
     * @param attribute
     * 
     */
    public LooperSurfaceView(Context context, AttributeSet attribute) {
        super(context, attribute);
        getHolder().addCallback(this);
    }

    /**
     * 
     * 
     * @param context
     * 
     */
    public LooperSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    /**
     * サーフェイスが作成されている場合、trueを返す。
     * 
     * 
     * @return
     * 
     */
    @Override
    public boolean isCreated() {
        return created;
    }

    /**
     * 終了処理を行う。
     * 
     * 
     * 
     */
    public void dispose() {

    }

    /**
     * サーフェイスが作成された。
     * 
     * 
     * @param arg0
     * 
     */
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        LogUtil.log("surfaceCreated : " + this);
    }

    /**
     * サーフェイスが変更された。
     * 
     * 
     * @param holder
     * @param format
     * @param width
     * @param height
     * 
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.log("surfaceChanged : " + this);
        created = true;
    }

    /**
     * サーフェイスが破棄された。
     * 
     * 
     * @param holder
     * 
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.log("surfaceDestroyed : " + this);
        created = false;
    }
}
