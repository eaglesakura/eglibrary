package com.eaglesakura.lib.android.game.graphics.gl11.util;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;

import com.eaglesakura.lib.android.game.graphics.Color;
import com.eaglesakura.lib.android.game.graphics.Sprite;
import com.eaglesakura.lib.android.game.graphics.canvas.BitmapImage;
import com.eaglesakura.lib.android.game.graphics.gl11.RawTextureImage;
import com.eaglesakura.lib.android.game.graphics.gl11.SpriteManager;
import com.eaglesakura.lib.android.game.graphics.gl11.TextureImageBase;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.VRAM;
import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * 巨大な一枚絵を表示する。
 * @author TAKESHI YAMASHITA
 *
 */
public class Background extends DisposableResource {
    /**
     * 画像読み込みタイプを指定する。
     * @author TAKESHI YAMASHITA
     *
     */
    public enum ImageType {
        /**
         * RGB888の不透明テクスチャ
         */
        RGB888,

        /**
         * RGBA8888の透過テクスチャ
         */
        RGBA8888,
    }

    /**
     * 分割したテクスチャ
     */
    TextureImageBase[][] textures = null;

    /**
     * テクスチャの分割サイズ
     */
    int textureWidth = 512;

    /**
     * テクスチャの分割サイズ高さ
     */
    int textureHeight = 512;

    /**
     * 画像本来の幅
     */
    int width = 0;

    /**
     * 画像本来の高さ
     */
    int height = 0;

    Color color = new Color();

    public Background() {

    }

    /**
     * テクスチャの分割サイズ
     * @param textureHeight
     */
    public Background setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
        return this;
    }

    /**
     * テクスチャの分割サイズ
     * @param textureWidth
     */
    public Background setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * 画像を分割して読み込む。
     * @param bitmap
     * @param originSize
     * @param type
     */
    public void load(final BitmapImage image, final ImageType type, VRAM vram, ImageLoadingListener listener) {
        if (listener == null) {
            listener = new ImageLoadingListener() {
                @Override
                public void onTextureLoadingComplete(Background image) {
                }

                @Override
                public boolean onSubTextureLoadingStart(Background image) {
                    return true;
                }

                @Override
                public void onSubTextureLoadingComplete(Background image, float completed) {
                }
            };
        }

        width = image.getWidth();
        height = image.getHeight();

        final int xTextureNum = (width + textureWidth - 1) / textureWidth;
        final int yTextureNum = (height + textureHeight - 1) / textureHeight;

        LogUtil.log("x-texture :: " + xTextureNum + " :: y-texture :: " + yTextureNum);
        textures = new TextureImageBase[yTextureNum][];
        for (int y = 0, i = 0; y < height; y += textureHeight, ++i) {
            textures[i] = new TextureImageBase[xTextureNum];
            for (int x = 0, k = 0; x < width; x += textureWidth, ++k) {
                if (listener.onSubTextureLoadingStart(this)) {

                    final int imageX = x;
                    final int imageY = y;
                    TextureImageBase subTexture = null;

                    switch (type) {
                        case RGBA8888:
                            subTexture = createSubTextureRGBA8888(image.getBitmap(), vram, imageX, imageY,
                                    textureWidth, textureHeight);
                            break;
                        case RGB888:
                            subTexture = createSubTextureRGB888(image.getBitmap(), vram, imageX, imageY, textureWidth,
                                    textureHeight);
                            break;
                    }

                    if (textures != null && textures[i] != null) {
                        textures[i][k] = subTexture;
                        final float maxTextures = yTextureNum * xTextureNum;
                        listener.onSubTextureLoadingComplete(this, (float) (i * xTextureNum + k + 1) / maxTextures);
                    } else {
                        subTexture.dispose();
                    }
                } else {
                    return;
                }
            }
        }

        listener.onTextureLoadingComplete(this);

    }

    /**
     * サブテクスチャを作成する。
     * テクスチャはRGBA8888が利用される。
     * @param origin
     * @param glManager
     * @param x
     * @param y
     * @return
     */
    public static TextureImageBase createSubTextureRGBA8888(Bitmap origin, VRAM vram, int x, int y, int textureWidth,
            int textureHeight) {
        int[] pixels = new int[textureWidth * textureHeight];
        final int pixWidth = Math.min(textureWidth, origin.getWidth() - x);
        final int pixHeight = Math.min(textureHeight, origin.getHeight() - y);
        origin.getPixels(pixels, 0, textureWidth, x, y, pixWidth, pixHeight);

        final int num = pixels.length;

        for (int i = 0; i < num; ++i) {
            final int pixel = pixels[i];
            pixels[i] = (pixel << 8) | ((pixel >> 24) & 0xff);
        }

        RawTextureImage texture = new RawTextureImage(vram, textureWidth, textureHeight);
        texture.loadMipmapRGBA8888(0, pixels, textureWidth, textureHeight);

        texture.bind();
        {
            GL11 gl = vram.getGL();
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        }
        texture.unbind();
        return texture;
    }

    /**
     * サブテクスチャを作成する。
     * テクスチャはRGB888が利用される。
     * @param origin
     * @param glManager
     * @param x
     * @param y
     * @return
     */
    public static TextureImageBase createSubTextureRGB888(Bitmap origin, VRAM vram, int x, int y, int textureWidth,
            int textureHeight) {
        int[] pixels = new int[textureWidth * textureHeight];
        final int pixWidth = Math.min(textureWidth, origin.getWidth() - x);
        final int pixHeight = Math.min(textureHeight, origin.getHeight() - y);
        origin.getPixels(pixels, 0, textureWidth, x, y, pixWidth, pixHeight);

        final int num = pixels.length;

        byte[] rgb = new byte[pixels.length * 3];
        int index = 0;
        for (int i = 0; i < num; ++i) {
            final int pixel = pixels[i];
            rgb[index++] = (byte) (pixel >> 16);
            rgb[index++] = (byte) (pixel >> 8);
            rgb[index++] = (byte) (pixel);
        }

        RawTextureImage texture = new RawTextureImage(vram, textureWidth, textureHeight);
        texture.loadMipmapRGB888(0, rgb, textureWidth, textureHeight);

        texture.bind();
        {
            GL11 gl = vram.getGL();
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        }
        texture.unbind();
        return texture;
    }

    /**
     * 色情報を取得する
     * @return
     */
    public Color getColor() {
        return color;
    }

    /**
     * 仮で作成したスプライトの位置へ描画する
     * @param spriteManager
     * @param sprite
     */
    public void draw(SpriteManager spriteManager, Sprite sprite) {
        color.set(sprite.getColorRGBA());
        draw(spriteManager, sprite.getDstLeft(), sprite.getDstTop(), sprite.getDstWidth(), sprite.getDstHeight());
    }

    /**
     * 指定領域に描画を行う。
     * @param spriteManager
     * @param dstX
     * @param dstY
     * @param dstWidth
     * @param dstHeight
     */
    public void draw(SpriteManager spriteManager, int dstX, int dstY, int dstWidth, int dstHeight) {
        if (textures == null) {
            return;
        }

        synchronized (textures) {
            final float xScale = (float) dstWidth / (float) width;
            final float yScale = (float) dstHeight / (float) height;

            // 実際に描画する1枚のピクセル数X
            final int xBlock = (int) (xScale * textureWidth);
            // 実際に描画する1枚ごとのピクセル数Y
            final int yBlock = (int) (yScale * textureHeight);

            // 最後のX方向末尾ではみ出したピクセル数
            final int xOverPixel = (width % textureWidth);

            // 最後のY方向末尾1行ではみ出したピクセル数
            final int yOverPixel = (height % textureHeight);

            final int rgba = color.getRGBA();
            for (int y = 0, i = 0; i < textures.length; y += yBlock, ++i) {
                if (textures[i] == null) {
                    return;
                }

                for (int x = 0, k = 0; k < textures[0].length; x += xBlock, ++k) {
                    TextureImageBase subImage = textures[i][k];

                    if (subImage != null) {
                        //! 最後のピクセルだけはちゃんと計算する
                        int subDstWidth = Math.min(xBlock, dstWidth - x);
                        int subDstHeight = Math.min(yBlock, dstHeight - y);

                        final int subSrcWidth = subDstWidth != xBlock ? xOverPixel : textureWidth;
                        final int subSrcHeight = subDstHeight != yBlock ? yOverPixel : textureHeight;

                        // 最後の描画でピクセル誤差を吸収する
                        if (i == (textures.length - 1)) {
                            subDstHeight = dstHeight - y;
                        }
                        if (k == (textures[0].length - 1)) {
                            subDstWidth = dstWidth - x;
                        }

                        spriteManager.drawImage(subImage, 0, 0, subSrcWidth, subSrcHeight, dstX + x, dstY + y,
                                subDstWidth, subDstHeight, 0, rgba);
                    }
                }
            }

        }
    }

    @Override
    public void dispose() {
        if (textures != null) {
            synchronized (textures) {
                for (TextureImageBase[] texArray : textures) {
                    if (texArray != null) {
                        for (int i = 0; i < texArray.length; ++i) {
                            TextureImageBase tex = texArray[i];
                            if (tex != null) {
                                tex.dispose();
                                texArray[i] = null;
                            }
                        }
                    }
                }
                textures = null;
            }
        }
    }

    /**
     * 画像読み込み時のリスナ。
     * @author TAKESHI YAMASHITA
     *
     */
    public interface ImageLoadingListener {
        /**
         * テクスチャへの転送を開始する。
         * 分割画像1枚ごとに呼ばれる。
         * @param image
         * @return 処理を続行する場合true
         */
        public boolean onSubTextureLoadingStart(Background image);

        /**
         * テクスチャへの転送が終わった時に呼び出される。
         * @param image
         * @param completed コンプリート率を0.0f〜1.0fで設定される
         * @return
         */
        public void onSubTextureLoadingComplete(Background image, float completed);

        /**
         * 全テクスチャへの転送が完了した場合に呼び出される。
         * @param image
         * @return
         */
        public void onTextureLoadingComplete(Background image);
    }
}
