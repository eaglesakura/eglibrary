package com.eaglesakura.lib.android.game.graphics;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;

public class ImageCorrector {

    /**
     * レンダリング可能エリア
     */
    RectF renderArea = new RectF(0, 0, 1, 1);

    /**
     * 画像レンダリングを行うエリア
     */
    RectF imageArea = new RectF(0, 0, 1, 1);

    /**
     * width / heightの値
     */
    float aspect = 1.0f;

    /**
     * デフォルトのピクセル数を取得する。
     * このピクセル数と現在のピクセル数で倍率を確定する。
     */
    float defPixels = 1.0f;

    /**
     * 現在のフィットタイプ。
     */
    FitType fitting = FitType.LONG;

    public enum FitType {
        /**
         * 長辺がフィット（画面全体に収まる）ようにする。
         */
        LONG,
    }

    /**
     * レンダリング範囲を設定する。
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setRenderArea(int x, int y, int width, int height) {
        renderArea.set(x, y, x + width, y + height);
    }

    /**
     * レンダリング範囲を設定する。
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setRenderArea(float x, float y, float width, float height) {
        renderArea.set(x, y, x + width, y + height);
    }

    /**
     * レンダリング範囲を設定する。
     * @param display
     */
    public void setRenderArea(VirtualDisplay display) {
        display.getDrawingArea(renderArea);
    }

    /**
     * 画像の幅・高さから画像のアスペクトを設定する。
     * @param width
     * @param height
     */
    public void setImageAspect(int width, int height) {
        aspect = (float) width / (float) height;
        setDefaultFitting(FitType.LONG);
    }

    /**
     * 画像のアスペクト比を取得する。
     * @return
     */
    public float getImageAspect() {
        return aspect;
    }

    /**
     * フィッティングを指定して初期化する。
     * @param fitting
     */
    public void setDefaultFitting(FitType fitting) {
        this.fitting = fitting;
        getDefaultRenderArea(fitting, imageArea);
        defPixels = imageArea.width() * imageArea.height();
    }

    /**
     * レンダリングエリアを初期化する。
     */
    public void reset() {
        setDefaultFitting(fitting);
    }

    /**
     * X方向に長い画像
     * @return
     */
    public boolean isXLongImage() {
        return aspect > 1;
    }

    /**
     * Y方向に長い画像
     * @return
     */
    public boolean isYLongImage() {
        return aspect <= 1;
    }

    /**
     * X方向に長いエリア
     * @return
     */
    public boolean isXLongArea() {
        return renderArea.width() > renderArea.height();
    }

    /**
     * Y方向に長いエリア
     * @return
     */
    public boolean isYLongArea() {
        return renderArea.height() >= renderArea.width();
    }

    public float getRenderAreaWidth() {
        return renderArea.width();
    }

    public float getRenderAreaHeight() {
        return renderArea.height();
    }

    /**
     * デフォルトのレンダリングエリアを取得する。
     * @param fitting
     * @param result
     * @return
     */
    public RectF getDefaultRenderArea(FitType fitting, RectF result) {
        float width = 0;
        float height = 0;
        switch (fitting) {
            case LONG:
                if (isYLongArea()) {
                    width = renderArea.width();
                    height = width / aspect;

                    final float mul = height / renderArea.height();
                    if (mul > 1) {
                        width /= mul;
                        height /= mul;
                    }

                } else {
                    height = renderArea.height();
                    width = height * aspect;

                    final float mul = width / renderArea.width();
                    if (mul > 1) {
                        width /= mul;
                        height /= mul;
                    }

                }
                break;
            default:
                break;
        }

        result.set(0, 0, width, height);
        result.offset(renderArea.centerX() - result.centerX(), renderArea.centerY() - result.centerY());
        return result;
    }

    /**
     * 画像エリアを取得する。
     * @param result
     * @return
     */
    public RectF getImageArea(RectF result) {
        result.set(imageArea);

        return result;
    }

    public float getRenderAreaLeft() {
        return renderArea.left;
    }

    public float getRenderAreaTop() {
        return renderArea.top;
    }

    public float getRenderAreaRight() {
        return renderArea.right;
    }

    public float getRenderAreaBottom() {
        return renderArea.bottom;
    }

    public float getRenderAreaCenterX() {
        return renderArea.centerX();
    }

    public float getRenderAreaCenterY() {
        return renderArea.centerY();
    }

    public float getImageAreaLeft() {
        return imageArea.left;
    }

    public float getImageAreaTop() {
        return imageArea.top;
    }

    public float getImageAreaRight() {
        return imageArea.right;
    }

    public float getImageAreaBottom() {
        return imageArea.bottom;
    }

    /**
     * 画像エリアを取得する。
     * @param result
     * @return
     */
    public Rect getImageArea(Rect result) {
        result.set((int) imageArea.left, (int) imageArea.top, (int) imageArea.right, (int) imageArea.bottom);
        return result;
    }

    /**
     * レンダリング領域を取得する。
     * @param result
     * @return
     */
    public RectF getRenderArea(RectF result) {
        result.set(renderArea);
        return result;
    }

    /**
     * スクロールを行う。
     * @param x
     * @param y
     */
    public void offsetImageArea(float x, float y) {
        imageArea.offset(x, y);
    }

    /**
     * ピクセル座標からU座標に変換する。
     * @param x
     * @return
     */
    public float pixToImageU(float x) {
        final float offset = x - imageArea.left;
        return offset / imageArea.width();
    }

    public float uToImagePix(float u) {
        u *= imageArea.width();
        u += imageArea.left;

        return u;
    }

    /**
     * ピクセル座標からV座標に変換する。
     * @param y
     * @return
     */
    public float pixToImageV(float y) {
        final float offset = y - imageArea.top;
        return offset / imageArea.height();
    }

    public float vToImagePix(float v) {
        v *= imageArea.height();
        v += imageArea.top;

        return v;
    }

    /**
     * x/y地点を中心としてスケーリングを行う。
     * @param x
     * @param y
     * @param scale
     */
    public void scale(float x, float y, float scale) {
        Matrix m = new Matrix();
        m.setScale(scale, scale, x, y);
        m.mapRect(imageArea);
        float height = imageArea.height();

        float fixedWidth = height * aspect;

        // 計算誤差補正を行う。
        imageArea.right = imageArea.left + fixedWidth;
    }

    /**
     * 画像エリア地点を中心としてスケーリングを行う。
     * @param x
     * @param y
     * @param scale
     */
    public void scale(float scale) {
        Matrix m = new Matrix();
        m.setScale(scale, scale, renderArea.centerX(), renderArea.centerY());
        m.mapRect(imageArea);
        float height = imageArea.height();

        float fixedWidth = height * aspect;

        // 計算誤差補正を行う。
        imageArea.right = imageArea.left + fixedWidth;
    }

    /**
     * イメージエリアの位置をオフセットさせる。
     * @param x
     * @param y
     */
    public void offset(float x, float y) {
        imageArea.offset(x, y);
    }

    /**
     * レンダリングエリアとジェスチャエリアの中心が同じならtrueを返す。
     * @return
     */
    public boolean isCenterFitting() {
        return imageArea.centerX() == renderArea.centerX() && imageArea.centerY() == renderArea.centerY();
    }

    /**
     * ピクセル数ベースで拡大率を取得する。
     * @return
     */
    public float getPixelScale() {
        return (imageArea.width() * imageArea.height()) / defPixels;
    }

    /**
     * レンダリングエリアからイメージエリアが飛び出していたら、飛び出さないようにする。
     * レンダリングエリアよりイメージエリアのほうが狭かったら、中心寄せに補正する。
     */
    public void correctImageArea() {
        correctImageArea(true, true);
    }

    /**
     * レンダリングエリアからイメージエリアが飛び出していたら、飛び出さないようにする。
     * レンダリングエリアよりイメージエリアのほうが狭かったら、中心寄せに補正する。
     */
    public void correctImageArea(boolean xCrrect, boolean yCorrect) {

        if (xCrrect) {
            if (imageArea.width() < renderArea.width()) {
                imageArea.offset(renderArea.centerX() - imageArea.centerX(), 0);
            } else {
                if (imageArea.left > renderArea.left) {
                    imageArea.offset(renderArea.left - imageArea.left, 0);
                } else if (imageArea.right < renderArea.right) {
                    imageArea.offset(renderArea.right - imageArea.right, 0);
                }
            }
        }

        if (yCorrect) {
            if (imageArea.height() < renderArea.height()) {
                imageArea.offset(0, renderArea.centerY() - imageArea.centerY());
            } else {
                if (imageArea.top > renderArea.top) {
                    imageArea.offset(0, renderArea.top - imageArea.top);
                } else if (imageArea.bottom < renderArea.bottom) {
                    imageArea.offset(0, renderArea.bottom - imageArea.bottom);
                }
            }
        }
    }
}
