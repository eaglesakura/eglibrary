/**
 *
 * @author eagle.sakura
 * @version 2010/05/30 : 新規作成
 */
package com.eaglesakura.lib.android.view;

import android.content.Context;
import android.view.SurfaceHolder;

import com.eaglesakura.lib.android.game.graphics.gl11.OpenGLManager;
import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.util.ContextUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * @author eagle.sakura
 * @version 2010/05/30 : 新規作成
 */
public class OpenGLView extends LooperSurfaceView {
    /**
     * OGL管理。
     */
    OpenGLManager glManager = new OpenGLManager();

    Context context = null;

    /**
     * 
     * @author eagle.sakura
     * @param context
     * @version 2010/05/30 : 新規作成
     */
    public OpenGLView(Context context) {
        super(context);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
        glManager.setSurfaceHolder(getHolder());
        this.context = context;
    }

    /**
     * サーフェイスが作成された。
     * 
     * @author eagle.sakura
     * @param holder
     * @version 2010/05/30 : 新規作成
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
     * 
     * @author eagle.sakura
     * @return
     * @version 2010/05/30 : 新規作成
     */
    public OpenGLManager getGLManager() {
        return glManager;
    }

    int pixelFormat = 0;

    public int getPixelFormat() {
        return pixelFormat;
    }

    /**
     * 
     * @author eagle.sakura
     * @param holder
     * @param format
     * @param width
     * @param height
     * @version 2010/05/30 : 新規作成
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.pixelFormat = format;
        LogUtil.log("GL Pixel Format : " + pixelFormat);
        glManager.autoConfigSpec(format, true);
        Vector2 v = ContextUtil.getDisplaySize(context, new Vector2());
        v.set(width, height);
        glManager.setSurfaceHolder(holder);

        //! 廃棄済みだったら、明示的にレジュームを行う。
        super.surfaceChanged(holder, format, width, height);
    }

    /**
     * サーフェイスが破棄されている場合trueを返す。 呼び出し後、破棄フラグはリセットされる。
     * 
     * @return
     */
    public boolean isDestroyed() {
        boolean result = destroyed;
        destroyed = false;
        return result;
    }

    boolean destroyed = false;

    /**
     * サーフェイスが破棄された。
     * 
     * @author eagle.sakura
     * @param holder
     * @version 2010/05/30 : 新規作成
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        destroyed = true;
    }
}
