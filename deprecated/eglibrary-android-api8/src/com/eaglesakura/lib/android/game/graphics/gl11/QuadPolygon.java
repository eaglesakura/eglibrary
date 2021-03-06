package com.eaglesakura.lib.android.game.graphics.gl11;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.VRAM;
import com.eaglesakura.lib.android.game.resource.IRawResource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * 四角形ポリゴンを扱うクラス。 <BR>
 * left,top / right top <BR>
 * ------------------- <BR>
 * |　　　　　　　　　　| <BR>
 * |　　　　　　　　　　| <BR>
 * |　　　　　　　　　　| <BR>
 * |　　　　　　　　　　| <BR>
 * ------------------- <BR>
 * left bottom / right bottom <BR>
 *
 *
 * データは左上、右上、左下、右下の順番に格納される。
 *
 * @author TAKESHI YAMASHITA
 */
public class QuadPolygon extends DisposableGLResource {
    /**
     * オブジェクト
     */
    int vbo = VRAM.NULL;

    public QuadPolygon(VRAM vram) {
        this(vram, -0.5f, 0.5f, 0.5f, -0.5f);
    }

    public QuadPolygon(VRAM vram, float left, float top, float right, float bottom) {
        super(vram);

        vbo = vram.genVertexBufferObject();
        GL11 gl = getGL();

        //! 頂点を１VBOにまとめる。
        float[] vertices = {
                // 位置情報
                left, top, //!< 左上
                right, top, //!< 右上
                left, bottom, //!< 左下
                right, bottom, //!< 右下

                //! UV情報
                0, 0, //!< 左上
                1, 0, //!< 右上
                0, 1, //!< 左下
                1, 1, //!< 右下
        };

        //! VBOに詰め込む。
        {
            ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer fb = bb.asFloatBuffer();
            fb.put(vertices);
            fb.position(0);

            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo);
            gl.glBufferData(GL11.GL_ARRAY_BUFFER, bb.capacity(), fb, GL11.GL_STATIC_DRAW);
            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
        }

        register();
    }

    @Override
    public List<IRawResource> getRawResources() {
        List<IRawResource> result = new ArrayList<IRawResource>();
        if (vbo != VRAM.NULL) {
            result.add(new GLResource(getGL(), Type.VertexBufferObject, vbo));
        }
        return result;
    }

    @Override
    public void onDispose() {
        if (vbo != VRAM.NULL) {
            vram.deleteVertexBufferObject(vbo);
            vbo = VRAM.NULL;
        }
    }

    /**
     * 頂点情報をGLに関連付ける。
     */
    public void bind() {
        GL11 gl = getGL();
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo);

        //! Posバインド
        {
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);

        }

        //! UVバインド
        {
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            //! 位置が 2要素 * 4頂点 * 4byte
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, (2 * 4) * 4);
        }

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
    }

    /**
     * 現在の行列 / 色状態で描画する。
     */
    public void draw() {
        GL11 gl = getGL();
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    /**
     * GLとの関連付けを解除する。
     */
    public void unbind() {
        GL11 gl = getGL();
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
    }
}
