package com.eaglesakura.lib.android.game.input;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.math.Vector2;
import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * マルチタッチを処理する。<BR>
 * 毎フレームupdate()を呼ぶ必要がある。<BR>
 * 座標は仮想ディスプレイ位置に投影されるため、 {@link VirtualDisplay}が必要になる。
 * 
 * @author Takeshi
 * 
 */
public class MultiTouchInput {
    protected TouchPoint[] touchPoints;

    /**
     * ディスプレイサイズ補正
     */
    VirtualDisplay virtualDisplay;

    /**
     * ピンチ認識時、最低限動かしている必要があるドラッグの長さ。
     */
    float pinchLength = 40.0f;

    /**
     * 属性情報。
     */
    int attrNow = 0;

    /**
     * 前フレームの属性情報。
     */
    int attrBefore = 0;

    /**
     * ピンチインが行われている。
     */
    static final int eAttrPichIn = 0x1 << 0;

    /**
     * ピンチアウトが行われている。
     */
    static final int eAttrPinchOut = 0x1 << 1;

    /**
     * ２箇所のタッチ管理を行う。
     * 
     * @author eagle.sakura
     * @version 2010/07/20 : 新規作成
     */
    public MultiTouchInput(VirtualDisplay display) {
        this(2, display);
    }

    /**
     * 
     * @author eagle.sakura
     * @param nums
     * @version 2010/07/20 : 新規作成
     */
    protected MultiTouchInput(int nums, VirtualDisplay disp) {
        touchPoints = new TouchPoint[nums];
        for (int i = 0; i < nums; ++i) {
            touchPoints[i] = new TouchPoint(i);
        }
        this.virtualDisplay = disp;
    }

    /**
     * タッチ箇所を取得する。
     * 
     * @author eagle.sakura
     * @param index
     * @return
     * @version 2010/07/20 : 新規作成
     */
    public TouchPoint getTouchPoint(int index) {
        return touchPoints[index];
    }

    /**
     * 管理しているタッチ座標の数を取得する。
     * @return
     */
    public int getTouchPointCount() {
        return touchPoints.length;
    }

    /**
     * タッチイベントを管理する。
     * 
     * @author eagle.sakura
     * @param me
     * @return
     * @version 2010/07/20 : 新規作成
     */
    public boolean onTouchEvent(MotionEvent me) {
        try {
            int index = (me.getAction() & 0xff00) >> 8;
            int id = (me.getPointerId(index));
            int action = (me.getAction() & 0xff);

            if (index >= 2) {
                return true;
            }

            Vector2 correctTouchPoint = virtualDisplay.projectionPixelPosition(new Vector2(me.getX(), me.getY()),
                    new Vector2());

            TouchPoint tp0 = touchPoints[id];
            switch (me.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return tp0.onActionDown(correctTouchPoint.x, correctTouchPoint.y);
                case MotionEvent.ACTION_UP:
                    return tp0.onActionUp(correctTouchPoint.x, correctTouchPoint.y);
                case MotionEvent.ACTION_MOVE: {
                    for (int i = 0; i < me.getPointerCount(); ++i) {
                        int _id = me.getPointerId(i);
                        TouchPoint point = getTouchPoint(_id);
                        correctTouchPoint = virtualDisplay.projectionPixelPosition(new Vector2(me.getX(i), me.getY(i)),
                                correctTouchPoint);
                        point.onActionMove(correctTouchPoint.x, correctTouchPoint.y);
                    }
                    return true;
                }
            }

            TouchPoint tp = touchPoints[id];
            correctTouchPoint = virtualDisplay.projectionPixelPosition(new Vector2(me.getX(index), me.getY(index)),
                    correctTouchPoint);

            switch (action) {
                case MotionEvent.ACTION_POINTER_UP:
                    return tp.onActionUp(correctTouchPoint.x, correctTouchPoint.y);
                case MotionEvent.ACTION_POINTER_DOWN:
                    return tp.onActionDown(correctTouchPoint.x, correctTouchPoint.y);
            }

        } catch (Exception e) {
            LogUtil.log(e);
        }
        return true;
    }

    /**
     * ドラッグされた距離を取得する。
     * 
     * @author eagle.sakura
     * @return
     * @version 2009/11/19 : 新規作成
     */
    public int getDrugVectorX() {
        return touchPoints[0].getDrugVectorX();
    }

    /**
     * 画面に触れた位置を取得する。
     * 
     * @return
     */
    public int getBeginTouchPosX() {
        return touchPoints[0].getTouchPosX();
    }

    /**
     * 画面に触れた位置を取得する。
     * 
     * @return
     */
    public int getBeginTouchPosY() {
        return touchPoints[0].getTouchPosY();
    }

    /**
     * 現在のタッチ位置を取得する。
     * 
     * @return
     */
    public int getCurrentTouchPosX() {
        return touchPoints[0].getCurrentX();
    }

    /**
     * 現在のタッチ位置を取得する。
     * 
     * @return
     */
    public int getCurrentTouchPosY() {
        return touchPoints[0].getCurrentY();
    }

    /**
     * ドラッグされた距離を取得する。
     * 
     * @return
     */
    public int getDrugVectorY() {
        return touchPoints[0].getDrugVectorY();
    }

    /**
     * タッチされているかを調べる。
     * 
     * @return
     */
    public boolean isTouch() {
        return touchPoints[0].isTouch();
    }

    /**
     * ディスプレイから指が離れているか。
     * 
     * @author eagle.sakura
     * @return
     */
    public boolean isRelease() {
        return touchPoints[0].isRelease();
    }

    /**
     * ディスプレイから指が離れた瞬間か。
     * 
     * @return
     */
    public boolean isReleaseOnce() {
        return touchPoints[0].isReleaseOnce();
    }

    /**
     * タッチされているかを調べる。
     * 
     * @return
     */
    public boolean isTouchOnce() {
        return touchPoints[0].isTouchOnce();
    }

    /**
     * 仮想ディスプレイ内をタッチしている場合、trueを返す。
     * 
     * @return
     */
    public boolean isInside() {
        return touchPoints[0].isInside();
    }

    /**
     * 毎フレームの更新を行う。
     * 
     */
    public void update() {
        for (TouchPoint tp : touchPoints) {
            tp.update();
        }

        //! 前のフレームの属性情報
        attrBefore = attrNow;

        //! 新しい属性情報
        attrNow = 0;
        if (isPinchIn()) {
            attrNow |= eAttrPichIn;
        }
        if (isPinchOut()) {
            attrNow |= eAttrPinchOut;
        }

        //! 位置の保存
        beforePosition.set(currentPosition);
        currentPosition.set(getTouchPoint(0).getCurrentX(), getTouchPoint(0).getCurrentY());

    }

    /**
     * タッチのスケーリング補正を行う。
     * 
     * @param x
     * @param y
     */
    public void setSizeScalling(float x, float y) {
        for (TouchPoint tp : touchPoints) {
            tp.setSizeScalling(x, y);
        }
    }

    /**
     * マルチタッチの各ポイントを扱う
     */
    public class TouchPoint {
        /**
         * 属性情報。
         */
        private int attribute = 0x0;
        /**
         * 前フレームの属性情報。
         */
        private int attrOld = 0x0;
        /**
         * 現フレームの属性情報。
         */
        private int attrNow = 0x0;
        /**
         * タッチしていた時間。
         */
        @SuppressWarnings("all")
        private int touchTimeMs = 0;
        /**
         * タッチ開始した時間。
         */
        private long touchStartTime = 0;

        /**
         * タッチしていた時間（フレーム）
         */
        private int touchFrame = 0;
        /**
         * タッチした座標。
         */
        private Point touchPos = new Point();
        /**
         * 離した位置。
         */
        private Point releasePos = new Point();

        /**
         * ディスプレイに触れている。
         */
        private static final int eAttrTouch = 1 << 0;

        @SuppressWarnings("unused")
        private int id = -1;

        private Vector2 sizeScalling = new Vector2(1, 1);

        /**
         * タッチ一箇所の値に対応している。
         * 
         * @author eagle.sakura
         * @param id
         */
        public TouchPoint(int id) {
            this.id = id;
        }

        void setSizeScalling(float x, float y) {
            sizeScalling.x = x;
            sizeScalling.y = y;
        }

        /**
         * タッチされた。
         * 
         * @author eagle.sakura
         * @param me
         */
        protected boolean onActionDown(float x, float y) {
            touchPos.x = (int) x;
            touchPos.y = (int) y;
            releasePos.x = (int) x;
            releasePos.y = (int) y;

            touchStartTime = System.currentTimeMillis();
            attribute = GameUtil.setFlag(attribute, eAttrTouch, true);
            return true;
        }

        /**
         * 移動された。
         * 
         * @author eagle.sakura
         * @param me
         */
        protected boolean onActionMove(float x, float y) {
            releasePos.x = (int) x;
            releasePos.y = (int) y;
            touchTimeMs = (int) (System.currentTimeMillis() - touchStartTime);
            attribute = GameUtil.setFlag(attribute, eAttrTouch, true);
            return true;
        }

        /**
         * 指が離された。
         * 
         * @author eagle.sakura
         * @param me
         * @return
         */
        protected boolean onActionUp(float x, float y) {
            releasePos.x = (int) x;
            releasePos.y = (int) y;
            touchTimeMs = (int) (System.currentTimeMillis() - touchStartTime);
            attribute = GameUtil.setFlag(attribute, eAttrTouch, false);
            return true;
        }

        /**
         * ディスプレイの外へ出た。
         * 
         * @author eagle.sakura
         * @param me
         * @return
         */
        protected boolean onActionOutside(float x, float y) {
            releasePos.x = (int) x;
            releasePos.y = (int) y;
            touchTimeMs = (int) (System.currentTimeMillis() - touchStartTime);
            attribute = GameUtil.setFlag(attribute, eAttrTouch, false);
            return true;
        }

        /**
         * ドラッグされた距離を取得する。
         * 
         * @author eagle.sakura
         * @return
         */
        public int getDrugVectorX() {
            return (int) ((releasePos.x - touchPos.x));
        }

        /**
         * 画面に触れた位置を取得する。
         * 
         * @author eagle.sakura
         * @return
         */
        public int getTouchPosX() {
            return (int) (touchPos.x);
        }

        /**
         * 画面に触れた位置を取得する。
         * 
         * @author eagle.sakura
         * @return
         */
        public int getTouchPosY() {
            return (int) (touchPos.y);
        }

        /**
         * 現在の指の位置、もしくは離した位置を取得する。
         * 
         * @author eagle.sakura
         * @return
         */
        public int getCurrentX() {
            return (int) (releasePos.x);
        }

        /**
         * 現在の指の位置、もしくは離した位置を取得する。
         * 
         * @author eagle.sakura
         * @return
         */
        public int getCurrentY() {
            return (int) (releasePos.y);
        }

        /**
         * 指定地点までの距離を取得する。
         * 
         * @author eagle.sakura
         * @param x
         * @param y
         * @return
         */
        public float getLength(int x, int y) {
            int lx = (int) (releasePos.x) - x, ly = (int) (releasePos.y) - y;

            return (float) Math.sqrt((double) (lx * lx + ly * ly));
        }

        /**
         * 指を引きずった長さを取得する。
         * 
         * @return
         */
        public float getDrugLength() {
            return (float) Math.sqrt(this.getDrugVectorX() * this.getDrugVectorX() + this.getDrugVectorY()
                    * this.getDrugVectorY());
        }

        /**
         * ドラッグされた距離を取得する。
         * 
         * @author eagle.sakura
         * @return
         */
        public int getDrugVectorY() {
            return (int) ((releasePos.y - touchPos.y));
        }

        /**
         * タッチされているかを調べる。
         * 
         * @author eagle.sakura
         * @return
         */
        public boolean isTouch() {
            return GameUtil.isFlagOn(attrNow, eAttrTouch);
        }

        /**
         * ディスプレイから指が離れているか。
         * 
         * @author eagle.sakura
         * @return
         */
        public boolean isRelease() {
            return !GameUtil.isFlagOn(attrNow, eAttrTouch);
        }

        /**
         * ディスプレイから指が離れた瞬間か。
         * 
         * @author eagle.sakura
         * @return
         */
        public boolean isReleaseOnce() {
            if (!GameUtil.isFlagOn(attrNow, eAttrTouch) && GameUtil.isFlagOn(attrOld, eAttrTouch)) {
                return true;
            }
            return false;
        }

        /**
         * タッチされているかを調べる。
         * 
         * @author eagle.sakura
         * @return
         */
        public boolean isTouchOnce() {
            if (GameUtil.isFlagOn(attrNow, eAttrTouch) && !GameUtil.isFlagOn(attrOld, eAttrTouch)) {
                return true;
            }
            return false;
        }

        /**
         * 毎フレームの更新を行う。
         * 
         * @author eagle.sakura
         */
        protected void update() {
            attrOld = attrNow;
            attrNow = attribute;

            if (isRelease() && !isReleaseOnce()) {
                touchFrame = 0;
            } else if (isTouch()) {
                ++touchFrame;
            }
        }

        /**
         * 何フレームタッチしたか。
         * 
         * @return
         */
        public int getTouchFrame() {
            return touchFrame;
        }

        /**
         * 画面内にタップがある場合trueを返す。
         * 
         * @return
         */
        public boolean isInside() {
            if (isRelease() && !isReleaseOnce()) {
                return false;
            }

            if (getCurrentX() < 0 || getCurrentX() > virtualDisplay.getVirtualDisplayWidth()) {
                return false;
            }

            if (getCurrentY() < 0 || getCurrentY() > virtualDisplay.getVirtualDisplayHeight()) {
                return false;
            }

            return true;
        }

        /**
         * 指定した範囲にタップがある場合trueを返す。
         * 
         * @return
         */
        public boolean isInside(Rect rect) {
            if (isRelease() && !isReleaseOnce()) {
                return false;
            }

            return rect.contains(getCurrentX(), getCurrentY());
        }
    }

    /**
     * ピンチ・インの判定を行う。
     * 
     * @author eagle.sakura
     * @return
     */
    public boolean isPinchIn() {
        TouchPoint tp0 = getTouchPoint(0), tp1 = getTouchPoint(1);

        //! どっちかが離されていたら認識しない
        if (tp0.isRelease() || tp1.isRelease()) {
            return false;
        }

        Vector2 v0 = new Vector2(tp0.getDrugVectorX(), tp0.getDrugVectorY()), v1 = new Vector2(tp1.getDrugVectorX(),
                tp1.getDrugVectorY());

        if (v0.length() < pinchLength || v1.length() < pinchLength) {
            return false;
        }

        float dot = v0.dot(v1);

        //! 同じ方向にベクトルが向いている。
        if (dot > 0) {
            return false;
        }

        v0.set(tp0.getTouchPosX(), tp0.getTouchPosY());
        v1.set(tp1.getTouchPosX(), tp1.getTouchPosY());
        float start = v0.length(v1);

        v0.set(tp0.getCurrentX(), tp0.getCurrentY());
        v1.set(tp1.getCurrentX(), tp1.getCurrentY());
        float end = v0.length(v1);

        //! 距離が狭まった
        return end < start;
    }

    /**
     * ピンチ・アウトの判定を行う。
     * 
     * @author eagle.sakura
     * @return
     */
    public boolean isPinchOut() {
        TouchPoint tp0 = getTouchPoint(0), tp1 = getTouchPoint(1);

        //! どっちかが離されていたら認識しない
        if (tp0.isRelease() || tp1.isRelease()) {
            return false;
        }

        Vector2 v0 = new Vector2(tp0.getDrugVectorX(), tp0.getDrugVectorY()), v1 = new Vector2(tp1.getDrugVectorX(),
                tp1.getDrugVectorY());

        if (v0.length() < pinchLength || v1.length() < pinchLength) {
            return false;
        }

        float dot = v0.dot(v1);

        //! 同じ方向にベクトルが向いている。
        if (dot > 0) {
            return false;
        }

        v0.set(tp0.getTouchPosX(), tp0.getTouchPosY());
        v1.set(tp1.getTouchPosX(), tp1.getTouchPosY());
        float start = v0.length(v1);

        v0.set(tp0.getCurrentX(), tp0.getCurrentY());
        v1.set(tp1.getCurrentX(), tp1.getCurrentY());
        float end = v0.length(v1);

        //! 距離が広まった
        return end > start;
    }

    /**
     * ピンチアウトが始まった瞬間にtrueを返す。
     * 
     * @return
     */
    public boolean isPinchOutStarting() {
        return (eAttrPinchOut & attrNow) != 0 && (eAttrPinchOut & attrBefore) == 0;
    }

    /**
     * ピンチインが始まった瞬間にtrueを返す。
     * 
     * @return
     */
    public boolean isPinchInStarting() {
        return (eAttrPichIn & attrNow) != 0 && (eAttrPichIn & attrBefore) == 0;
    }

    Vector2 beforePosition = new Vector2();

    Vector2 currentPosition = new Vector2();

    /**
     * 1フレームでのXの移動量。
     * 
     * @return
     */
    public float getFrameDrugX() {
        return currentPosition.x - beforePosition.x;
    }

    /**
     * 1フレームでのYの移動量。
     * 
     * @return
     */
    public float getFrameDrugY() {
        return currentPosition.y - beforePosition.y;
    }

    /**
     * 指定したエリアにあるタッチポイントを取得する。
     * @param area
     * @return
     */
    public TouchPoint getEnableTouchPoint(Rect area) {
        for (TouchPoint point : touchPoints) {
            if (point.isInside(area)) {
                return point;
            }
        }
        return null;
    }

    /**
     * 有効なタッチポイント数を取得する。
     * 
     * @return
     */
    public int getEnableTouchPoints() {
        int result = 0;
        if (getTouchPoint(0).isTouch()) {
            result++;
        }
        if (getTouchPoint(1).isTouch()) {
            result++;
        }
        return result;
    }

    /**
     * 関連付けられた仮想ディスプレイを取得する。
     * @return
     */
    public VirtualDisplay getVirtualDisplay() {
        return virtualDisplay;
    }
}
