package com.eaglesakura.lib.android.game.graphics.gl11;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.Color;
import com.eaglesakura.lib.android.game.graphics.ImageBase;
import com.eaglesakura.lib.android.game.graphics.Sprite;
import com.eaglesakura.lib.android.game.resource.DisposableResource;

import android.graphics.Rect;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * スプライト描画の管理を行う。
 *
 * @author TAKESHI YAMASHITA
 */
public class SpriteManager extends DisposableResource {
    //! 投影先の計算
    VirtualDisplay virtualDisplay;

    //! OpenGL
    GPU gpu;

    //! 四角形描画用
    QuadPolygon quadPolygon;

    int contextColor = 0xffffffff;

    TextureImageBase texture = null;
    FontTexture fontTexture = null;

    Rect renderArea = null;

    /**
     *
     * @param display
     * @param gl
     */
    public SpriteManager(VirtualDisplay display, GPU gpu) {
        this.virtualDisplay = display;
        this.gpu = gpu;
        init();
    }

    void init() {
        quadPolygon = new QuadPolygon(gpu.getVRAM());
    }

    /**
     * GLを取得する。
     */
    public GPU getGPU() {
        return gpu;
    }

    /**
     * 仮想ディスプレイ環境を取得する。
     */
    public VirtualDisplay getVirtualDisplay() {
        return virtualDisplay;
    }

    /**
     * 描画エリアを指定位置に変更する。
     */
    public void setRenderArea(int x, int y, int width, int height) {
    }

    /**
     * 描画エリアを全体に直す。
     */
    public void clearRenderArea() {
    }

    /**
     * 描画開始時に必ず呼び出す必要がある。
     */
    public void begin() {
        gpu.resetCamera();
        gpu.resetWorldMatrix();
        //! 描画先座標を設定
        gpu.updateDrawArea(virtualDisplay);

        GL11 gl = gpu.getGL();

        //! ライト無効化
        gl.glDisable(GL10.GL_LIGHTING);

        //! 深度テストの無効化と、評価式の変更
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_ALWAYS);

        //! 描画用四角形の関連付け
        quadPolygon.bind();

        //! ポリゴン色の設定
        setColor(0xffffffff);
        gl.glColor4f(1, 1, 1, 1);

        //! テクスチャ行列のリセット
        {
            gl.glMatrixMode(GL10.GL_TEXTURE);
            gl.glLoadIdentity();
            gl.glMatrixMode(GL10.GL_MODELVIEW);
        }
    }

    /**
     * スプライト情報を元に描画する。
     */
    public void draw(Sprite sprite) {
        Rect src = sprite.getSrcRect();
        Rect dst = sprite.getDstRect();
        drawImage(sprite.getImage(), src.left, src.top, src.width(), src.height(), dst.left, dst.top, dst.width(),
                dst.height(), sprite.getRotateDegree(), sprite.getColorRGBA());
    }

    /**
     * 単純な位置に描画を行う。
     */
    public void drawImage(ImageBase image, int x, int y) {
        drawImage(image, 0, 0, image.getWidth(), image.getHeight(), x, y, image.getWidth(), image.getHeight(), 0,
                0xffffffff);
    }

    /**
     * 単純な位置に描画を行う。
     */
    public void drawImage(ImageBase image, int x, int y, int colorRGBA) {
        drawImage(image, 0, 0, image.getWidth(), image.getHeight(), x, y, image.getWidth(), image.getHeight(), 0,
                colorRGBA);
    }

    /**
     * 画像の描画を行う。
     */
    public void drawImage(ImageBase image, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY,
                          int dstWidth, int dstHeight, float degree, int colorRGBA) {

        final int left = dstX;
        final int top = dstY;
        final int right = (dstX + dstWidth);
        final int bottom = (dstY + dstHeight);
        //! 描画が画面外のため何もしない
        if (renderArea != null) {

            //! 右側が0を下回っている、左側が画面サイズよりも大きい、下が画面から見切れている、上が画面から見切れている、いずれかがヒット
            if (right < renderArea.left || left > (int) renderArea.right || bottom < renderArea.top
                    || top > (int) renderArea.bottom) {
                return;
            }
        } else {
            if (left > virtualDisplay.getVirtualDisplayWidth() || right < 0
                    || top > virtualDisplay.getVirtualDisplayHeight() || bottom < 0) {
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

        GL11 gl = gpu.getGL();

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
            gl.glTranslatef(translateX, translateY, 0);

            // aspectによる歪みを抑制する
            {
                final float aspect = displayWidth / displayHeight;
                gl.glScalef(1.0f / aspect, 1.0f, 1.0f);
                gl.glRotatef(degree, 0, 0, 1);
                gl.glScalef(sizeX * aspect, sizeY, 1.0f);
            }
        }

        //! テクスチャ位置を行列で操作する
        {
            texture.bindTextureCoord(srcX, srcY, srcWidth, srcHeight);
        }

        //! 描画する
        quadPolygon.draw();
    }

    /**
     * 仮想ディスプレイを全て消去する。
     */
    public void clear(int colorRGBA) {
        fillRect(0, 0, virtualDisplay.getVirtualDisplayWidth(), virtualDisplay.getVirtualDisplayHeight(), colorRGBA);
    }

    /**
     * 仮想ディスプレイの内容を単色クリアする。
     */
    public void clear(int r, int g, int b, int a) {
        clear(Color.toColorRGBA(r, g, b, a));
    }

    /**
     * 四角形を描画する
     */
    public void fillRect(Rect area, int colorRGBA) {
        if (area == null) {
            return;
        }

        fillRect(area.left, area.top, area.width(), area.height(), colorRGBA);
    }

    /**
     * 四角形を描画する。
     */
    public void fillRect(int x, int y, int width, int height, int colorRGBA) {
        final float displayWidth = virtualDisplay.getVirtualDisplayWidth();
        final float displayHeight = virtualDisplay.getVirtualDisplayHeight();

        GL11 gl = gpu.getGL();

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
            gl.glTranslatef(translateX, translateY, 0);
            gl.glScalef(sizeX, sizeY, 1.0f);
        }

        //! 描画する
        quadPolygon.draw();
    }

    /**
     * ポリゴンの描画色を指定する。
     */
    void setColor(int colorRGBA) {
        //! 描画色指定
        if (contextColor != colorRGBA) {
            gpu.getGL().glColor4x(((colorRGBA >> 24) & 0xff) * 0x10000 / 255,
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
        final GL11 gl = gpu.getGL();

        //! 描画用四角形の廃棄
        quadPolygon.unbind();

        //! 評価式を標準に戻す
        gl.glDepthFunc(GL10.GL_LESS);

        {
            gl.glMatrixMode(GL10.GL_TEXTURE);
            gl.glLoadIdentity();
            gl.glMatrixMode(GL10.GL_MODELVIEW);
        }

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
