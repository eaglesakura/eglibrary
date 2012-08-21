package com.eaglesakura.lib.android.game.graphics.gl11;

import javax.microedition.khronos.opengles.GL10;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.VRAM;

/**
 * RGBAの適当な色配列からテクスチャを作成する。
 * @author TAKESHI YAMASHITA
 *
 */
public class RawTextureImage extends TextureImageBase {

    public RawTextureImage(VRAM vram, final int texWidth, final int texHeight) {
        super(vram);
        width = texWidth;
        height = texHeight;
        textureId = vram.genTexture();
        register();
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
            getGL().glTexImage2D(GL10.GL_TEXTURE_2D, mipLevel, GL10.GL_RGBA, mipWidth, mipHeight, 0, GL10.GL_RGBA,
                    GL10.GL_UNSIGNED_BYTE, OpenGLManager.wrapColor(rgba8888Array));
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
            getGL().glTexImage2D(GL10.GL_TEXTURE_2D, mipLevel, GL10.GL_RGB, mipWidth, mipHeight, 0, GL10.GL_RGB,
                    GL10.GL_UNSIGNED_BYTE, OpenGLManager.wrap(rgbArray));
        }
        unbind();
    }
}
