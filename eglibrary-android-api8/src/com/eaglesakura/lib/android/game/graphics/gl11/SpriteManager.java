package com.eaglesakura.lib.android.game.graphics.gl11;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Rect;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.Color;
import com.eaglesakura.lib.android.game.graphics.DisposableResource;
import com.eaglesakura.lib.android.game.graphics.ImageBase;
import com.eaglesakura.lib.android.game.graphics.Sprite;

/**
 * スプライト描画の管理を行う。
 * 
 * @author Takeshi
 * 
 */
public class SpriteManager extends DisposableResource {
    //! 投影先の計算
    VirtualDisplay virtualDisplay;

    //! OpenGL
    OpenGLManager glManager;

    //! 四角形描画用
    QuadDepthPolygon quadPolygon;

    int contextColor = 0xffffffff;

    TextureImageBase texture = null;
    FontTexture fontTexture = null;
    boolean textureMatrixIdentity = true;

    Rect renderArea = null;

    static final float DEPTH_DEFAULT = 1.0f;
    float polyDepth = DEPTH_DEFAULT;

    /**
     * 
     * @param display
     * @param gl
     */
    public SpriteManager(VirtualDisplay display, OpenGLManager gl) {
        this.virtualDisplay = display;
        this.glManager = gl;
        init();

        //! 描画先座標を設定
        glManager.updateDrawArea(virtualDisplay);
    }

    void init() {
        quadPolygon = new QuadDepthPolygon(glManager, 0);
    }

    /**
     * GLを取得する。
     * 
     * @return
     */
    public OpenGLManager getGlManager() {
        return glManager;
    }

    /**
     * 仮想ディスプレイ環境を取得する。
     * 
     * @return
     */
    public VirtualDisplay getVirtualDisplay() {
        return virtualDisplay;
    }

    /**
     * 描画エリアを指定位置に変更する。
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setRenderArea(int x, int y, int width, int height) {
        renderArea = new Rect(x, y, x + width, y + height);
        final GL11 gl = getGlManager().getGL();
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_ALWAYS);
        polyDepth = 0.1f;
        fillRect(x, y, width, height, 0x00000000);
        gl.glDepthFunc(GL10.GL_EQUAL);
    }

    /**
     * 描画エリアを全体に直す。
     */
    public void clearRenderArea() {
        if (renderArea == null) {
            return;
        }
        final GL11 gl = getGlManager().getGL();
        gl.glDepthFunc(GL10.GL_ALWAYS);
        polyDepth = DEPTH_DEFAULT;
        fillRect(renderArea.left - 1, renderArea.top - 1, renderArea.width() + 2, renderArea.height() + 2, 0x00000000);
        renderArea = null;
        gl.glDisable(GL10.GL_DEPTH_TEST);
    }

    /**
     * 描画開始時に必ず呼び出す必要がある。
     */
    public void begin() {
        glManager.resetCamera();
        glManager.resetWorldMatrix();
        GL11 gl = glManager.getGL();

        //! ライト無効化
        gl.glDisable(GL10.GL_LIGHTING);

        //! 深度テストの無効化と、評価式の変更
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_ALWAYS);

        //! 描画用四角形の関連付け
        quadPolygon.bind();

        //! ポリゴン色の設定
        setColor(0xffffffff);

        //! テクスチャ行列のリセット
        {
            gl.glMatrixMode(GL10.GL_TEXTURE);
            textureMatrixIdentity = true;
            gl.glLoadIdentity();
            gl.glMatrixMode(GL10.GL_MODELVIEW);
        }
    }

    /**
     * スプライト情報を元に描画する。
     * 
     * @param sprite
     */
    public void draw(Sprite sprite) {
        Rect src = sprite.getSrcRect();
        Rect dst = sprite.getDstRect();
        drawImage(sprite.getImage(), src.left, src.top, src.width(), src.height(), dst.left, dst.top, dst.width(),
                dst.height(), sprite.getRotateDegree(), sprite.getColorRGBA());
    }

    /**
     * 単純な位置に描画を行う。
     * @param image
     * @param x
     * @param y
     */
    public void drawImage(ImageBase image, int x, int y) {
        drawImage(image, 0, 0, image.getWidth(), image.getHeight(), x, y, image.getWidth(), image.getHeight(), 0,
                0xffffffff);
    }

    /**
     * 単純な位置に描画を行う。
     * @param image
     * @param x
     * @param y
     * @param colorRGBA
     */
    public void drawImage(ImageBase image, int x, int y, int colorRGBA) {
        drawImage(image, 0, 0, image.getWidth(), image.getHeight(), x, y, image.getWidth(), image.getHeight(), 0,
                colorRGBA);
    }

    /**
     * 画像の描画を行う。
     * 
     * @param image
     * @param srcX
     * @param srcY
     * @param srcWidth
     * @param srcHeight
     * @param dstX
     * @param dstY
     * @param dstWidth
     * @param dstHeight
     * @param degree
     * @param colorRGBA
     */
    public void drawImage(ImageBase image, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY,
            int dstWidth, int dstHeight, float degree, int colorRGBA) {

        //! 描画が画面外のため何もしない
        if (renderArea != null) {
            final int left = dstX;
            final int top = dstY;
            final int right = (dstX + dstWidth);
            final int bottom = (dstY + dstHeight);

            //! 右側が0を下回っている、左側が画面サイズよりも大きい、下が画面から見切れている、上が画面から見切れている、いずれかがヒット
            if (right < renderArea.left || left > (int) renderArea.right || bottom < renderArea.top
                    || top > (int) renderArea.bottom) {
                return;
            }
        }

        //! 描画が画面外のため、何もしない。
        if (getVirtualDisplay().isOutsideVirtual(dstX, dstY, dstWidth, dstHeight)) {
            return;
        }

        if (image != texture) {
            if (!(image instanceof TextureImageBase)) {
                throw new IllegalArgumentException("image is not TextureImageBase!!");
            }
            setTexture((TextureImageBase) image);
        }

        final float displayWidth = virtualDisplay.getVirtualDisplayWidth();
        final float displayHeight = virtualDisplay.getVirtualDisplayHeight();

        GL11 gl = glManager.getGL();

        setColor(colorRGBA);

        //! 描画位置を行列で操作する
        {
            float sizeX = (float) dstWidth / (float) displayWidth * 2;
            float sizeY = (float) dstHeight / (float) displayHeight * 2;
            float sx = (float) dstX / (float) displayWidth * 2;
            float sy = (float) dstY / (float) displayHeight * 2;

            gl.glLoadIdentity();
            final float translateX = -1.0f + sizeX / 2.0f + sx;
            final float translateY = 1.0f - sizeY / 2.0f - sy;
            gl.glTranslatef(translateX, translateY, polyDepth);
            gl.glScalef(sizeX, sizeY, 1.0f);
            gl.glRotatef(degree, 0, 0, 1);
        }

        //! テクスチャ位置を行列で操作する
        {
            gl.glMatrixMode(GL10.GL_TEXTURE);

            //! identity以外の設定がされている場合、もとに戻す
            if (!textureMatrixIdentity) {
                gl.glLoadIdentity();
                textureMatrixIdentity = true;
            }

            //! identity以外の行列が必要な場合は加工
            if (srcX != 0 || srcY != 0 || image.getWidth() != srcWidth || image.getHeight() != srcHeight
                    || texture.getTextureScaleX() != 1.0f || texture.getTextureScaleY() != 1.0f) {
                float sizeX = (float) srcWidth / (float) image.getWidth();
                float sizeY = (float) srcHeight / (float) image.getHeight();
                float sx = (float) srcX / (float) image.getWidth();
                float sy = (float) srcY / (float) image.getHeight();

                gl.glScalef(texture.getTextureScaleX(), texture.getTextureScaleY(), 1);
                gl.glTranslatef(sx, sy, 0.0f);
                gl.glScalef(sizeX, sizeY, 1.0f);
                textureMatrixIdentity = false;
            }
            gl.glMatrixMode(GL10.GL_MODELVIEW);
        }

        //! 描画する
        quadPolygon.draw();
    }

    /**
     * 仮想ディスプレイを全て消去する。
     * 
     * @param colorRGBA
     */
    public void clear(int colorRGBA) {
        fillRect(0, 0, virtualDisplay.getVirtualDisplayWidth(), virtualDisplay.getVirtualDisplayHeight(), colorRGBA);
    }

    /**
     * 仮想ディスプレイの内容を単色クリアする。
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public void clear(int r, int g, int b, int a) {
        clear(Color.toColorRGBA(r, g, b, a));
    }

    /**
     * 四角形を描画する。
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void fillRect(int x, int y, int width, int height, int colorRGBA) {
        final float displayWidth = virtualDisplay.getVirtualDisplayWidth();
        final float displayHeight = virtualDisplay.getVirtualDisplayHeight();

        GL11 gl = glManager.getGL();

        setColor(colorRGBA);
        setTexture(null);

        //! 描画位置を行列で操作する
        {
            float sizeX = (float) width / (float) displayWidth * 2;
            float sizeY = (float) height / (float) displayHeight * 2;
            float sx = (float) x / (float) displayWidth * 2;
            float sy = (float) y / (float) displayHeight * 2;

            gl.glLoadIdentity();
            final float translateX = -1.0f + sizeX / 2.0f + sx;
            final float translateY = 1.0f - sizeY / 2.0f - sy;
            gl.glTranslatef(translateX, translateY, polyDepth);
            gl.glScalef(sizeX, sizeY, 1.0f);
        }

        //! 描画する
        quadPolygon.draw();
    }

    /**
     * ポリゴンの描画色を指定する。
     * 
     * @param colorRGBA
     */
    void setColor(int colorRGBA) {
        //! 描画色指定
        if (contextColor != colorRGBA) {
            glManager.getGL().glColor4x(((colorRGBA >> 24) & 0xff) * 0x10000 / 255,
                    ((colorRGBA >> 16) & 0xff) * 0x10000 / 255, ((colorRGBA >> 8) & 0xff) * 0x10000 / 255,
                    ((colorRGBA & 0xff)) * 0x10000 / 255);
            contextColor = colorRGBA;
        }

        if (fontTexture != null) {
            fontTexture.setFontColorRGBA(colorRGBA);
        }
    }

    /**
     * 描画用テクスチャを設定する。
     * 
     * @param newTexture
     */
    void setTexture(TextureImageBase newTexture) {

        //! 同じテクスチャなら何もしない
        if (texture == newTexture) {
            return;
        }

        //! 古いテクスチャを廃棄する。
        if (texture != null) {
            texture.unbind();
            texture = null;
        }

        //! 新しいテクスチャをバインドする。
        if (newTexture != null) {
            texture = newTexture;
            newTexture.bind();
        }

        //! テクスチャがフォントだったら補正用に設定する。
        if (texture instanceof FontTexture) {
            fontTexture = (FontTexture) texture;
        } else {
            fontTexture = null;
        }
    }

    /**
     * 描画終了時に必ず呼び出す必要がある。
     */
    public void end() {
        final GL11 gl = getGlManager().getGL();

        //! 描画用四角形の廃棄
        quadPolygon.unbind();

        //! 評価式を標準に戻す
        gl.glDepthFunc(GL10.GL_LESS);

        //! テクスチャをアンバインドする。
        setTexture(null);
    }

    /**
     * 管理している資源の開放を行う。
     */
    @Override
    public void dispose() {
        setTexture(null);
        if (quadPolygon != null) {
            quadPolygon.dispose();
            quadPolygon = null;
        }
    }
}
