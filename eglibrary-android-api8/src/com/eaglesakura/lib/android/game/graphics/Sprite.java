package com.eaglesakura.lib.android.game.graphics;

import android.graphics.Rect;

import com.eaglesakura.lib.android.game.graphics.SpriteMaster.AnimationFrame;
import com.eaglesakura.lib.android.game.input.MultiTouchInput;
import com.eaglesakura.lib.android.game.input.MultiTouchInput.TouchPoint;
import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.util.GameUtil;

/**
 * スプライト描画情報を記録する。
 * 
 * @author TAKESHI YAMASHITA
 * 
 */
public class Sprite {

    /**
     * 画像横方向をセンタリング。
     */
    public static final int POSITION_CENTER_X = 0x1 << 0;

    /**
     * 画像縦方向をセンタリング。
     */
    public static final int POSITION_CENTER_Y = 0x1 << 1;

    /**
     * 右寄せ描画
     */
    public static final int POSITION_RIGHT = 0x1 << 2;

    /**
     * 下寄せ描画
     */
    public static final int POSITION_BOTTOM = 0x1 << 3;

    /**
     * 上寄せ描画（標準）
     */
    public static final int POSITION_TOP = 0x0;

    /**
     * 左寄せ描画（標準）
     */
    public static final int POSITION_LEFT = 0x0;

    /**
     * 中央寄せ
     */
    public static final int POSITION_CENTER = POSITION_CENTER_X | POSITION_CENTER_Y;

    /**
     * 描画色。
     */
    int color = 0xffffffff;

    /**
     * 描画フレーム
     */

    int frame = 0;
    /**
     * 回転角度情報
     */
    float rotate = 0;

    SpriteMaster master;

    /**
     * 描画先情報。
     */
    Rect dstArea = null;

    /**
     * スプライトのスケーリング値
     */
    float scale = 1.0f;

    /**
     * マスターを指定して同一のスプライトを作成する
     * @param master
     */
    public Sprite(SpriteMaster master) {
        if (master == null) {
            throw new NullPointerException("master is null");
        }
        this.master = master;
        dstArea = new Rect(0, 0, master.image.getWidth(), master.image.getHeight());
    }

    /**
     * 単一のスプライトを作成する。
     * @param image
     */
    public Sprite(ImageBase image) {
        this(new SpriteMaster(image));
    }

    /**
     * 次のフレームを描画する。
     */
    public Sprite nextFrame() {
        setFrame(frame + master.frameOffset);
        return this;
    }

    /**
     * 現在のフレームを取得する
     * 
     * @return
     */
    protected AnimationFrame getCurrentFrame() {
        //! 画像が登録されていない場合、アニメーションをさせない。
        if (master.frames.size() == 0) {
            master.noAnimation();
        }

        int current = frame / master.komaFrame;
        return master.frames.get(GameUtil.minmax(0, master.frames.size() - 1, current));
    }

    /**
     * 指定したグリッドへSource位置を設定する。
     * @param onceWidth
     * @param onceHeight
     * @param index
     */
    public Sprite setSliceGrid(final int blockWidth, final int blockHeight, final int index) {
        Rect src = getCurrentFrame().area;

        //! 並べられるシートの数を数える
        final int x = (getImage().getWidth() / blockWidth);

        //! 画像シートの位置を求める
        final int px = (index % x);
        final int py = (index / x);

        src.left = px * blockWidth;
        src.top = py * blockHeight;
        src.right = (src.left + blockWidth);
        src.bottom = (src.top + blockHeight);
        return this;
    }

    /**
     * 描画フレームを設定する。
     */
    public Sprite setFrame(int newFrame) {
        frame = newFrame;
        int current = frame / master.komaFrame;
        if (current >= master.frames.size()) {
            frame += (master.komaFrame * master.endOffset);

        }
        //! 上限・下限の指定する
        frame = GameUtil.minmax(0, (master.frames.size()) * master.komaFrame, frame);
        return this;
    }

    /**
     * テクスチャの幅を取得する。
     */
    public int getSrcWidth() {
        return getCurrentFrame().area.width();
    }

    /**
     * テクスチャの高さを取得する。
     */
    public int getSrcHeight() {
        return getCurrentFrame().area.height();
    }

    /**
     * 描画領域の幅を取得する。
     * @return
     */
    public int getDstWidth() {
        return dstArea.width();
    }

    /**
     * 描画領域の高さを取得する。
     * @return
     */
    public int getDstHeight() {
        return dstArea.height();
    }

    public int getDstLeft() {
        return dstArea.left;
    }

    public int getDstRight() {
        return dstArea.right;
    }

    public int getDstTop() {
        return dstArea.top;
    }

    public int getDstBottom() {
        return dstArea.bottom;
    }

    public int getDstCenterX() {
        return dstArea.centerX();
    }

    public int getDstCenterY() {
        return dstArea.centerY();
    }

    /**
     * スプライトのスケーリング値を取得する。
     * @return
     */
    public float getScale() {
        return scale;
    }

    /**
     * デフォルトのスケーリング値ならtrueを返す。
     * ある程度のブレは許容する。
     * @return
     */
    public boolean isDefaultScale() {
        return scale >= 0.99999 && scale < 1.00001;
    }

    /**
     * スプライトのスケーリング値をmul倍する。
     * @param mul
     */
    public Sprite mulScaling(float mul) {
        scale *= mul;
        return this;
    }

    /**
     * スプライトのスケーリング値を設定する
     * @param scale
     */
    public Sprite setScale(float scale) {
        this.scale = scale;
        return this;
    }

    /**
     * 現在のフレームを取得する。
     * 
     * @return
     */
    public int getFrame() {
        return frame;
    }

    /**
     * アニメーションの遷移レベルを取得する。<BR>
     * 0.0f（0%）〜1.0f（100%）で遷移する。
     * 
     * @return
     */
    public float getAnimationProgress() {
        float maxFrame = master.komaFrame * master.frames.size();
        return GameUtil.minmax(0, 1, (float) frame / maxFrame);
    }

    /**
     * アニメーションが終了していたらtrueを返す。
     * 
     * @return
     */
    public boolean isAnimationFinish() {
        return getAnimationProgress() >= 1.0f;
    }

    /**
     * 描画位置を設定する。<BR>
     * デフォルト設定の場合、スプライトはセンタリングされる。
     * 
     * @param x
     * @param y
     * @param flags
     */
    public Sprite setSpritePosition(int x, int y) {
        return setSpritePosition(x, y, scale, scale, POSITION_CENTER_X | POSITION_CENTER_Y);
    }

    /**
     * 
     * @param x
     * @param y
     * @param flags
     */
    public Sprite setSpritePosition(int x, int y, int flags) {
        return setSpritePosition(x, y, scale, scale, flags);
    }

    /**
     * 描画位置を設定する。
     * 
     * @param x
     * @param y
     * @param scaleX
     * @param scaleY
     * @param flags
     */
    public Sprite setSpritePosition(int x, int y, float scaleX, float scaleY, int flags) {

        Rect src = getCurrentFrame().area;
        int dstWidth = (int) (scaleX * src.width());
        int dstHeight = (int) (scaleY * src.height());

        //! 横方向の補正を行う
        {
            if ((flags & POSITION_CENTER_X) != 0) {
                x -= (dstWidth / 2);
            } else if ((flags & POSITION_RIGHT) != 0) {
                x -= dstWidth;
            }
        }

        //! 縦方向の補正を行う
        {
            if ((flags & POSITION_CENTER_Y) != 0) {
                y -= (dstHeight / 2);
            } else if ((flags & POSITION_BOTTOM) != 0) {
                y -= (dstHeight);
            }
        }

        scale = (float) Math.sqrt(scaleX * scaleY);
        dstArea.set(x, y, x + dstWidth, y + dstHeight);

        return this;
    }

    /**
     * 描画位置を設定する。
     * 
     * @param x
     * @param y
     * @param scaleX
     * @param scaleY
     * @param flags
     */
    public Sprite setSpritePosition(int x, int y, int dstWidth, int dstHeight, int flags) {
        //! 横方向の補正を行う
        {
            if ((flags & POSITION_CENTER_X) != 0) {
                x -= (dstWidth / 2);
            } else if ((flags & POSITION_RIGHT) != 0) {
                x -= dstWidth;
            }
        }

        //! 縦方向の補正を行う
        {
            if ((flags & POSITION_CENTER_Y) != 0) {
                y -= (dstHeight / 2);
            } else if ((flags & POSITION_BOTTOM) != 0) {
                y -= (dstHeight);
            }
        }

        dstArea.set(x, y, x + dstWidth, y + dstHeight);
        scale = 1;
        return this;
    }

    /**
     * 指定したピクセル数、描画エリアを移動する。
     * @param x
     * @param y
     */
    public Sprite offsetSpritePosition(int x, int y) {
        dstArea.offset(x, y);
        return this;
    }

    /**
     * 関連付けられたスプライトのひな形を取得する。
     * 
     * @return
     */
    public SpriteMaster getMaster() {
        return master;
    }

    /**
     * 描画色RGBAを設定する。
     * 
     * @param color
     */
    public void setColorRGBA(int color) {
        this.color = color;
    }

    /**
     * 描画色RGBのみを設定する。
     * Aの値は保たれる。
     * @param colorRGBX
     */
    public Sprite setColorRGB(int colorRGBX) {
        this.color = ((colorRGBX & 0xffffff00) | (this.color & 0xff));
        return this;
    }

    /**
     * 色を指定した位置へ遷移させる
     * @param targetRGBA
     * @param offset
     */
    public Sprite moveColorRGBA(final int targetRGBA, final int offset) {
        final int nowR = Color.toColorR(color);
        final int nowG = Color.toColorG(color);
        final int nowB = Color.toColorB(color);
        final int nowA = Color.toColorA(color);

        final int nextR = Color.toColorR(targetRGBA);
        final int nextG = Color.toColorG(targetRGBA);
        final int nextB = Color.toColorB(targetRGBA);
        final int nextA = Color.toColorA(targetRGBA);

        color = Color.toColorRGBA(//
                GameUtil.targetMove(nowR, offset, nextR), //
                GameUtil.targetMove(nowG, offset, nextG), //
                GameUtil.targetMove(nowB, offset, nextB), //
                GameUtil.targetMove(nowA, offset, nextA) //
                );
        return this;
    }

    /**
     * αのみ遷移させる
     * RGBは固定される。
     * @param targetA
     * @param offset
     */
    public Sprite moveColorA(int targetA, int offset) {
        moveColorRGBA((color & 0xffffff00) | (targetA & 0xff), offset);
        return this;
    }

    /**
     * RGBのみ遷移させる。
     * αは固定される。
     * @param targetRGB
     * @param offset
     */
    public Sprite moveColorRGB(int targetRGB, int offset) {
        moveColorRGBA((targetRGB & 0xffffff00) | (color & 0xff), offset);
        return this;
    }

    /**
     * 画像のαのみを変更する
     * @param alpha
     */
    public Sprite setColorA(int alpha) {
        this.color = (color & 0xffffff00) | (alpha & 0xff);
        return this;
    }

    /**
     * 画像のアルファのみを変更する。
     * @param alpha
     */
    public Sprite setColorA(float alpha) {
        setColorA((int) (alpha * 255));
        return this;
    }

    /**
     * RGBA化された色情報を取得する。
     * 
     * @return
     */
    public int getColorRGBA() {
        return color;
    }

    public int getColorA() {
        return Color.toColorA(color);
    }

    public float getColorAf() {
        return Color.toColorAf(color);
    }

    /**
     * 描画色RGBAを設定する。 値は0.0f〜1.0fである必要がある。
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public Sprite setColorRGBA(float r, float g, float b, float a) {
        color = (((int) (r * 255)) << 24) | (((int) (g * 255)) << 16) | (((int) (b * 255)) << 8)
                | (((int) (a * 255)) << 0);
        return this;
    }

    /**
     * 関連付けられた画像を取得する。
     * 
     * @return
     */
    public ImageBase getImage() {
        return master.image;
    }

    /**
     * 描画すべき画像ブロックを取得する。
     * 
     * @return
     */
    public Rect getSrcRect() {
        return getCurrentFrame().area;
    }

    /**
     * 描画先のエリアを取得する。
     * 
     * @return
     */
    public Rect getDstRect() {
        return dstArea;
    }

    /**
     * 画像回転を取得する。
     * 
     * @return
     */
    public float getRotateDegree() {
        return rotate;
    }

    /**
     * 回転を設定する。<BR>
     * 回転はそのまま記録され、360度系に正規化はされない。<BR>
     * スプライトは反時計回りに回転する。
     * @param set
     */
    public void setRotateDegree(float set) {
        rotate = set;
    }

    /**
     * 衝突している場合trueを返す。
     * @param pos
     * @return
     */
    public boolean isIntersect(Vector2 pos) {
        return isIntersect((int) pos.x, (int) pos.y);
    }

    /**
     * 衝突している場合trueを返す。
     * @param pos
     * @return
     */
    public boolean isIntersect(int x, int y) {
        final int left = Math.min(dstArea.left, dstArea.right);
        final int right = Math.max(dstArea.left, dstArea.right);

        final int top = Math.min(dstArea.top, dstArea.bottom);
        final int bottom = Math.max(dstArea.top, dstArea.bottom);

        return x >= left && x <= right && y >= top && y <= bottom;
    }

    /**
     * スプライトと指が接触していたらタッチ座標。
     * @param input
     * @return
     */
    public TouchPoint findIntersect(MultiTouchInput input) {
        for (int i = 0; i < input.getTouchPointCount(); ++i) {
            TouchPoint touchPoint = input.getTouchPoint(i);
            if (!touchPoint.isRelease() && !touchPoint.isReleaseOnce()
                    && isIntersect(touchPoint.getCurrentX(), touchPoint.getCurrentY())) {
                return touchPoint;
            }
        }
        return null;
    }

    /**
     * スプライトと指が接触していたら!=nullを返す。
     * touchOnce / touch / releaseOnceの場合に!=nullを返す。
     * @param input
     * @return
     */
    public TouchPoint findIntersectTouchOrReleaseOnce(MultiTouchInput input) {
        for (int i = 0; i < input.getTouchPointCount(); ++i) {
            TouchPoint touchPoint = input.getTouchPoint(i);
            if ((touchPoint.isReleaseOnce() || touchPoint.isTouch())
                    && isIntersect(touchPoint.getCurrentX(), touchPoint.getCurrentY())) {
                return touchPoint;
            }
        }
        return null;
    }

    /**
     * スプライトと指が接触していたら!=nullを返す。
     * @param input
     * @return
     */
    public TouchPoint findIntersectReleaseOnce(MultiTouchInput input) {
        for (int i = 0; i < input.getTouchPointCount(); ++i) {
            TouchPoint touchPoint = input.getTouchPoint(i);
            if (touchPoint.isReleaseOnce() && isIntersect(touchPoint.getCurrentX(), touchPoint.getCurrentY())) {
                return touchPoint;
            }
        }
        return null;
    }

    /**
     * スプライトと指が接触していたら!=nullを返す。
     * @param input
     * @return
     */
    public TouchPoint isIntersectTouchOnce(MultiTouchInput input) {
        for (int i = 0; i < input.getTouchPointCount(); ++i) {
            TouchPoint touchPoint = input.getTouchPoint(i);
            if (touchPoint.isTouchOnce() && isIntersect(touchPoint.getCurrentX(), touchPoint.getCurrentY())) {
                return touchPoint;
            }
        }
        return null;
    }

    /**
     * スプライトと指が接触していたら!=nullを返す。
     * @param input
     * @return
     */
    public TouchPoint findIntersectTouch(MultiTouchInput input) {
        for (int i = 0; i < input.getTouchPointCount(); ++i) {
            TouchPoint touchPoint = input.getTouchPoint(i);
            if (touchPoint.isTouch() && isIntersect(touchPoint.getCurrentX(), touchPoint.getCurrentY())) {
                return touchPoint;
            }
        }
        return null;
    }

    /**
     * スプライトのマスタ情報を変更する。
     * 変更後は画像SRC/DSTを補正する。
     * @param master
     * @return
     */
    public Sprite changeMaster(SpriteMaster master) {
        this.master = master;
        setFrame(getFrame());
        setSpritePosition(getDstCenterX(), getDstCenterY());
        return this;
    }
}