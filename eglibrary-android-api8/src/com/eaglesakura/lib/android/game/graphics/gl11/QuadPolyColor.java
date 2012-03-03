package com.eaglesakura.lib.android.game.graphics.gl11;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * スプライト描画時の頂点カラーを補完する
 * @author Takeshi
 *
 */
public class QuadPolyColor {
    OpenGLManager glManager;
    GL11 gl;

    final byte[] colorFilters = {
            //! r, g, b, a
            (byte) 255, (byte) 255, (byte) 255, (byte) 255, //!< 左上
            (byte) 255, (byte) 255, (byte) 255, (byte) 255, //!< 右上
            (byte) 255, (byte) 255, (byte) 255, (byte) 255, //!< 左下
            (byte) 255, (byte) 255, (byte) 255, (byte) 255, //!< 右下
    };

    ByteBuffer buffer = ByteBuffer.allocateDirect(colorFilters.length);

    public QuadPolyColor(OpenGLManager gl) {
        this.glManager = gl;
        this.gl = gl.getGL();
        setColorRGBA(0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff);
    }

    public void setColorRGBA(int leftTop, int rightTop, int leftBottom, int rightBottom) {
        int index = 0;

        {
            final int color = leftTop;
            colorFilters[index++] = (byte) ((color >> 24) & 0xff);
            colorFilters[index++] = (byte) ((color >> 16) & 0xff);
            colorFilters[index++] = (byte) ((color >> 8) & 0xff);
            colorFilters[index++] = (byte) ((color >> 0) & 0xff);
        }
        {
            final int color = rightTop;
            colorFilters[index++] = (byte) ((color >> 24) & 0xff);
            colorFilters[index++] = (byte) ((color >> 16) & 0xff);
            colorFilters[index++] = (byte) ((color >> 8) & 0xff);
            colorFilters[index++] = (byte) ((color >> 0) & 0xff);
        }
        {
            final int color = leftBottom;
            colorFilters[index++] = (byte) ((color >> 24) & 0xff);
            colorFilters[index++] = (byte) ((color >> 16) & 0xff);
            colorFilters[index++] = (byte) ((color >> 8) & 0xff);
            colorFilters[index++] = (byte) ((color >> 0) & 0xff);
        }
        {
            final int color = rightBottom;
            colorFilters[index++] = (byte) ((color >> 24) & 0xff);
            colorFilters[index++] = (byte) ((color >> 16) & 0xff);
            colorFilters[index++] = (byte) ((color >> 8) & 0xff);
            colorFilters[index++] = (byte) ((color >> 0) & 0xff);
        }

        buffer.put(colorFilters);
        buffer.position(0);
    }

    /**
     * カラーバインドを行う。
     */
    public void bind() {
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, buffer);
    }

    /**
     * カラーバインドを外す
     */
    public void unbind() {
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
}
