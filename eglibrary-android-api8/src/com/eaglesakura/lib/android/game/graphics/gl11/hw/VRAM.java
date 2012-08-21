package com.eaglesakura.lib.android.game.graphics.gl11.hw;

import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.resource.GarbageCollector;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * EGLContextに関連付けられたVRAM領域を取得する
 * VRAMはgc機構を備えており、適当なタイミングでgc()を行うことができる。
 * ただし、GLのワーキングスレッドに属している必要がある。
 */
public class VRAM extends DisposableResource {
    EGLManager egl;
    GL11 gl11;
    GL11ExtensionPack gl11EP;

    /**
     * GC対象管理クラス
     * その場で開放させるため、ハンドラは指定しない。
     */
    private GarbageCollector garbageCollector = new GarbageCollector(null);

    VRAM(EGLManager egl) {
        this.egl = egl;
        this.gl11 = (GL11) egl.getGL();
        this.gl11EP = (GL11ExtensionPack) gl11;
    }

    /**
     * 無効なオブジェクト
     */
    public static int NULL = 0;

    /**
     * フレームバッファを生成する。
     * @return
     */
    public int genFrameBufferObject() {
        int[] buffer = new int[1];
        gl11EP.glGenFramebuffersOES(1, buffer, 0);
        if (buffer[0] == NULL) {
            throw new IllegalStateException("buffer not create");
        }
        return buffer[0];
    }

    /**
     * フレームバッファを削除する。
     * 別スレッドから投げられた場合、GLスレッドにpostされる。
     * @param buffer
     */
    public void deleteFrameBufferObject(final int buffer) {
        gl11.glGetError();
        gl11EP.glDeleteFramebuffersOES(1, new int[] {
            buffer
        }, 0);
    }

    /**
     * レンダリング用バッファを生成する。
     * @return
     */
    public int genRenderBuffer() {
        int[] buffer = new int[1];
        gl11EP.glGenRenderbuffersOES(1, buffer, 0);
        if (buffer[0] == 0) {
            throw new IllegalStateException("buffer not create");
        }
        return buffer[0];
    }

    /**
     * レンダリングバッファを削除する
     * 別スレッドから投げられた場合、GLスレッドにpostされる。
     * @param buffer
     */
    public void deleteRenderBuffer(final int buffer) {
        gl11.glGetError();
        gl11EP.glDeleteRenderbuffersOES(1, new int[] {
            buffer
        }, 0);
    }

    /**
     * VBOのバッファをひとつ作成する。
     * 
     * @return
     */
    public int genVertexBufferObject() {
        int[] buf = new int[1];
        gl11.glGetError();
        gl11.glGenBuffers(1, buf, 0);
        if (gl11.glGetError() == GL11.GL_OUT_OF_MEMORY) {
            throw new OutOfMemoryError("glGenTexture Error");
        }

        return buf[0];
    }

    /**
     * VBOのバッファをひとつ削除する。
     * 別スレッドから投げられた場合、GLスレッドにpostされる。
     * 
     * @param vbo
     */
    public void deleteVertexBufferObject(final int vbo) {
        gl11.glGetError();
        gl11.glDeleteBuffers(1, new int[] {
            vbo
        }, 0);
    }

    /**
     * テクスチャバッファをひとつ作成する。
     * 
     * @return
     */
    public int genTexture() {
        gl11.glGetError();
        int[] buf = new int[1];
        gl11.glGetError();
        gl11.glGenTextures(1, buf, 0);
        if (gl11.glGetError() == GL11.GL_OUT_OF_MEMORY) {
            throw new OutOfMemoryError("glGenTexture Error");
        }

        return buf[0];
    }

    /**
     * GC管理クラスを取得する。
     * @return
     */
    public GarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    /**
     * テクスチャバッファを削除する。
     * 
     * @param tex
     */
    public void deleteTexture(final int tex) {
        gl11.glGetError();
        gl11.glDeleteTextures(1, new int[] {
            tex
        }, 0);
    }

    /**
     * GLオブジェクトを取得する
     * @return
     */
    public GL11 getGL() {
        return gl11;
    }

    /**
     * 解放対象のメモリを全て解放する。
     */
    public int gc() {
        int gcItems = garbageCollector.gc();
        LogUtil.log(String.format("gc OpenGL GC Resources :: %d", gcItems));
        LogUtil.log(String.format("Markers :: %d", garbageCollector.getGcTargetCount()));
        return gcItems;
    }

    @Override
    public void dispose() {
        // 強制的な解放を行わせる
        int gcItems = garbageCollector.delete();
        LogUtil.log(String.format("Delete OpenGL GC Resources :: %d", gcItems));
        LogUtil.log(String.format("Markers :: %d", garbageCollector.getGcTargetCount()));
    }

    /**
     * エラーを持っていた場合、GLのエラーを出力する。
     * @return
     */
    public boolean printGLError() {
        return egl.printGlError();
    }

    /**
     * EGLのエラーを表示する
     * @return
     */
    public boolean printEGLError() {
        return egl.printEglError();
    }
}
