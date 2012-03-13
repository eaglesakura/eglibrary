package com.eaglesakura.lib.android.game.graphics.gl11;

import java.util.List;

import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import com.eaglesakura.lib.android.game.graphics.DisposableResource;

/**
 * 廃棄が必要なOpenGL ESの資源を管理。
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class DisposableGLResource extends DisposableResource {

    /**
     * OpenGL資源の無効オブジェクト／NULLを示す。
     */
    public static final int GL_NULL = 0;

    /**
     * リソースの種類。
     * @author TAKESHI YAMASHITA
     *
     */
    public enum Type {
        /**
         * テクスチャリソース
         */
        Texture {
            @Override
            public void delete(GL11 gl, int id) {
                gl.glDeleteTextures(1, new int[] {
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
                gl.glDeleteBuffers(1, new int[] {
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
                gl11.glDeleteFramebuffersOES(1, new int[] {
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
                gl11.glDeleteRenderbuffersOES(1, new int[] {
                    id
                }, 0);
            }
        };

        /**
         * リソースの削除を行わせる。
         * @param gl
         * @param id
         */
        public abstract void delete(GL11 gl, int id);
    }

    /**
     * GLの資源を示すクラス。
     * @author TAKESHI YAMASHITA
     *
     */
    public static class GLResource {
        public Type type;
        public int id;

        public GLResource(Type type, int id) {
            this.type = type;
            this.id = id;
        }

        public void delete(GL11 gl) {
            type.delete(gl, id);
        }
    }

    /**
     * GC制御クラス
     */
    private GLGarbageCollector garbageCollector;

    public DisposableGLResource(GLGarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    /**
     * finalizeは確実性が低いため、オーバーライドを許さない。
     */
    @Override
    protected final void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * GC対象のリソースを同期する。
     */
    protected void syncGC() {
        garbageCollector.add(this);
    }

    /**
     * 管理する資源を取得する。
     * @return
     */
    public abstract List<GLResource> getRawResources();

    /**
     * 廃棄されるときに呼び出される。
     * finalizeを含め、2回呼ばれることがある。
     */
    public abstract void onDispose();

    /**
     * 明示的にdisposeしたときは自動的にコレクタから排除する。
     */
    @Override
    public final void dispose() {
        onDispose();
        garbageCollector.remove(this);
    }
}
