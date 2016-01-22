package com.eaglesakura.lib.android.view;

import com.eaglesakura.lib.android.game.util.LogUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
     */
    @Override
    public boolean isCreated() {
        return created;
    }

    /**
     * 終了処理を行う。
     */
    public void dispose() {

    }

    /**
     * サーフェイスが作成された。
     */
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        LogUtil.log("surfaceCreated : " + this);
    }

    /**
     * サーフェイスが変更された。
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.log("surfaceChanged : " + this);
        created = true;
    }

    /**
     * サーフェイスが破棄された。
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.log("surfaceDestroyed : " + this);
        created = false;
    }
}
