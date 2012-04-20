package com.eaglesakura.lib.android.game.graphics.gl11;

import java.nio.Buffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.gl11.DisposableGLResource.GLResource;
import com.eaglesakura.lib.android.game.graphics.gl11.DisposableGLResource.Type;
import com.eaglesakura.lib.android.game.resource.IRawResource;

/**
 * レンダリングターゲット用のテクスチャ。
 * 作成されたテクスチャは必ず上下が逆さになることに注意。
 * @author TAKESHI YAMASHITA
 *
 */
public class RenderTargetTexture extends TextureImageBase {

    /**
     * 描画対象の仮想ディスプレイ
     */
    VirtualDisplay display = new VirtualDisplay();

    /**
     * 描画対象のカラーバッファ。
     * 通常、テクスチャとバインドされている。
     */
    int colorBuffer = GL_NULL;

    /**
     * 描画対象の深度バッファ。
     * テクスチャにはバインドされていない。
     */
    int depthBuffer = GL_NULL;

    /**
     * 描画対象のフレームバッファ
     */
    int frameBuffer = GL_NULL;

    /**
     * テクスチャレンダリング時のUVオフセット値。
     * 上下反転の補正用
     */
    float yUvOffset = 0;

    GL11 gl11 = null;
    GL11ExtensionPack gl11EP = null;

    public RenderTargetTexture(OpenGLManager glManager, int targetWidth, int targetHeight) {
        super(glManager);
        gl11 = glManager.getGL();
        gl11EP = (GL11ExtensionPack) gl11;
        display.setRealDisplaySize(targetWidth, targetHeight);
        display.setVirtualDisplaySize(targetWidth, targetHeight);
        height = targetHeight;
        width = targetWidth;

        initRenderFrame();
    }

    /**
     * 
     */
    protected void initRenderFrame() {
        GL10 gl10 = glManager.getGL();
        GL11ExtensionPack gl = (GL11ExtensionPack) gl10;

        textureId = glManager.genTexture();
        frameBuffer = glManager.genFrameBufferObject();
        colorBuffer = glManager.genRenderBuffer();
        depthBuffer = glManager.genRenderBuffer();

        final int renderWidth = toGLTextureSize(width);
        final int renderHeight = toGLTextureSize(height);
        textureScale.x = (float) width / (float) renderWidth;
        textureScale.y = (float) height / (float) renderHeight;
        {
            final int yOver = renderHeight - height;
            yUvOffset = (float) yOver / (float) renderHeight;
        }

        //! レンダリング対象のテクスチャを生成する
        {
            bind();
            {
                gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
                gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
                gl10.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, renderWidth, renderHeight, 0, GL10.GL_RGBA,
                        GL10.GL_UNSIGNED_BYTE, null);
            }
            unbind();
        }

        //! フレームバッファ関連付け
        gl.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, frameBuffer);
        //! 深度バッファの準備
        {
            gl.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, depthBuffer);
            gl.glRenderbufferStorageOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, GL11ExtensionPack.GL_DEPTH_COMPONENT16,
                    renderWidth, renderHeight);
            gl.glFramebufferRenderbufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, GL11ExtensionPack.GL_DEPTH_COMPONENT,
                    GL11ExtensionPack.GL_RENDERBUFFER_OES, depthBuffer);
            gl.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, 0);
        }
        //! カラーバッファの準備
        {
            gl.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, colorBuffer);
            gl.glRenderbufferStorageOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, GL11ExtensionPack.GL_RGBA8, renderWidth,
                    renderHeight);
            gl.glFramebufferRenderbufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                    GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES, GL11ExtensionPack.GL_RENDERBUFFER_OES, colorBuffer);
            gl.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, 0);
        }
        //! カラーバッファとテクスチャの関連付け
        {
            gl.glFramebufferTexture2DOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                    GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES, GL10.GL_TEXTURE_2D, textureId, 0);
        }
        {
            int status = gl.glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES);
            if (status != GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {
                throw new RuntimeException("Framebuffer is not complete: " + status);
            }
        }

        gl11.glClearColor(0, 0, 0, 0);
        gl11.glClear(GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_COLOR_BUFFER_BIT);
        //! フレームバッファ関連付け解除
        gl.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);

        register();
    }

    @Override
    public void bindTextureCoord(int x, int y, int w, int h) {
        GL11 gl = glManager.getGL();
        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();
        float sizeX = (float) w / (float) getWidth();
        float sizeY = (float) h / (float) getHeight();
        float sx = (float) x / (float) getWidth();
        float sy = (float) y / (float) getHeight();

        // 上下反転に対応
        {
            gl.glScalef(1, -1, 0);
            gl.glTranslatef(0, yUvOffset, 0);
        }
        gl.glScalef(getTextureScaleX(), getTextureScaleY(), 1);
        gl.glTranslatef(sx, sy, 0.0f);
        gl.glScalef(sizeX, sizeY, 1.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    /**
     * テクスチャへのレンダリングを開始する。
     */
    public void bindRenderTarget() {
        //! フレームバッファ関連付け
        gl11.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        glManager.printGlError();
        gl11EP.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, frameBuffer);
        glManager.printGlError();
        gl11.glViewport(0, 0, display.getVirtualDisplayWidth(), display.getVirtualDisplayHeight());
    }

    /**
     * 投影用のディスプレイを取得する。
     * @return
     */
    public VirtualDisplay getDisplay() {
        return display;
    }

    /**
     * テクスチャのレンダリングを終了する。
     */
    public void unbindRenderTarget(VirtualDisplay originDisplay) {
        gl11EP.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
        glManager.printGlError();
        glManager.updateDrawArea(originDisplay);
        glManager.printGlError();
    }

    /**
     * 利用しているリソースの一覧を追加する。
     */
    @Override
    public List<IRawResource> getRawResources() {
        List<IRawResource> result = super.getRawResources();
        if (frameBuffer != GL_NULL) {
            result.add(new GLResource(getGL(), Type.FrameBuffer, frameBuffer));
        }
        if (colorBuffer != GL_NULL) {
            result.add(new GLResource(getGL(), Type.RenderBuffer, colorBuffer));
        }
        if (depthBuffer != GL_NULL) {
            result.add(new GLResource(getGL(), Type.RenderBuffer, depthBuffer));
        }
        return result;
    }

    /**
     * 持っているリソースを廃棄する。
     */
    @Override
    public void onDispose() {
        if (frameBuffer != GL_NULL) {
            glManager.deleteFrameBufferObject(frameBuffer);
            frameBuffer = GL_NULL;
        }

        if (colorBuffer != GL_NULL) {
            glManager.deleteRenderBuffer(colorBuffer);
            colorBuffer = GL_NULL;
        }
        if (depthBuffer != GL_NULL) {
            glManager.deleteRenderBuffer(depthBuffer);
            depthBuffer = GL_NULL;
        }
        super.onDispose();
    }

    protected Buffer fullScreenRenderVertices = null;

    public void drawFullScreen() {
        if (fullScreenRenderVertices == null) {
            //! 頂点を１VBOにまとめる。
            float[] vertices = {
                    // 位置情報
                    -1, 1, //!< 左上
                    1, 1, //!< 右上
                    -1, -1, //!< 左下
                    1, -1, //!< 右下

                    //! UV情報
                    0, getTextureScaleY(), //!< 左上
                    getTextureScaleX(), getTextureScaleY(), //!< 右上
                    0, 0, //!< 左下
                    getTextureScaleX(), 0, //!< 右下
            };
            fullScreenRenderVertices = OpenGLManager.wrap(vertices);
        }

        GL11 gl = glManager.getGL();
        fullScreenRenderVertices.position(0);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glLoadIdentity();
        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, fullScreenRenderVertices);
        fullScreenRenderVertices.position(2 * 4);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, fullScreenRenderVertices);

        bind();
        {
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        }
        unbind();
    }
}
