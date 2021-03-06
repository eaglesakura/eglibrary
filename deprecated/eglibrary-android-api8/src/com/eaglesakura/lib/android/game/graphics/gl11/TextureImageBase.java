package com.eaglesakura.lib.android.game.graphics.gl11;

import com.eaglesakura.lib.android.game.graphics.ImageBase;
import com.eaglesakura.lib.android.game.graphics.gl11.DisposableGLResource.GLResource;
import com.eaglesakura.lib.android.game.graphics.gl11.DisposableGLResource.Type;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.VRAM;
import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.resource.IRawResource;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * GLES 1.1でのテクスチャを管理する。<BR>
 * GLのテクスチャサイズ制限を回避するため、画像サイズは2^nピクセルになるよう余白が追加される。
 *
 * @author TAKESHI YAMASHITA
 */
public abstract class TextureImageBase extends ImageBase {

    /**
     * GL管理クラス
     */
    protected VRAM vram = null;

    /**
     * バインド対象
     */
    protected int textureId = VRAM.NULL;

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

    protected TextureImageBase(VRAM vram) {
        super(vram.getGarbageCollector());
        this.vram = vram;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    protected GL11 getGL() {
        return vram.getGL();
    }

    @Override
    public List<IRawResource> getRawResources() {
        List<IRawResource> result = new LinkedList<IRawResource>();
        if (textureId != VRAM.NULL) {
            result.add(new GLResource(getGL(), Type.Texture, textureId));
        }
        return result;
    }

    @Override
    public void onDispose() {
        if (textureId != VRAM.NULL) {
            vram.deleteTexture(textureId);
            textureId = VRAM.NULL;
        }
    }

    /**
     * テクスチャ管理番号を取得する。
     */
    public int getTextureID() {
        return textureId;
    }

    /**
     * OpenGLへ関連付ける。
     */
    public void bind() {
        if (textureId == VRAM.NULL) {
            return;
        }
        GL11 gl = getGL();
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, getTextureID());
    }

    /**
     * UVのバインドを行わせる。
     */
    public void bindTextureCoord(int x, int y, int w, int h) {
        GL11 gl = getGL();
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
        if (textureId == VRAM.NULL) {
            return;
        }
        GL11 gl = getGL();
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    /**
     * 横方向のテクスチャスケーリングを取得する。
     * Origin-Texture Width / GLES-Texture Widthが格納される。
     */
    public float getTextureScaleX() {
        return textureScale.x;
    }

    /**
     * 縦方向のテクスチャスケーリングを取得する。
     * Origin-Texture Height / GLES-Texture Heightが格納される。
     */
    public float getTextureScaleY() {
        return textureScale.y;
    }

    /**
     * テクスチャのフィルタを指定する。 フィルタはリニアの場合true／ニアレストネイバー法の場合はfalseを指定する。
     */
    public void setTextureLinearFilter(boolean linear) {
        if (textureId == VRAM.NULL) {
            return;
        }

        GL11 gl = getGL();
        int type = linear ? GL10.GL_LINEAR : GL10.GL_NEAREST;
        //! テクスチャ属性指定。
        bind();
        {
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, type);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, type);
        }
        unbind();
    }

    /**
     * baseSizeをテクスチャに適した大きさに変更する。
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
