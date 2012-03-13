package com.eaglesakura.lib.android.game.graphics.gl11;

import javax.microedition.khronos.opengles.GL10;

public class RawTextureImage extends TextureImageBase {

    public RawTextureImage(OpenGLManager glManager, final int texWidth, final int texHeight) {
        super(glManager);
        width = texWidth;
        height = texHeight;
        textureId = glManager.genTexture();
        syncGC();
        setTextureLinearFilter(false);
    }

    /**
     * MipMapの適当な場所へテクスチャを流し込む。
     * @param mipLevel
     * @param rgba8888Array
     * @param mipWidth
     * @param mipHeight
     */
    public void loadMipmapRGBA8888(final int mipLevel, final int[] rgba8888Array, final int mipWidth,
            final int mipHeight) {
        // テクスチャ転送
        bind();
        {
            glManager.getGL().glTexImage2D(GL10.GL_TEXTURE_2D, mipLevel, GL10.GL_RGBA, mipWidth, mipHeight, 0,
                    GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, OpenGLManager.wrapColor(rgba8888Array));
        }
        unbind();
    }

    /**
     * MipMapの適当な場所へテクスチャを流し込む。
     * @param mipLevel
     * @param rgbaArray
     * @param mipWidth
     * @param mipHeight
     */
    public void loadMipmapRGB888(final int mipLevel, final byte[] rgbArray, final int mipWidth, final int mipHeight) {
        // テクスチャ転送
        bind();
        {
            glManager.getGL().glTexImage2D(GL10.GL_TEXTURE_2D, mipLevel, GL10.GL_RGB, mipWidth, mipHeight, 0,
                    GL10.GL_RGB, GL10.GL_UNSIGNED_BYTE, OpenGLManager.wrap(rgbArray));
        }
        unbind();
    }
}
