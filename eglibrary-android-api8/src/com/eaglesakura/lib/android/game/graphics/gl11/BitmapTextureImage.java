package com.eaglesakura.lib.android.game.graphics.gl11;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLUtils;

import com.eaglesakura.lib.android.game.graphics.canvas.Graphics;

/**
 * Bitmap画像を利用してテクスチャを生成する。
 * 
 * @author TAKESHI YAMASHITA
 * 
 */
public class BitmapTextureImage extends TextureImageBase {

    /**
     * 画像を利用してテクスチャを生成する。<BR>
     * 引数imageはrecycleされないので、外部で適宜解放すること。<BR>
     * <BR>
     * GLに適さないテクスチャサイズの場合、拡大する。
     * 
     * @param image
     * @param glManager
     */
    public BitmapTextureImage(Bitmap image, OpenGLManager glManager) {
        super(glManager);
        if (image == null) {
            throw new NullPointerException("input bitmap image is null !!");
        }
        initTexture(image);
    }

    /**
     * 
     * @param glManager
     */
    protected BitmapTextureImage(OpenGLManager glManager) {
        super(glManager);
    }

    /**
     * 画像からテクスチャを生成する。
     * @param image
     */
    protected void initTexture(Bitmap image) {
        dispose();
        Bitmap convertedImage = convertBitmap(image);
        width = image.getWidth();
        height = image.getHeight();

        //! テクスチャ倍率を補正する
        {
            textureScale.x = (float) width / (float) convertedImage.getWidth();
            textureScale.y = (float) height / (float) convertedImage.getHeight();
        }

        //! テクスチャ情報を転送する
        textureId = glManager.genTexture();
        bind();
        {
            glManager.getGL().glGetError();
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, convertedImage, 0);
            if (glManager.getGL().glGetError() == GL11.GL_OUT_OF_MEMORY) {
                throw new OutOfMemoryError("GLUtils#texImage2D");
            }

            //! 一時生成したテクスチャなら解放する。
            //! 引数imageの開放は外部に任せる
            if (convertedImage != image) {
                convertedImage.recycle();
                convertedImage = null;
            }
            setTextureLinearFilter(false);
        }
        unbind();

        syncGC();
    }

    /**
     * @param image
     * @return
     */
    protected Bitmap convertBitmap(Bitmap image) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        final int glTexWidth = toGLTextureSize(width);
        final int glTexHeight = toGLTextureSize(height);

        //! 正規のインプットがあったばあい、そのまま帰す。
        if (width == glTexWidth && height == glTexHeight) {
            return image;
        }

        //! 適したサイズのBitmapを作成する
        //! 余白が発生するが、それは描画時のテクスチャ行列で修正してやる
        {
            Bitmap bitmap = Bitmap.createBitmap(glTexWidth, glTexHeight, image.getConfig());
            Graphics g = new Graphics();
            g.setCanvas(new Canvas(bitmap));

            g.setColorRGBA(255, 255, 255, 255);
            g.drawBitmap(image, 0, 0);
            return bitmap;
        }
    }
}
