/**
 *
 * @author eagle.sakura
 * @version 2010/05/25 : 新規作成
 */
package com.eaglesakura.lib.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * @author eagle.sakura
 * @version 2010/05/25 : 新規作成
 */
public class LooperSurfaceView extends SurfaceView implements SurfaceHolder.Callback, ILooperSurface {
    /**
     * サーフェイスの作成が完了したらtrue。
     */
    private boolean created = false;

    /**
     * ループを行うサーフェイスを扱う。
     * 
     * @author eagle.sakura
     * @param context
     * @param attribute
     * @version 2010/05/25 : 新規作成
     */
    public LooperSurfaceView(Context context, AttributeSet attribute) {
        super(context, attribute);
        getHolder().addCallback(this);
    }

    /**
     * 
     * @author eagle.sakura
     * @param context
     * @version 2010/05/30 : 新規作成
     */
    public LooperSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    /**
     * サーフェイスが作成されている場合、trueを返す。
     * 
     * @author eagle.sakura
     * @return
     * @version 2010/05/25 : 新規作成
     */
    @Override
    public boolean isCreated() {
        return created;
    }

    /**
     * 終了処理を行う。
     * 
     * @author eagle.sakura
     * @version 2010/05/25 : 新規作成
     */
    public void dispose() {

    }

    /**
     * サーフェイスが作成された。
     * 
     * @author eagle.sakura
     * @param arg0
     * @version 2010/05/24 : 新規作成
     */
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        LogUtil.log("surfaceCreated : " + this);
    }

    /**
     * サーフェイスが変更された。
     * 
     * @author eagle.sakura
     * @param holder
     * @param format
     * @param width
     * @param height
     * @version 2010/05/24 : 新規作成
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.log("surfaceChanged : " + this);
        created = true;
    }

    /**
     * サーフェイスが破棄された。
     * 
     * @author eagle.sakura
     * @param holder
     * @version 2010/05/24 : 新規作成
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.log("surfaceDestroyed : " + this);
        created = false;
    }
}
