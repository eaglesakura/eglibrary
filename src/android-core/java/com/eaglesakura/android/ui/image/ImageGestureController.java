package com.eaglesakura.android.ui.image;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.eaglesakura.android.graphics.ImageCorrector;
import com.eaglesakura.android.thread.loop.HandlerLoopController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.util.MathUtil;
import com.eaglesakura.math.Vector2;
import com.eaglesakura.util.Timer;
import com.eaglesakura.util.LogUtil;

/**
 * 一枚絵を基本として、下記の機能を扱うベースを対応する。
 * <br>
 * これは操作系等を統一しているのみで、レンダリングは無関係となる。
 */
public class ImageGestureController implements View.OnTouchListener {
    /**
     * スケーリングの入力中だったらtrue
     */
    private boolean scaleInput = false;

    private boolean touchNow = false;

    private final ImageCorrector imageCorrector = new ImageCorrector();

    private final Context context;

    private GestureDetector touchDetector;

    private ScaleGestureDetector scaleGestureDetector;

    private Handler handler = UIHandler.getInstance();

    private HandlerLoopController loopController;

    /**
     * 浮動小数計算で認める丸め誤差
     */
    private final float FLOAT_CALC_ROUND = 0.0000001f;

    private float maxScaling = 3.0f;

    private float scaleEffectUpBound = 1.15f;

    Vector2 scaleCenter = new Vector2();


    /**
     * 画像移動のベクトル
     */
    Vector2 imageMoveVector = new Vector2();

    ImageGestureListener listener;

    /**
     * 移動速度
     */
    Vector2 lookAtSpeed;

    int lookAtFrames;

    Timer freeTimer = new Timer();


    public ImageGestureController(Context context) {
        this.context = context.getApplicationContext();
    }

    public ImageCorrector getImageCorrector() {
        return imageCorrector;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void initialize() {
        touchDetector = new GestureDetector(context, singleTapListener);
        touchDetector.setOnDoubleTapListener(doubleTapListener);

        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
    }

    /**
     * エフェクト実行が必要であればtrue
     *
     * @return
     */
    private boolean hasEffectUpdateRequest() {
        if (imageMoveVector.length() > 0.0001) {
            return true;
        }

        // 等倍よりも小さかったら拡大処理を行う
        if (imageCorrector.getPixelScale() < 1.0 || imageCorrector.getPixelScale() > maxScaling) {

            return true;
        }

        return false;
    }

    /**
     * 移動を行う
     */
    public void updateEffect() {

        if (!scaleInput) {
            // スケーリング入力していなかったら
            if (imageCorrector.getPixelScale() < 1.0f) {
                imageCorrector.scale(scaleCenter.x, scaleCenter.y, scaleEffectUpBound);
                // スケーリングがオーバーしたら1.0に戻す
                if (imageCorrector.getPixelScale() > 1.0f) {
                    imageCorrector.resetScale();
                }

            }

            // 誤差が一定以内だったらフィットさせる
            if (Math.abs(1.0f - imageCorrector.getPixelScale()) < FLOAT_CALC_ROUND) {
                imageCorrector.scale(1.0f / imageCorrector.getPixelScale());
            }
        }


        // 位置補正
        if (!touchNow && imageCorrector.getPixelScale() >= 1.0f) {
            Vector2 lookAtSpeed = this.lookAtSpeed;

            if (lookAtSpeed != null && lookAtFrames-- > 0) {
                imageCorrector.offset(lookAtSpeed.x, lookAtSpeed.y);
            } else {
                final float CENTER_X = imageCorrector.getRenderAreaCenterX();
                final float CENTER_Y = imageCorrector.getRenderAreaCenterY();
                float boundMoveSpeed = imageCorrector.getRenderAreaWidth() / 15;

                if (imageCorrector.getImageAreaLeft() > CENTER_X) {
                    imageCorrector.moveToTargetLeft(CENTER_X, boundMoveSpeed);
                }
                if (imageCorrector.getImageAreaRight() < CENTER_X) {
                    imageCorrector.moveToTargetRight(CENTER_X, boundMoveSpeed);
                }

                if (imageCorrector.getImageAreaTop() > CENTER_Y) {
                    imageCorrector.moveToTargetTop(CENTER_X, boundMoveSpeed);
                }
                if (imageCorrector.getImageAreaBottom() < CENTER_Y) {
                    imageCorrector.moveToTargetBottom(CENTER_X, boundMoveSpeed);
                }
            }
        }
    }

    public void clearLookAt() {
        lookAtSpeed = null;
        lookAtFrames = 0;
    }

    /**
     * 注目点を指定する
     *
     * @param newCenterRenderPosX
     * @param newCenterRenderPosY
     * @param frame
     */
    public void setLookAtRenderPos(int newCenterRenderPosX, int newCenterRenderPosY, int frame) {
        Rect area = imageCorrector.getRenderArea(new Rect());
        int currentCenterX = area.centerX();
        int currentCenterY = area.centerY();

        float diffX = newCenterRenderPosX - currentCenterX;
        float diffY = newCenterRenderPosY - currentCenterY;

        lookAtSpeed = new Vector2(-diffX / (float) frame, -diffY / (float) frame);
        lookAtFrames = frame;
    }

    public void setListener(ImageGestureListener listener) {
        this.listener = listener;
    }

    /**
     * タッチを放置している時間を取得する。
     * <br>
     * タッチ中の場合は負の値を返却する
     *
     * @return
     */
    public long getFreeTimeMs() {
        if (touchNow) {
            return -1;
        }
        return freeTimer.end();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        freeTimer.start();
        if (touchDetector == null || scaleGestureDetector == null) {
            return true;
        }
        clearLookAt();
        touchDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            scaleInput = false;
            touchNow = false;
        } else {
            touchNow = true;
        }
        return true;
    }

    private final GestureDetector.OnGestureListener singleTapListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (listener != null) {
                return listener.onClick(ImageGestureController.this, e);
            } else {
                return false;
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (scaleInput) {
                return false;
            }

            imageCorrector.offset(-distanceX, -distanceY);

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (scaleInput) {
                return false;
            }

            LogUtil.log("onFling(%f, %f)", velocityX, velocityY);
//            imageMoveVector.set(velocityX, velocityY);
            return true;
        }
    };

    private final GestureDetector.OnDoubleTapListener doubleTapListener = new GestureDetector.OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (listener != null) {
                return listener.onDoubleClick(ImageGestureController.this, e);
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    };

    private final ScaleGestureDetector.OnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scaleInput = true;
            scaleCenter.set(detector.getFocusX(), detector.getFocusY());
            return true;
        }


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor();
            factor = MathUtil.minmax(0.8f, 1.5f, factor);   // 一瞬で多大な影響を受けないように範囲制限
            imageCorrector.scale(scaleCenter.x, scaleCenter.y, factor);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            scaleInput = false;

            if (listener != null) {
                listener.onScaled(ImageGestureController.this, scaleCenter);
            }
        }
    };

    public interface ImageGestureListener {
        /**
         * 画像をクリックした
         *
         * @param gesture
         * @param event
         * @return 反応したらtrue
         */
        boolean onClick(ImageGestureController gesture, MotionEvent event);

        /**
         * 画像をダブルクリックした
         *
         * @param gesture
         * @param event
         * @return 反応したらtrue
         */
        boolean onDoubleClick(ImageGestureController gesture, MotionEvent event);

        /**
         * スケーリングを変更した
         *
         * @param gesture
         * @param center
         * @return 反応したらtrue
         */
        boolean onScaled(ImageGestureController gesture, Vector2 center);
    }
}
