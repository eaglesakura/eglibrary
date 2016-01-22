package com.eaglesakura.lib.android.game.graphics;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * スプライト表示用の1オブジェクトを示す。<BR>
 * スプライトはアニメーションを含み、1画像シートから複数の画像ブロックを切り出してアニメーション化することができる。
 *
 * @author TAKESHI YAMASHITA
 */
public class SpriteMaster {
    ImageBase image;

    List<AnimationFrame> frames = new ArrayList<AnimationFrame>();

    /**
     * 描画角度
     */
    float rotateDegree = 0;
    /**
     * フレームが終端まで来た場合に戻るコマ数
     */
    int endOffset = 0;

    /**
     * 1コマでのフレーム数
     */
    int komaFrame = 1;

    /**
     * 1フレームでの増量。
     */
    int frameOffset = 1;

    public SpriteMaster(ImageBase origin) {
        this.image = origin;
    }

    /**
     * フレームを追加する。
     */
    void addAnimationFrame(final AnimationFrame frame) {
        frames.add(frame);
    }

    /**
     * フレームを追加する。
     */
    public void addAnimationFrame(final Rect area) {
        AnimationFrame frame = new AnimationFrame();
        frame.area = area;
        addAnimationFrame(frame);
    }

    /**
     * 1ブロックの画像の大きさとインデックスを指定して画像位置を決定する。<BR>
     * 画像は必ず横方向に並んでいるものとする。
     *
     * @param blockWidth  ブロック幅
     * @param blockHeight ブロック高
     * @param index       ブロック番号
     */
    public void addAnimationFrame(final int blockWidth, final int blockHeight, final int index) {
        Rect src = new Rect();

        //! 並べられるシートの数を数える
        final int x = (image.getWidth() / blockWidth);

        //! 画像シートの位置を求める
        final int px = (index % x);
        final int py = (index / x);

        src.left = px * blockWidth;
        src.top = py * blockHeight;
        src.right = (src.left + blockWidth);
        src.bottom = (src.top + blockHeight);
        addAnimationFrame(src);
    }

    /**
     * 複数の画像ブロックを一括に登録する。
     */
    public void addAnimationFrames(final int blockWidth, final int blockHeight, final int startIndex, final int num) {
        for (int i = 0; i < num; ++i) {
            addAnimationFrame(blockWidth, blockHeight, (startIndex + i));
        }
    }

    /**
     * アニメーションを行わない。
     */
    void noAnimation() {
        frames.clear();
        if (image != null) {
            addAnimationFrame(new Rect(0, 0, image.getWidth(), image.getHeight()));
        } else {
            addAnimationFrame(new Rect(0, 0, 1, 1));
        }
    }

    /**
     * 常にアニメーションをループする。<BR>
     * 適当なマイナス補正を入れれば問題ない。
     */
    public static final int ANIMATION_LOOPING = -1000;

    /**
     * アニメーションを停止する。
     */
    public static final int ANIMATION_STOP = 0;

    /**
     * 終端に来た場合のループコマ数を指定する。 <BR>
     *
     * @see #ANIMATION_LOOPING
     * @see #ANIMATION_STOP
     */
    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    /**
     * 新規のスプライトコピーを作成する。
     */
    public Sprite newSprite() {
        return new Sprite(this);
    }

    /**
     * 一コマの表示フレーム数を設定する。
     */
    public void setKomaFrame(int komaFrame) {
        komaFrame = Math.max(1, komaFrame);
        this.komaFrame = komaFrame;
    }

    /**
     * 1フレームを示す。
     */
    static class AnimationFrame {
        public Rect area = null;
    }

    /**
     * 元画像を取得する
     */
    public ImageBase getImage() {
        return image;
    }
}
