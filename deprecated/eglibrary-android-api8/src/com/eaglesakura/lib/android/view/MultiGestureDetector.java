package com.eaglesakura.lib.android.view;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

import com.eaglesakura.lib.android.game.thread.UIHandler;

public class MultiGestureDetector implements android.view.GestureDetector.OnGestureListener, OnScaleGestureListener {

    GestureDetector gesture = null;
    ScaleGestureDetector scaleGesture = null;
    OnMultiGestureListener listener = null;
    boolean scaling = false;
    Handler handler = new UIHandler();

    /**
     * スケールと通常のジェスチャの両方を受け取る。
     * @author TAKESHI YAMASHITA
     *
     */
    public interface OnMultiGestureListener extends android.view.GestureDetector.OnGestureListener,
            OnScaleGestureListener {

        /**
         * タッチが終わった時に追加で呼び出される。
         * @param me
         */
        void onTouchEnd(MotionEvent me, boolean scalingNow);
    }

    /**
     * 
     * @param context
     * @param listener
     */
    public MultiGestureDetector(Context context, OnMultiGestureListener listener) {
        setListener(listener);
        this.gesture = new GestureDetector(this);
        this.scaleGesture = new ScaleGestureDetector(context, this);
    }

    /**
     * ロングタップの有効・無効を設定する。
     * @param set
     */
    public void setIsLongpressEnabled(boolean set) {
        gesture.setIsLongpressEnabled(set);
    }

    /**
     * タッチイベントを中継する。
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        gesture.onTouchEvent(event);
        scaleGesture.onTouchEvent(event);

        final int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_OUTSIDE) {
            listener.onTouchEnd(event, scaling);
        }
        return true;
    }

    /**
     * リスナを更新する。
     * @param listener
     */
    public void setListener(OnMultiGestureListener listener) {
        this.listener = listener;
        if (listener == null) {
            this.listener = stubListener;
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (scaling) {
            return false;
        }

        return listener.onDown(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (scaling) {
            return false;
        }
        return listener.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public void onShowPress(MotionEvent e) {
        if (scaling) {
            return;
        }
        listener.onShowPress(e);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (scaling) {
            return false;
        }
        return listener.onSingleTapUp(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (scaling) {
            return false;
        }
        return listener.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (scaling) {
            return;
        }
        listener.onLongPress(e);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        boolean result = listener.onScaleBegin(detector);
        if (result) {
            scaling = true;
        }
        return result;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaling = true;
        return listener.onScale(detector);
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scaling = false;
            }
        }, 1000 / 4);
        listener.onScaleEnd(detector);
    }

    static OnMultiGestureListener stubListener = new OnMultiGestureListener() {

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onTouchEnd(MotionEvent me, boolean scaling) {

        }
    };
}
