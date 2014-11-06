package com.eaglesakura.material.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.eaglesakura.android.thread.HandlerLoopController;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.material.R;
import com.eaglesakura.math.MathUtil;

/**
 * Ripple風にくり抜きを行う
 * <p/>
 * clipが行える場合はround rectを利用し、行えない場合はrectで動作する
 */
public class RippleEffectLayout extends FrameLayout {

    static final String BUNDLE_KEY_FROM_AREA = "BUNDLE_KEY_FROM_AREA";

    /**
     *
     */
    protected RippleState rippleState;

    /**
     * 定期実行
     */
    protected HandlerLoopController loopController;


    public RippleEffectLayout(Context context) {
        super(context);
    }

    public RippleEffectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RippleEffectLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public RippleEffectLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void draw(Canvas canvas) {
        if (rippleState != null) {
            rippleState.setupCanvas(canvas);
        }
        super.draw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (rippleState != null) {
            float alpha = rippleState.getRenderWeight();
            child.setAlpha(alpha);
        } else {
            child.setAlpha(1.0f);
        }

        return super.drawChild(canvas, child, drawingTime);
    }

    /**
     * Rippleを設定する
     *
     * @param fromPosition
     */
    public void setRipple(RectF fromPosition) {
        rippleState = new RippleState();
        rippleState.fromPosition = new RectF(fromPosition);
        rippleState.durationTimeMs = getResources().getInteger(R.integer.EsMaterial_Ripple_Duration);
    }

    public void setRipple(Bundle bundle) {
        RectF fromArea = bundle.getParcelable(BUNDLE_KEY_FROM_AREA);
        if (fromArea != null) {
            setRipple(fromArea);
        }
    }

    public void setRipple(View view) {
        setRipple(new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
    }

    /**
     * エフェクトを停止させる
     */
    public void stopEffect() {
        if (loopController != null) {
            loopController.disconnect();
            loopController.dispose();
            loopController = null;
        }
    }

    /**
     * Rippleを開始する
     */
    public void startEffect() {
        stopEffect();
        rippleState.startedTime = System.currentTimeMillis();
        loopController = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                if (rippleState.getRenderWeight() >= 1.0f) {
                    stopEffect();
                }
                invalidate();

            }
        };
        loopController.setFrameRate(60);
        loopController.connect();
    }

    /**
     * エフェクトを閉じる
     */
    public void revertEffect() {
        startEffect();
        rippleState.revert = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopEffect();
        super.onDetachedFromWindow();
    }

    protected class RippleState {
        boolean revert = false;

        /**
         * 開始位置
         */
        RectF fromPosition;

        /**
         * 移行時間
         */
        int durationTimeMs = 1000 * 1;

        /**
         * 開始時刻
         */
        long startedTime;

        /**
         * 移行ウェイトを取得する
         *
         * @return
         */
        float getMoveWeight() {
            if (startedTime == 0) {
                // まだ始まっていない
                return 0;
            }

            final long currentTime = System.currentTimeMillis();
            final long offsetTime = currentTime - startedTime;
            if (offsetTime >= durationTimeMs) {
                // 指定時刻を経過している
                return 1.0f;
            }

            // 計算する
            return (float) offsetTime / (float) durationTimeMs;
        }

        float getRenderWeight() {
            if (revert) {
                return 1.0f - getMoveWeight();
            } else {
                return getMoveWeight();
            }
        }

        void setupCanvas(Canvas canvas) {
            final RectF TARGET_POS = new RectF(getLeft(), getTop(), getRight(), getBottom());
            float WEIGHT = getRenderWeight();
            float WEIGHT_INVERT = 1.0f - WEIGHT;

            // 位置を計算する
            final RectF currentPos = new RectF(
                    MathUtil.blendValue(TARGET_POS.left, fromPosition.left, WEIGHT),
                    MathUtil.blendValue(TARGET_POS.top, fromPosition.top, WEIGHT),
                    MathUtil.blendValue(TARGET_POS.right, fromPosition.right, WEIGHT),
                    MathUtil.blendValue(TARGET_POS.bottom, fromPosition.bottom, WEIGHT)
            );

            // 半径を計算する
            final float currentRadius = Math.min((currentPos.width() / 2) * WEIGHT_INVERT, (currentPos.height() / 2) * WEIGHT_INVERT);

            // パスのセットアップ
            Path path = new Path();
            path.addRoundRect(currentPos, currentRadius, currentRadius, Path.Direction.CW);
            canvas.clipPath(path);
        }
    }

    /**
     * View位置を記録する
     *
     * @param fromView
     * @param bundle
     * @return
     */
    public static Bundle saveFromView(View fromView, Bundle bundle) {
        return saveFromArea(new RectF(fromView.getLeft(), fromView.getTop(), fromView.getRight(), fromView.getBottom()), bundle);
    }

    /**
     * エリアを記録する
     *
     * @param area
     * @param bundle
     * @return
     */
    public static Bundle saveFromArea(RectF area, Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }

        bundle.putParcelable(BUNDLE_KEY_FROM_AREA, area);

        return bundle;
    }
}
