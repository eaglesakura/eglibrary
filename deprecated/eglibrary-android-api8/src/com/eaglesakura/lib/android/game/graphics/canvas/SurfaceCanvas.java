package com.eaglesakura.lib.android.game.graphics.canvas;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * SurfaceViewでのGraphics管理
 *
 * @author TAKESHI YAMASHITA
 */
public class SurfaceCanvas extends Graphics {
    private SurfaceHolder holder = null;

    /**
     * @param holder
     */
    public SurfaceCanvas(SurfaceHolder holder) {
        setHolder(holder);
    }

    /**
     *
     *
     * @param holder
     *
     */
    public void setHolder(SurfaceHolder holder) {
        this.holder = holder;
    }

    /**
     * 描画の開始を明示する。
     */
    public boolean lock(VirtualDisplay display) {
        setCanvas(holder.lockCanvas());
        boolean result = getCanvas() != null;

        if (result) {
            clearMatrix();
            Matrix m = new Matrix();
            Rect area = display.getDrawingArea(new Rect());
            m.postTranslate(area.left, area.top);
            m.postScale(display.getDeviceScaling(), display.getDeviceScaling());
            setWidth(area.width());
            setHeight(area.height());
            loadMatrix(m);
        }

        return result;
    }

    /**
     * サーフェイス情報をフロントバッファに転送する。
     */
    public void unlock() {
        holder.unlockCanvasAndPost(getCanvas());
        setCanvas(null);
    }
}
