/**
 *
 * @author eagle.sakura
 * @version 2009/12/11 : 新規作成
 */
package com.eaglesakura.lib.android.game.graphics.canvas;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;

/**
 * @author eagle.sakura
 * @version 2009/12/11 : 新規作成
 */
public class SurfaceCanvas extends Graphics {
    private SurfaceHolder holder = null;

    /**
     * @author eagle.sakura
     * @param target
     * @version 2009/11/29 : 新規作成
     */
    public SurfaceCanvas(SurfaceHolder holder) {
        setHolder(holder);
    }

    /**
     * 
     * @author eagle.sakura
     * @param holder
     * @version 2010/07/16 : 新規作成
     */
    public void setHolder(SurfaceHolder holder) {
        this.holder = holder;
    }

    /**
     * 描画の開始を明示する。
     * 
     * @author eagle.sakura
     * @version 2009/11/29 : 新規作成
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
     * 
     * @author eagle.sakura
     * @version 2009/11/29 : 新規作成
     */
    public void unlock() {
        holder.unlockCanvasAndPost(getCanvas());
        setCanvas(null);
    }
}
