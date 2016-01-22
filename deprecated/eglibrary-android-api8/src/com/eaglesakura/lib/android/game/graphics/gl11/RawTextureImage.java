package com.eaglesakura.lib.android.game.graphics.gl11;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.VRAM;

import javax.microedition.khronos.opengles.GL10;

/**
 * RGBAの適当な色配列からテクスチャを作成する。
 *
 * @author TAKESHI YAMASHITA
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
     */
    public void loadMipmapRGBA8888(final int mipLevel, final int[] rgba8888Array, final int mipWidth,
                                   final int mipHeight) {
        // テクスチャ転送
        bind();
        {
            getGL().glTexImage2D(GL10.GL_TEXTURE_2D, mipLevel, GL10.GL_RGBA, mipWidth, mipHeight, 0, GL10.GL_RGBA,
                    GL10.GL_UNSIGNED_BYTE, VRAM.wrapColor(rgba8888Array));
        }
        unbind();
    }

    /**
     * MipMapの適当な場所へテクスチャを流し込む。
     */
    public void loadMipmapRGB888(final int mipLevel, final byte[] rgbArray, final int mipWidth, final int mipHeight) {
        // テクスチャ転送
        bind();
        {
            getGL().glTexImage2D(GL10.GL_TEXTURE_2D, mipLevel, GL10.GL_RGB, mipWidth, mipHeight, 0, GL10.GL_RGB,
                    GL10.GL_UNSIGNED_BYTE, VRAM.wrap(rgbArray));
        }
        unbind();
    }
}
