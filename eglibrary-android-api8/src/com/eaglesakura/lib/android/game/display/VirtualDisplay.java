package com.eaglesakura.lib.android.game.display;

import android.graphics.Rect;
import android.graphics.RectF;

import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.util.GameUtil;

/**
 * ディスプレイサイズに関わる補正を行う。<BR>
 * <BR>
 * 基本的に、画面はスケーリング＆センタリングされる。<BR>
 * 基本となる仮想ディスプレイサイズを指定し、そのアスペクト比を保持して実際のディスプレイサイズが決定される。<BR>
 * アスペクト比は可能な限り保持するが、端数の関係で完全一致は諦めること。<BR>
 * 実際の描画を行うための解像度は必ず偶数になる。<BR>
 * 全ての入力が偶数である場合、上下の隙間のピクセル数は必ず一致する。<BR>
 * 
 * @author Takeshi
 * 
 */
public class VirtualDisplay {
    /**
     * 実際のディスプレイサイズ
     */
    Vector2 realDisplaySize = new Vector2(720, 1280);

    /**
     * 仮想ディスプレイサイズ
     */
    Vector2 virtualDisplaySize = new Vector2(480, 800);

    /**
     * 仮想ディスプレイの実描画位置
     */
    RectF drawingArea = new RectF(0, 0, 1, 1);

    /**
     * サイズのスケーリング値。
     */
    float scaling = 1.0f;

    /**
     * 仮想ディスプレイのフィットタイプ。
     * @author Takeshi
     *
     */
    public enum FitType {
        AUTO, HORIZONTAL, VERTICAL,
    }

    /**
     * ディスプレイサイズの補正を行う。
     */
    public VirtualDisplay() {
        setRealDisplaySize(720, 1280);
        setVirtualDisplaySize(480, 800);
    }

    /**
     * 実ディスプレイサイズを設定する。
     * 
     * @param width
     * @param height
     */
    public void setRealDisplaySize(float width, float height) {
        realDisplaySize.set(width, height);
    }

    /**
     * 仮想ディスプレイのサイズを指定する。
     * 自動的に全画面が収まるようにフィットさせる。
     * @param width
     * @param height
     */
    public void setVirtualDisplaySize(float width, float height) {
        setVirtualDisplaySize(width, height, FitType.AUTO);
    }

    /**
     * 仮想ディスプレイサイズを設定する。
     * 
     * @param width
     * @param height
     * @param type
     */
    public void setVirtualDisplaySize(float width, float height, FitType type) {
        virtualDisplaySize.set(width, height);

        //! 縦横比を計算
        final float mulX = realDisplaySize.x / width;
        final float mulY = realDisplaySize.y / height;

        //! スケーリング値が小さい方にあわせて、描画先を設定する。
        if (type == FitType.AUTO) {
            scaling = Math.min(mulX, mulY);
        } else if (type == FitType.VERTICAL) {
            scaling = mulY;
        } else if (type == FitType.HORIZONTAL) {
            scaling = mulX;
        }
        drawingArea.set(0, 0, virtualDisplaySize.x * scaling, virtualDisplaySize.y * scaling);
        GameUtil.round(drawingArea);

        //! ジャストフィット用の補正を行う。
        if (scaling == mulX && drawingArea.width() != realDisplaySize.x) {
            drawingArea.right = realDisplaySize.x;
        }
        if (scaling == mulY && drawingArea.height() != realDisplaySize.y) {
            drawingArea.bottom = realDisplaySize.y;
        }

        //! 描画位置をセンタリングする
        int offsetX = (int) (realDisplaySize.x - drawingArea.width());
        int offsetY = (int) (realDisplaySize.y - drawingArea.height());
        drawingArea.offsetTo(offsetX / 2, offsetY / 2);
        GameUtil.round(drawingArea);

        //! 幅が偶数じゃない場合、丸めを行う。
        if (((int) drawingArea.width()) % 2 != 0) {
            drawingArea.right += 1;
        }
        //! 高さが偶数じゃない場合、丸めを行う。
        if (((int) drawingArea.height()) % 2 != 0) {
            drawingArea.bottom += 1;
        }
    }

    /**
     * 実際の描画先の座標を取得する。
     * 
     * @param result
     * @return
     */
    public RectF getDrawingArea(RectF result) {
        result.set(drawingArea);
        return result;
    }

    /**
     * 実際の描画先座標を取得する。
     * 
     * @param result
     * @return
     */
    public Rect getDrawingArea(Rect result) {
        result.set((int) drawingArea.left, (int) drawingArea.top, (int) drawingArea.right, (int) drawingArea.bottom);
        return result;
    }

    public int getDrawingAreaWidth() {
        return (int) drawingArea.width();
    }

    public int getDrawingAreaHeight() {
        return (int) drawingArea.height();
    }

    /**
     * 実際の物理的なディスプレイサイズを取得する。
     * 
     * @param result
     * @return
     */
    public Vector2 getRealDisplaySize(Vector2 result) {
        result.set(realDisplaySize);
        return result;
    }

    /**
     * 仮想ディスプレイサイズを取得する。
     * 
     * @param result
     * @return
     */
    public Vector2 getVirtualDisplaySize(Vector2 result) {
        result.set(virtualDisplaySize);
        return result;
    }

    /**
     * 仮想ディスプレイに対して、実ディスプレイが何倍あるかのスケーリング値を取得する。<BR>
     * 仮想ディスプレイのほうが小さい場合、 > 1.0f<BR>
     * 実ディスプレイのほうが小さい場合、 < 1.0f<BR>
     * となる。
     * 
     * @return
     */
    public float getDeviceScaling() {
        return scaling;
    }

    /**
     * 仮想ディスプレイの横サイズの中央を取得する。
     * @return
     */
    public int getVirtualDisplayCenterX() {
        return (int) virtualDisplaySize.x / 2;
    }

    /**
     * 仮想ディスプレイの縦サイズの中央を取得する。
     * @return
     */
    public int getVirtualDisplayCenterY() {
        return (int) virtualDisplaySize.y / 2;
    }

    /**
     * 仮想ディスプレイの横サイズを取得する。
     * @return
     */
    public int getVirtualDisplayWidth() {
        return (int) virtualDisplaySize.x;
    }

    /**
     * 仮想ディスプレイの縦サイズを取得する
     * @return
     */
    public int getVirtualDisplayHeight() {
        return (int) virtualDisplaySize.y;
    }

    /**
     * 指定した座標が画面外だったらtrueを返す。
     * 
     * @param realPos
     * @return
     */
    public boolean isOutsideReal(Vector2 realPos) {
        return drawingArea.contains(realPos.x, realPos.y);
    }

    /**
     * 指定した座標が画面外だったらtrueを返す。
     * 
     * @param area
     * @return
     */
    public boolean isOutsideReal(int left, int top, int width, int height) {
        return !drawingArea.contains(left, top, left + width, top + height);
    }

    /**
     * 仮想ディスプレイの外だったらtrueを返す
     * 
     * @param area
     * @return
     */
    public boolean isOutsideVirtual(int left, int top, int width, int height) {
        final int right = (left + width);
        final int bottom = (top + height);

        //! 右側が0を下回っている、左側が画面サイズよりも大きい、下が画面から見切れている、上が画面から見切れている、いずれかがヒット
        return right < 0 || left > (int) virtualDisplaySize.x || bottom < 0 || top > (int) virtualDisplaySize.y;
    }

    /**
     * 実際のピクセル位置を仮想ディスプレイ位置に変換する。
     * 
     * @param realPos
     *            ピクセル単位の位置
     * @param result
     *            結果を格納する参照。realPosと同じオブジェクトで問題ない。
     * @return
     */
    public Vector2 projectionPixelPosition(Vector2 realPos, Vector2 result) {
        result.set((realPos.x - drawingArea.left) / scaling, (realPos.y - drawingArea.top) / scaling);
        return result;
    }

    /**
     * 実際のピクセル位置を正規化座標系に変換する。 GL正規化座標系のため、下側が0.0、上側が1.0となる。
     * 
     * @param realPos
     *            ピクセル単位の位置
     * @param result
     *            結果を格納する参照。realPosと同じオブジェクトで問題ない。
     * @return
     */
    public Vector2 projectionNormalizePosition(Vector2 realPos, Vector2 result) {
        projectionPixelPosition(realPos, result);
        result.x /= virtualDisplaySize.x;
        result.y /= virtualDisplaySize.y;

        result.y = 1.0f - result.y;
        return result;
    }

    /**
     * 実際のピクセル位置を正規化座標系に変換する。 
     * この値は、仮想ディスプレイに対するUVとして動作する。
     * 
     * @param realPos ピクセル単位の位置
     * @param result 結果を格納する参照。realPosと同じオブジェクトで問題ない。
     * @return
     */
    public Vector2 projectionNormalizePosition2D(Vector2 realPos, Vector2 result) {
        projectionPixelPosition(realPos, result);
        result.x /= virtualDisplaySize.x;
        result.y /= virtualDisplaySize.y;
        return result;
    }
}
