package com.eaglesakura.lib.android.view;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.canvas.SurfaceCanvas;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;

/**
 * Canvasによる描画が可能なビュー。
 */
public class CanvasView extends LooperSurfaceView {
    private SurfaceCanvas canvas = null;

    /**
     *
     *
     * @param context
     */
    public CanvasView(Context context) {
        super(context);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        canvas = new SurfaceCanvas(null);
        canvas.setHolder(getHolder());
    }

    /**
     * サーフェイスのロックを行う。
     */
    public boolean lock(VirtualDisplay display) {
        boolean result = canvas.lock(display);
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        super.surfaceCreated(arg0);
    }

    /**
     * 描画用ラッパーを取得する。
     */
    public SurfaceCanvas getGraphics() {
        return canvas;
    }

    /**
     * サーフェイスの描画終了と転送を行う。
     */
    public void unlock() {
        canvas.unlock();
    }

}
