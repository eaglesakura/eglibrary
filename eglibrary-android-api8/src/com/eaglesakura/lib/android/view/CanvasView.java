/**
 *
 * @author eagle.sakura
 * @version 2010/05/24 : 新規作成
 */
package com.eaglesakura.lib.android.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.SurfaceHolder;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.canvas.SurfaceCanvas;

/**
 * Canvasによる描画が可能なビュー。
 * 
 * @author eagle.sakura
 * @version 2010/05/24 : 新規作成
 */
public class CanvasView extends LooperSurfaceView {
    private SurfaceCanvas canvas = null;

    /**
     * 
     * @author eagle.sakura
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
     * 
     * @author eagle.sakura
     * @return
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
     * 
     * @author eagle.sakura
     * @return
     */
    public SurfaceCanvas getGraphics() {
        return canvas;
    }

    /**
     * サーフェイスの描画終了と転送を行う。
     * 
     * @author eagle.sakura
     */
    public void unlock() {
        canvas.unlock();
    }

}
