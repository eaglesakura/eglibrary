package com.eaglesakura.lib.android.game.graphics.gl11;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.eaglesakura.lib.android.game.graphics.ImageBase;
import com.eaglesakura.lib.android.game.math.Vector2;

/**
 * GLES 1.1でのテクスチャを管理する。<BR>
 * GLのテクスチャサイズ制限を回避するため、画像サイズは2^nピクセルになるよう余白が追加される。
 * @author Takeshi
 *
 */
public abstract class TextureImageBase extends ImageBase {

    static final boolean DEBUG = false;

    /**
     * 関連付けるGL
     */
    protected OpenGLManager glManager;

    /**
     * バインド対象
     */
    protected int textureId = GL_NULL;

    /**
     * テクスチャの幅
     */
    protected int width = 2;

    /**
     * テクスチャの高さ
     */
    protected int height = 2;

    /**
     * 元の画像からの倍率。
     * 通常のテクスチャであれば 1.0 / 1.0が格納されている。
     */
    protected Vector2 textureScale = new Vector2(1, 1);

    protected TextureImageBase(OpenGLManager glManager) {
        super(glManager.getGarbageCollector());
        this.glManager = glManager;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public List<GLResource> getRawResources() {
        List<GLResource> result = new LinkedList<DisposableGLResource.GLResource>();
        if (textureId != GL_NULL) {
            result.add(new GLResource(Type.Texture, textureId));
        }
        return result;
    }

    @Override
    public void onDispose() {
        if (textureId != GL_NULL) {
            glManager.deleteTexture(textureId);
            textureId = GL_NULL;
        }
    }

    /**
     * テクスチャ管理番号を取得する。
     * 
     * @return
     */
    public int getTextureID() {
        return textureId;
    }

    /**
     * OpenGLへ関連付ける。
     */
    public void bind() {
        if (textureId == GL_NULL) {
            return;
        }
        GL11 gl = glManager.getGL();
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, getTextureID());
    }

    /**
     * UVのバインドを行わせる。
     */
    public void bindTextureCoord(int x, int y, int w, int h) {
        GL11 gl = glManager.getGL();
        gl.glMatrixMode(GL10.GL_TEXTURE);
        gl.glLoadIdentity();

        if (x == 0 && y == 0 && w == getWidth() && h == getHeight() && getTextureScaleX() == 1
                && getTextureScaleY() == 1) {
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            return;
        }

        float sizeX = (float) w / (float) getWidth();
        float sizeY = (float) h / (float) getHeight();
        float sx = (float) x / (float) getWidth();
        float sy = (float) y / (float) getHeight();

        gl.glScalef(getTextureScaleX(), getTextureScaleY(), 1);
        gl.glTranslatef(sx, sy, 0.0f);
        gl.glScalef(sizeX, sizeY, 1.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    /**
     * OpenGLへの関連付けを解除する。
     */
    public void unbind() {
        if (textureId == GL_NULL) {
            return;
        }
        GL11 gl = glManager.getGL();
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    /**
     * 横方向のテクスチャスケーリングを取得する。
     * Origin-Texture Width / GLES-Texture Widthが格納される。
     * @return
     */
    public float getTextureScaleX() {
        return textureScale.x;
    }

    /**
     * 縦方向のテクスチャスケーリングを取得する。
     * Origin-Texture Height / GLES-Texture Heightが格納される。
     * @return
     */
    public float getTextureScaleY() {
        return textureScale.y;
    }

    /**
     * テクスチャのフィルタを指定する。 フィルタはリニアの場合true／ニアレストネイバー法の場合はfalseを指定する。
     * 
     * @param linear
     */
    public void setTextureLinearFilter(boolean linear) {
        if (textureId == GL_NULL) {
            return;
        }

        GL11 gl = glManager.getGL();
        int type = linear ? GL10.GL_LINEAR : GL10.GL_NEAREST;
        //! テクスチャ属性指定。
        bind();
        {
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, type);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, type);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, type);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, type);
        }
        unbind();
    }

    /**
     * baseSizeをテクスチャに適した大きさに変更する。
     * @param baseSize
     * @return
     */
    protected static int toGLTextureSize(int baseSize) {
        int result = 2;
        while (result <= 2048) {
            if (baseSize <= result) {
                return result;
            }
            result *= 2;
        }
        return result;
    }
}
