package com.eaglesakura.lib.android.game.graphics.gl11;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.graphics.RectF;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.Color;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.VRAM;
import com.eaglesakura.lib.android.game.math.Matrix4x4;
import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * OpenGL管理を行う。<BR>
 * {@link #gc()}等のリソース管理も行う。
 */
public class GPU {

    /**
     * GL本体。
     */
    private GL11 gl11 = null;

    /**
     * 関連付けられたEGL
     */
    private EGLManager egl = null;

    /**
     * GL描画用スレッドを作成する。
     * 
     * 
     * @param holder
     */
    public GPU(EGLManager egl) {
        this.egl = egl;
        gl11 = egl.getGL();
        //! デフォルトのStateを設定する
        {
            //! 深度テスト有効
            gl11.glEnable(GL10.GL_DEPTH_TEST);
            gl11.glEnable(GL10.GL_BLEND);
            gl11.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

            //! カリング無効
            gl11.glDisable(GL10.GL_CULL_FACE);

            //! 行列無効化
            gl11.glMatrixMode(GL10.GL_PROJECTION);
            gl11.glLoadIdentity();
            gl11.glMatrixMode(GL10.GL_MODELVIEW);
            gl11.glLoadIdentity();

            //!
            gl11.glDisable(GL10.GL_LIGHTING);

            gl11.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        }
    }

    /**
     * メインで使用するGLインターフェースを取得する。
     * 
     * 
     * @return
     */
    public GL11 getGL() {
        return gl11;
    }

    /**
     * VRAM領域を取得する
     * @return
     */
    public VRAM getVRAM() {
        return egl.getVRAM();
    }

    public EGLManager getEGL() {
        return egl;
    }

    /**
     * 通常合成
     */
    public static final int BLEND_ALPHA_NORMAL = 0;

    /**
     * アルファ合成
     */
    public static final int BLEND_ALPHA_ADD = 1;

    /**
     * 減算合成
     */
    public static final int BLEND_ALPHA_SUB = 2;

    /**
     * 乗算合成
     */
    public static final int BLEND_ALPHA_MUL = 3;

    /**
     * 色加算合成
     */
    public static final int BLEND_COLOR_ADD = 4;

    /**
     * 
     * @param mode
     */
    public void setBlendMode(int mode) {
        switch (mode) {
            case BLEND_ALPHA_NORMAL:
                gl11.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case BLEND_ALPHA_ADD:
                gl11.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
                break;
            case BLEND_ALPHA_SUB:
                break;
            case BLEND_ALPHA_MUL:
                gl11.glBlendFunc(GL10.GL_DST_COLOR, GL10.GL_ZERO);
                break;
            case BLEND_COLOR_ADD:
                gl11.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);
                break;
            default:
                break;
        }
    }

    /**
     * バッファの消去を行う。
     * 
     * 
     * @param mask
     */
    public void clear(int mask) {
        gl11.glClear(mask);
    }

    /**
     * バッファの消去を行う。<BR>
     * color / depthの両方をクリアする。
     * 
     * 
     */
    public void clear() {
        gl11.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_STENCIL_BUFFER_BIT);
    }

    /**
     * 消去色を設定する。
     * 
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public void clearColorRGBA(float r, float g, float b, float a) {
        gl11.glClearColor(r, g, b, a);
    }

    /**
     * 消去色を指定する。 値は0～255で指定。
     * 
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public void clearColorRGBA(int r, int g, int b, int a) {
        r = (r & 0xff) * 256 / 255;
        g = (g & 0xff) * 256 / 255;
        b = (b & 0xff) * 256 / 255;
        a = (a & 0xff) * 256 / 255;
        gl11.glClearColorx(r << 8, g << 8, b << 8, a << 8);
    }

    /**
     * 消去色を指定する。
     * @param rgba
     */
    public void clearColorRGBA(int rgba) {
        clearColorRGBA(Color.toColorR(rgba), Color.toColorG(rgba), Color.toColorB(rgba), Color.toColorA(rgba));
    }

    /**
     * 指定した４ｘ４行列をプッシュする。
     * 
     * 
     * @param trans
     */
    public void pushMatrixF(Matrix4x4 trans) {
        gl11.glPushMatrix();
        gl11.glMultMatrixf(trans.m, 0);
    }

    /**
     * 行列を取り出す。
     * 
     * 
     */
    public void popMatrix() {
        gl11.glPopMatrix();
    }

    public String toGlErrorInfo(int error) {
        String info = "glError :: " + error;

        switch (error) {
            case GL10.GL_NO_ERROR:
                info = "GL_NO_ERROR";
                break;
            case GL10.GL_OUT_OF_MEMORY:
                info = "GL_OUT_OF_MEMORY";
                break;
            case GL10.GL_INVALID_ENUM:
                info = "GL_INVALID_ENUM";
                break;
            default:
                break;
        }

        return info;
    }

    String toEglErrorInfo(int error) {
        String info = "eglError :: " + error;
        switch (error) {
            case EGL10.EGL_SUCCESS:
                info = "EGL_SUCCESS";
                break;
            case EGL10.EGL_BAD_SURFACE:
                info = "EGL_SUCCESS";
                break;
            case EGL10.EGL_BAD_ALLOC:
                info = "EGL_BAD_ALLOC";
                break;
            case EGL10.EGL_NOT_INITIALIZED:
                info = "EGL_NOT_INITIALIZED";
                break;
            case EGL10.EGL_BAD_ACCESS:
                info = "EGL_BAD_ACCESS";
                break;
            case EGL10.EGL_BAD_ATTRIBUTE:
                info = "EGL_BAD_ATTRIBUTE";
                break;
            case EGL10.EGL_BAD_CURRENT_SURFACE:
                info = "EGL_BAD_CURRENT_SURFACE";
                break;
            case EGL10.EGL_BAD_CONTEXT:
                info = "EGL_BAD_CONTEXT";
                break;
            case EGL10.EGL_BAD_NATIVE_WINDOW:
                info = "EGL_BAD_NATIVE_WINDOW";
                break;
            case EGL10.EGL_BAD_MATCH:
                info = "EGL_BAD_MATCH";
                break;
        }
        return info;
    }

    /**
     * エラー内容をログ出力し、SUCCESS以外ならtrueを返す。
     * @param error
     * @return
     */
    public boolean printGlError(int error) {
        if (error != GL10.GL_NO_ERROR) {
            LogUtil.log(toGlErrorInfo(error));
        }
        return error != GL10.GL_NO_ERROR;
    }

    /**
     * エラー内容をログ表示し、SUCCESS以外ならtrueを返す。
     * @return
     */
    public boolean printGlError() {
        return printGlError(gl11.glGetError());
    }

    /**
     * エラー内容をログ出力し、SUCCESS以外ならtrueを返す。
     * @param error
     * @return
     */
    boolean printEglError(int error) {
        if (error != EGL10.EGL_SUCCESS) {
            LogUtil.log(toEglErrorInfo(error));
        }
        return error != EGL10.EGL_SUCCESS;
    }

    /*
    private void printSpec() {
        int[] value = {
            -1
        };
        {
            LogUtil.log("GL_VENDOR : " + gl11.glGetString(GL10.GL_VENDOR));
            LogUtil.log("GL_RENDERER : " + gl11.glGetString(GL10.GL_RENDERER));
            LogUtil.log("GL_VERSION : " + gl11.glGetString(GL10.GL_VERSION));
        }
        //! 性能出力
        {
            gl11.glGetIntegerv(GL11Ext.GL_MAX_PALETTE_MATRICES_OES, value, 0);
            LogUtil.log("GL_MAX_PALETTE_MATRICES_OES : " + value[0]);

            gl11.glGetIntegerv(GL11Ext.GL_MAX_VERTEX_UNITS_OES, value, 0);
            LogUtil.log("GL_MAX_VERTEX_UNITS_OES : " + value[0]);

            gl11.glGetIntegerv(GL10.GL_MAX_MODELVIEW_STACK_DEPTH, value, 0);
            LogUtil.log("GL_MAX_MODELVIEW_STACK_DEPTH : " + value[0]);

            gl11.glGetIntegerv(GL10.GL_MAX_PROJECTION_STACK_DEPTH, value, 0);
            LogUtil.log("GL_MAX_PROJECTION_STACK_DEPTH : " + value[0]);
        }
    }
    */

    /**
     * カメラ関連の行列をリセットし、単位行列化する。
     */
    public void resetCamera() {
        gl11.glMatrixMode(GL10.GL_PROJECTION);
        // gl11.glPopMatrix();
        gl11.glLoadIdentity();
        gl11.glMatrixMode(GL10.GL_MODELVIEW);
    }

    /**
     * ワールド行列を単位行列化する。 このメソッド呼出後、GL_MODELVIEWが設定されている。
     */
    public void resetWorldMatrix() {
        gl11.glMatrixMode(GL10.GL_MODELVIEW);
        gl11.glLoadIdentity();
    }

    /**
     * 描画エリアを補正する。
     * 
     * @param correction
     */
    public void updateDrawArea(VirtualDisplay correction) {
        RectF area = correction.getDrawingArea(new RectF());
        Vector2 realDisplaySize = correction.getRealDisplaySize(new Vector2());
        gl11.glViewport((int) area.left, (int) (realDisplaySize.y - area.bottom), (int) area.width(),
                (int) area.height());
    }

    /**
     * バックバッファの内容を撮影し、バッファに収める
     * @return
     */
    public Bitmap captureSurfaceRGB888(Rect area) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * area.width() * area.height());
        gl11.glReadPixels(area.left, area.top, area.width(), area.height(), GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buffer);
        buffer.position(0);

        byte[] source = new byte[4 * area.width() * area.height()];
        buffer.get(source);
        buffer = null;
        System.gc();

        Bitmap bmp = Bitmap.createBitmap(area.width(), area.height(), Config.ARGB_8888);

        for (int y = 0; y < area.height(); ++y) {
            for (int x = 0; x < area.width(); ++x) {
                int head = (bmp.getWidth() * y + x) * 4;
                bmp.setPixel(x, area.height() - y - 1, ((((int) source[head + 3]) & 0xff) << 24)
                        | ((((int) source[head + 0]) & 0xff) << 16) | ((((int) source[head + 1]) & 0xff) << 8)
                        | (((int) source[head + 2]) & 0xff));
            }
        }
        return bmp;
    }

    /**
     * デバイス座標系のXをU座標に変換する
     * @param x
     * @return
     */
    public static float deviceX2U(float x) {
        return (x + 1.0f) / 2;
    }

    /**
     * 
     * @param u
     * @return
     */
    public static float u2DeviceX(float u) {
        return u * 2 - 1.0f;
    }

    /**
     * デバイス座標系のYをV座標に変換する
     * @param y
     * @return
     */
    public static float deviceY2V(float y) {
        return 1.0f - ((y + 1.0f) / 2);
    }

    /**
     * V座標をデバイス座標系のYに変換する
     * @param v
     * @return
     */
    public static float v2DeviceU(float v) {
        return -(v * 2 - 1.0f);
    }

    /**
     * float値を16bit固定小数へ変換する
     * @param f
     * @return
     */
    public static int float2fixed(float f) {
        return (int) (f * 0x10000);
    }

    /**
     * 16bit固定小数値をfloatへ変換する
     * @param fixed
     * @return
     */
    public static float fixed2float(int fixed) {
        return (float) ((double) fixed / (double) (0x10000));
    }
}
