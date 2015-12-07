package com.eaglesakura.material.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.eaglesakura.android.thread.HandlerLoopController;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.R;
import com.eaglesakura.math.MathUtil;
import com.eaglesakura.util.LogUtil;

/**
 * Ripple風にくり抜きを行う
 * <br>
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

    /**
     * Round効果を行う場合はtrue
     */
    boolean enableRound = true;


    public RippleEffectLayout(Context context) {
        super(context);
        initializeEffectLayer(context, null, 0, 0);
    }

    public RippleEffectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeEffectLayer(context, attrs, 0, 0);
    }

    public RippleEffectLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeEffectLayer(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("NewApi")
    public RippleEffectLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeEffectLayer(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("NewApi")
    protected void initializeEffectLayer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            LogUtil.log("not supported LAYER_TYPE_HARDWARE clip");
            // not support hardware clip
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }


    @Override
    public void draw(Canvas canvas) {
        if (rippleState != null) {

            rippleState.setupCanvas(canvas);
        }
        super.draw(canvas);
    }

    @SuppressLint("NewApi")
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (rippleState != null) {
            float alpha = rippleState.getRenderWeight();
            alpha *= 2;
            if (alpha < 1) {
                child.setAlpha(0);
            } else {
                child.setAlpha(alpha - 1.0f);
            }
        } else {
            child.setAlpha(1.0f);
        }

        return super.drawChild(canvas, child, drawingTime);
    }

    /**
     * ラウンド効果を有効にする場合はtrue
     *
     * @param enableRound
     */
    public void setRoundEnable(boolean enableRound) {
        this.enableRound = enableRound;
    }

    public void cancelRipple() {
        stopEffect();
        rippleState = null;
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
        int[] displaySize = ContextUtil.getDisplaySize(getContext());
        RectF fromArea = null;
        if (bundle != null) {
            fromArea = bundle.getParcelable(BUNDLE_KEY_FROM_AREA);
        }
        if (fromArea == null) {
            fromArea = new RectF(0, 0, displaySize[0] / 2, displaySize[1] / 2);
            fromArea.offsetTo(displaySize[0] / 4, displaySize[1] / 4);
        }
        setRipple(fromArea);
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
        if (rippleState == null) {
            return;
        }

        stopEffect();
        rippleState.startedTime = System.currentTimeMillis();
        loopController = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                if (rippleState.getRenderWeight() >= 1.0f) {
                    stopEffect();
                }
                invalidate();
//                LogUtil.log("invalidate");
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

        /**
         * clip用のpathを生成する
         *
         * @return
         */
        Path createPath() {
            final RectF TARGET_POS = new RectF(getLeft(), getTop(), getRight(), getBottom());
            float WEIGHT = Math.min(1.0f, getRenderWeight() * 2);
            float WEIGHT_INVERT = 1.0f - WEIGHT;

            // 位置を計算する
            final RectF currentPos = new RectF(
                    MathUtil.blendValue(TARGET_POS.left, fromPosition.left, WEIGHT),
                    MathUtil.blendValue(TARGET_POS.top, fromPosition.top, WEIGHT),
                    MathUtil.blendValue(TARGET_POS.right, fromPosition.right, WEIGHT),
                    MathUtil.blendValue(TARGET_POS.bottom, fromPosition.bottom, WEIGHT)
            );

            // 半径を計算する
            float currentRadius = Math.min((currentPos.width() / 2) * WEIGHT_INVERT, (currentPos.height() / 2) * WEIGHT_INVERT);
            if (!enableRound) {
                // ちょっとだけroundさせることはやる
                currentRadius = Math.min(currentRadius, Math.max(getWidth(), getHeight()) / 200);
            }

            // パスのセットアップ
            Path path = new Path();
            path.addRoundRect(currentPos, currentRadius, currentRadius, Path.Direction.CW);
            return path;
        }

        void setupCanvas(Canvas canvas) {
            Path path = createPath();
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
        Rect position = new Rect();
        fromView.getGlobalVisibleRect(position);
        return saveFromArea(new RectF(position), bundle);
//        Rect area = new Rect();
//        fromView.getGlobalVisibleRect(area);
//        return saveFromArea(new RectF(area), bundle);
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

    /**
     * アニメーションを指定する
     *
     * @param transaction
     */
    public static void setRippleTransaction(FragmentTransaction transaction) {
        transaction.setCustomAnimations(
                R.anim.fragment_ripple_upper_enter,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_ripple_upper_exit
        );
    }

    /**
     * Rippleエフェクトを開始する
     * <br>
     * このメソッドはTransaction.replaceを行う前に呼び出さなければならない
     *
     * @param transaction
     * @param fromView
     * @param targetFragment
     */
    public static void startRippleTransaction(FragmentTransaction transaction, View fromView, Fragment targetFragment) {
        setRippleTransaction(transaction);
        if (fromView != null) {
            targetFragment.setArguments(saveFromView(fromView, targetFragment.getArguments()));
        }
    }
}
