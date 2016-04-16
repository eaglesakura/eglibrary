package com.eaglesakura.lib.android.view;

import com.eaglesakura.lib.android.game.graphics.gl11.GPU;
import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.util.ContextUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

import android.content.Context;
import android.view.SurfaceHolder;

/**
 *
 *
 */
@Deprecated
public class OpenGLView extends LooperSurfaceView {
    /**
     * OGL管理。
     */
    GPU gpu = new GPU(null);

    Context context = null;

    /**
     *
     *
     * @param context
     *
     */
    public OpenGLView(Context context) {
        super(context);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
        this.context = context;
    }

    /**
     * サーフェイスが作成された。
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Vector2 v = ContextUtil.getDisplaySize(context, new Vector2());
        if (holder.getSurfaceFrame().width() != 0 && holder.getSurfaceFrame().height() != 0) {
            v.set(holder.getSurfaceFrame().width(), holder.getSurfaceFrame().height());
        }
        LogUtil.log("DisplaySize : " + v);
        super.surfaceCreated(holder);
    }

    /**
     * GL管理クラスを取得する。
     */
    public GPU getGLManager() {
        return gpu;
    }

    int pixelFormat = 0;

    public int getPixelFormat() {
        return pixelFormat;
    }

    /**
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
        this.pixelFormat = format;
        LogUtil.log("GL Pixel Format : " + pixelFormat);
        //! 廃棄済みだったら、明示的にレジュームを行う。
        super.surfaceChanged(holder, format, width, height);
    }

    /**
     * サーフェイスが破棄されている場合trueを返す。 呼び出し後、破棄フラグはリセットされる。
     */
    public boolean isDestroyed() {
        boolean result = destroyed;
        destroyed = false;
        return result;
    }

    boolean destroyed = false;

    /**
     * サーフェイスが破棄された。
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        destroyed = true;
    }
}
