package com.eaglesakura.lib.android.game.graphics.gl11;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.VRAM;
import com.eaglesakura.lib.android.game.resource.GCResourceBase;
import com.eaglesakura.lib.android.game.resource.IRawResource;

import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

/**
 * 廃棄が必要なOpenGL ESの資源を管理。
 *
 * @author TAKESHI YAMASHITA
 */
public abstract class DisposableGLResource extends GCResourceBase {
    /**
     * VRAMのメモリ管理クラス
     */
    protected VRAM vram = null;

    /**
     * リソースの種類。
     *
     * @author TAKESHI YAMASHITA
     */
    public enum Type {
        /**
         * テクスチャリソース
         */
        Texture {
            @Override
            public void delete(GL11 gl, int id) {
                gl.glDeleteTextures(1, new int[]{
                        id
                }, 0);
            }
        },

        /**
         * VBOの頂点/indexバッファ
         */
        VertexBufferObject {
            @Override
            public void delete(GL11 gl, int id) {
                gl.glDeleteBuffers(1, new int[]{
                        id
                }, 0);
            }
        },

        /**
         * フレームバッファ
         */
        FrameBuffer {
            @Override
            public void delete(GL11 gl, int id) {
                GL11ExtensionPack gl11 = (GL11ExtensionPack) gl;
                gl11.glDeleteFramebuffersOES(1, new int[]{
                        id
                }, 0);
            }
        },

        /**
         * レンダリングバッファ
         */
        RenderBuffer {
            @Override
            public void delete(GL11 gl, int id) {
                GL11ExtensionPack gl11 = (GL11ExtensionPack) gl;
                gl11.glDeleteRenderbuffersOES(1, new int[]{
                        id
                }, 0);
            }
        };

        /**
         * リソースの削除を行わせる。
         */
        public abstract void delete(GL11 gl, int id);
    }

    /**
     * GLの資源を示すクラス。
     *
     * @author TAKESHI YAMASHITA
     */
    public static class GLResource implements IRawResource {
        private Type type;
        private int id;
        private GL11 gl;

        public GLResource(GL11 gl, Type type, int id) {
            this.type = type;
            this.id = id;
            this.gl = gl;
        }

        @Override
        public void dispose() {
            type.delete(gl, id);
        }
    }

    public DisposableGLResource(VRAM vram) {
        super(vram.getGarbageCollector());
        this.vram = vram;
    }

    public final GL11 getGL() {
        return vram.getGL();
    }
}
