/**
 * サーフェース内のCanvasを管理する。
 * @author eagle.sakura
 * @version 2009/11/29 : 新規作成
 */
package com.eaglesakura.lib.android.game.graphics.canvas;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;

/**
 * プリミティブの描画を管理する。
 * 
 * @author eagle.sakura
 */
public class Graphics {
    Canvas canvas = null;
    Paint paint = new Paint();

    /**
     * @author eagle.sakura
     * @param target
     */
    public Graphics() {
        setFontSize(24);
        clearMatrix();
    }

    /**
     * 描画用のキャンバスを指定する。
     * 
     * @author eagle.sakura
     * @param canvas
     */
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        if (canvas != null) {
            setWidth(canvas.getWidth());
            setHeight(canvas.getHeight());
        }
    }

    int width = 0;
    int height = 0;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * 描画時のスケーリングを指定する。
     * 
     * @param x
     * @param y
     */
    public void setDrawScalling(float x, float y) {
        Matrix m = new Matrix();
        m.setScale(x, y);
        canvas.setMatrix(m);
    }

    /**
     * 背景を特定色でクリアする。
     * 
     * @author eagle.sakura
     * @param r
     * @param g
     * @param b
     * @param a
     * @version 2009/11/29 : 新規作成
     */
    public void clearRGBA(int r, int g, int b, int a) {
        canvas.drawColor(toColorARGB(a, r, g, b), Mode.CLEAR);
    }

    /**
     * 幅を取得する。
     * 
     * @author eagle.sakura
     * @version 2009/11/30 : 新規作成
     */
    public int getWidth() {
        return width;
    }

    /**
     * 高さを取得する。
     * 
     * @author eagle.sakura
     * @return
     * @version 2009/11/30 : 新規作成
     */
    public int getHeight() {
        return height;
    }

    /**
     * ロックされたキャンバスを取得する。
     * 
     * @author eagle.sakura
     * @return
     * @version 2009/11/29 : 新規作成
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * ペイントクラスを取得する。
     * 
     * @author eagle.sakura
     * @return
     * @version 2009/11/29 : 新規作成
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * 描画色を指定する。
     * 
     * @author eagle.sakura
     * @param a
     * @param r
     * @param g
     * @param b
     */
    public void setColorRGBA(int r, int g, int b, int a) {
        paint.setColor(toColorARGB(a, r, g, b));
    }

    /**
     * アンチエイリアスの有効・無効の指定を行う。<BR>
     * デフォルトは無効である。
     * 
     * @author eagle.sakura
     * @param set
     */
    public void setAntiAlias(boolean set) {
        paint.setAntiAlias(set);
    }

    /**
     * 描画する太さを指定する。
     * 
     * @param size
     */
    public void setStrokeSize(int size) {
        paint.setStrokeWidth(size);
    }

    /**
     * イメージの描画を行う。
     * 
     * @author eagle.sakura
     * @param bmp
     * @param src
     * @param dst
     */
    public void drawBitmap(Bitmap bmp, Rect src, Rect dst) {
        canvas.drawBitmap(bmp, src, dst, paint);
    }

    /**
     * イメージ描画を行う。
     * 
     * @author eagle.sakura
     * @param bmp
     * @param x
     * @param y
     */
    public void drawBitmap(Bitmap bmp, int x, int y) {
        canvas.drawBitmap(bmp, (float) x, (float) y, paint);
    }

    /**
     * イメージ描画を行う。
     * 
     * @author eagle.sakura
     * @param bmp
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public void drawBitmap(Bitmap bmp, int x, int y, int w, int h) {
        srcRect.set(0, 0, bmp.getWidth(), bmp.getHeight());
        dstRect.set(x, y, x + w, y + h);
        drawBitmap(bmp, srcRect, dstRect);
    }

    /**
     * 
     * @author eagle.sakura
     * @param x
     * @param y
     * @param r
     * @version 2010/05/26 : 新規作成
     */
    public void drawCircle(int x, int y, float r) {
        paint.setStyle(Style.STROKE);
        canvas.drawCircle((float) x, (float) y, r, paint);
    }

    /**
     * 
     * @author eagle.sakura
     * @param x
     * @param y
     * @param r
     * @version 2010/05/26 : 新規作成
     */
    public void fillCircle(int x, int y, float r) {
        paint.setStyle(Style.FILL);
        canvas.drawCircle((float) x, (float) y, r, paint);
    }

    /**
     * 四角形の塗りつぶしを行う。
     * 
     * @author eagle.sakura
     * @param x
     * @param y
     * @param w
     * @param h
     * @version 2009/11/29 : 新規作成
     */
    public void fillRect(int x, int y, int w, int h) {
        paint.setStyle(Style.FILL);
        canvas.drawRect((float) x, (float) y, (float) (x + w), (float) (y + h), paint);
    }

    /**
     * 四角形の外枠を描画する。
     * 
     * @author eagle.sakura
     * @param x
     * @param y
     * @param w
     * @param h
     * @version 2009/11/29 : 新規作成
     */
    public void drawRect(int x, int y, int w, int h) {
        paint.setStyle(Style.STROKE);
        canvas.drawRect((float) x, (float) y, (float) (x + w), (float) (y + h), paint);
    }

    /**
     * テキストを左揃えにする。
     */
    public static final int STRING_LEFT = 0;

    /**
     * テキストを上揃えにする。
     */
    public static final int STRING_TOP = 0;

    /**
     * テキストを下揃えにする。
     */
    public static final int STRING_BOTTOM = 1 << 0;
    /**
     * テキストを右揃えにする。
     */
    public static final int STRING_RIGHT = 1 << 1;

    /**
     * 中央揃えにする。
     */
    public static final int STRING_CENTER_X = 1 << 2;

    /**
     * 中央揃えにする。
     */
    public static final int STRING_CENTER_Y = 1 << 3;

    /**
     * 文字列を描画する。
     * 
     * @author eagle.sakura
     * @param str
     * @param x
     * @param y
     * @param start
     * @param end
     * @param flags
     * @version 2009/12/13 : 新規作成
     */
    public void drawString(String str, int x, int y, int start, int end, int flags) {
        if (start < 0 || end < 0) {
            start = 0;
            end = str.length();
        }

        Rect area = new Rect();
        paint.getTextBounds(str, start, end, area);
        int width = area.width();
        int height = area.height();

        //! left / topに位置を合わせる
        x -= area.left;
        y -= area.top;

        if ((flags & STRING_CENTER_X) != 0) {
            x -= (width >> 1);
        } else if ((flags & STRING_RIGHT) != 0) {
            x -= width;
        }

        if ((flags & STRING_CENTER_Y) != 0) {
            y -= (height >> 1);
        } else if ((flags & STRING_BOTTOM) != 0) {
            y -= height;
        }

        canvas.drawText(str, start, end, (float) x, (float) y, paint);
    }

    /**
     * 文字列の高さを取得する。
     * 
     * @author eagle.sakura
     * @param str
     * @return
     * @version 2009/12/13 : 新規作成
     */
    public int getStringHeight(String str) {
        Rect r = srcRect;
        paint.getTextBounds(str, 0, str.length(), r);
        return r.height();
    }

    /**
     * 文字列の幅を取得する。
     * 
     * @author eagle.sakura
     * @param str
     * @param start
     * @param end
     * @return
     * @version 2009/12/13 : 新規作成
     */
    public int getStringWidth(String str, int start, int end) {
        Rect r = srcRect;
        paint.getTextBounds(str, start, end, r);
        return r.width();
    }

    /**
     * 文字列の幅を取得する。
     * 
     * @author eagle.sakura
     * @param str
     * @return
     * @version 2009/12/13 : 新規作成
     */
    public int getStringWidth(String str) {
        Rect r = srcRect;
        paint.getTextBounds(str, 0, str.length(), r);
        return r.width();
    }

    /**
     * 線を描画する。
     * 
     * @author eagle.sakura
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @version 2009/12/02 : 新規作成
     */
    public void drawLine(int startX, int startY, int endX, int endY) {
        paint.setStyle(Style.STROKE);
        canvas.drawLine((float) startX, (float) startY, (float) endX, (float) endY, paint);
    }

    /**
     * 描画テキストのサイズを指定する。
     * 
     * @author eagle.sakura
     * @param size
     * @version 2009/11/14 : 新規作成
     */
    public void setFontSize(int size) {
        paint.setTextSize((float) size);
    }

    /**
     * ARGB色に変換する。
     * 
     * @author eagle.sakura
     * @param a
     * @param r
     * @param g
     * @param b
     * @return
     * @version 2009/11/14 : 新規作成
     */
    public static int toColorARGB(int a, int r, int g, int b) {
        return Color.argb(a, r, g, b);
    }

    /**
     * 描画行列スタック。
     */
    List<Matrix> matrixStack = new ArrayList<Matrix>();

    /**
     * 上に積まれた描画行列を全て消去する。
     */
    public void clearMatrix() {
        while (matrixStack.size() > 1) {
            matrixStack.remove(0);
        }
        if (matrixStack.size() == 0) {
            matrixStack.add(new Matrix());
        }
        if (canvas != null) {
            canvas.setMatrix(matrixStack.get(0));
        }
    }

    /**
     * 現在の描画行列×matrixをCanvasに指定する。
     * 
     * @param matrix
     */
    public void pushMatrix(Matrix matrix) {
        Matrix tempMatrix = new Matrix(matrixStack.get(0));
        tempMatrix.postConcat(matrix);
        matrixStack.add(0, tempMatrix);
        canvas.setMatrix(tempMatrix);
    }

    /**
     * 指定した行列を最上位に設定する。
     * 
     * @param matrix
     */
    public void loadMatrix(Matrix matrix) {
        matrixStack.remove(0);
        matrixStack.add(new Matrix(matrix));
        canvas.setMatrix(matrixStack.get(0));
    }

    /**
     * 最上位の行列を取り除き、下の行列をloadする。
     */
    public void popMatrix() {
        matrixStack.remove(0);
        canvas.setMatrix(matrixStack.get(0));
    }

    /**
     * テンポラリ領域。
     */
    private Rect srcRect = new Rect();

    private Rect dstRect = new Rect();
}
