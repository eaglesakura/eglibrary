package com.eaglesakura.lib.android.game.graphics.gl11;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;

/**
 * レンダリングターゲット用のテクスチャ。
 * 作成されたテクスチャは必ず上下が逆さになることに注意。
 * @author Takeshi
 *
 */
public class RenderTargetTexture extends TextureImageBase {

    /**
     * 描画対象の仮想ディスプレイ
     */
    VirtualDisplay display;

    /**
     * 描画対象のカラーバッファ。
     * 通常、テクスチャとバインドされている。
     */
    int colorBuffer = NULL;

    /**
     * 描画対象の深度バッファ。
     * テクスチャにはバインドされていない。
     */
    int depthBuffer = NULL;

    /**
     * 描画対象のフレームバッファ
     */
    int frameBuffer = NULL;

    GL11 gl11 = null;
    GL11ExtensionPack gl11EP = null;

    public RenderTargetTexture(OpenGLManager glManager, int targetWidth, int targetHeight) {
        super(glManager);
        gl11 = glManager.getGL();
        gl11EP = (GL11ExtensionPack) gl11;
        display = new VirtualDisplay();
        display.setRealDisplaySize(targetWidth, targetHeight);
        display.setVirtualDisplaySize(targetWidth, targetHeight);
        height = targetHeight;
        width = targetWidth;
    }

    /**
     * 
     */
    void initRenderFrame() {
        GL10 gl10 = glManager.getGL();
        GL11ExtensionPack gl = (GL11ExtensionPack) gl10;

        textureId = glManager.genTexture();
        frameBuffer = glManager.genFrameBufferObject();
        colorBuffer = glManager.genRenderBuffer();
        depthBuffer = glManager.genRenderBuffer();

        final int renderWidth = toGLTextureSize(width);
        final int renderHeight = toGLTextureSize(height);

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
        }
        //! カラーバッファの準備
        {
            gl.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, colorBuffer);
            gl.glRenderbufferStorageOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, GL11ExtensionPack.GL_RGBA8, renderWidth,
                    renderHeight);
            gl.glFramebufferRenderbufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                    GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES, GL11ExtensionPack.GL_RENDERBUFFER_OES, colorBuffer);
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

        //! フレームバッファ関連付け解除
        gl.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, frameBuffer);
    }

    /**
     * テクスチャへのレンダリングを開始する。
     */
    public void bindRenderTarget() {
        //! フレームバッファ関連付け
        gl11EP.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, frameBuffer);
        gl11.glViewport(0, 0, display.getVirtualDisplayWidth(), display.getVirtualDisplayHeight());
    }

    /**
     * テクスチャのレンダリングを終了する。
     */
    public void unbindRenderTarget(VirtualDisplay originDisplay) {
        gl11EP.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
        glManager.updateDrawArea(originDisplay);
    }

    @Override
    public void dispose() {
        super.dispose();

        if (frameBuffer != NULL) {
            glManager.deleteFrameBufferObject(frameBuffer);
        }

        if (colorBuffer != NULL) {
            glManager.deleteRenderBuffer(colorBuffer);
        }
        if (depthBuffer != NULL) {
            glManager.deleteRenderBuffer(depthBuffer);
        }
    }
}
