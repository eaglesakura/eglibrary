package com.eaglesakura.lib.android.game.input;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.view.KeyEvent;

import com.eaglesakura.lib.android.game.util.GameUtil;

/**
 * ゲーム中でキー入力をサポートするためのクラス。
 * デフォルトは上下左右。
 * @author TAKESHI YAMASHITA
 *
 */
public class KeyInput {

    public class Key {
        /**
         * キーが入力されている。
         */
        private static final int ATTR_KEY_PRESS = 0x1 << 0;

        /**
         * 最新のキー入力情報。
         * 毎フレーム更新される。
         */
        int attrNow = 0x00000000;

        /**
         * 1フレーム前の入力情報。
         */
        int attrOld = 0x00000000;

        /**
         * 非同期入力用の属性情報。
         */
        int attribute = 0x00000000;

        /**
         * 連続入力しているフレーム数。
         * 完全にreleaseされたときにリセットされる。
         */
        int pressFrames = 0;

        /**
         * 対応しているキーコード
         */
        int keyCode = 0;

        public Key(int keyCode) {
            this.keyCode = keyCode;
        }

        /**
         * タッチされているかを調べる。
         * 
         * 
         * @return
         */
        public boolean isTouch() {
            return GameUtil.isFlagOn(attrNow, ATTR_KEY_PRESS);
        }

        /**
         * ディスプレイから指が離れているか。
         * 
         * 
         * @return
         */
        public boolean isRelease() {
            return !GameUtil.isFlagOn(attrNow, ATTR_KEY_PRESS);
        }

        /**
         * ディスプレイから指が離れた瞬間か。
         * 
         * 
         * @return
         */
        public boolean isReleaseOnce() {
            if (!GameUtil.isFlagOn(attrNow, ATTR_KEY_PRESS) && GameUtil.isFlagOn(attrOld, ATTR_KEY_PRESS)) {
                return true;
            }
            return false;
        }

        /**
         * タッチされているかを調べる。
         * 
         * 
         * @return
         */
        public boolean isTouchOnce() {
            if (GameUtil.isFlagOn(attrNow, ATTR_KEY_PRESS) && !GameUtil.isFlagOn(attrOld, ATTR_KEY_PRESS)) {
                return true;
            }
            return false;
        }

        /**
         * 毎フレームの更新を行う。
         * 
         * 
         */
        void update() {
            attrOld = attrNow;
            attrNow = attribute;

            if (isRelease() && !isReleaseOnce()) {
                pressFrames = 0;
            } else if (isTouch()) {
                ++pressFrames;
                attribute = 0;
            }

            if (keyCode == KEY_BACK) {
                attribute = 0;
            }
        }
    }

    /**
     * 上キーを押した
     */
    public static final int KEY_UP = KeyEvent.KEYCODE_DPAD_UP;

    /**
     * 下キーを押した
     */
    public static final int KEY_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;

    /**
     * 左キーを押した
     */
    public static final int KEY_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;

    /**
     * 右キーを押した
     */
    public static final int KEY_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;

    /**
     * 中央ボタンを押した。
     */
    public static final int KEY_CENTER = KeyEvent.KEYCODE_DPAD_CENTER;

    /**
     * 決定ボタンを押した
     */
    public static final int KEY_ENTER = KeyEvent.KEYCODE_ENTER;

    /**
     * 戻るボタンを押した
     */
    public static final int KEY_BACK = KeyEvent.KEYCODE_BACK;

    /**
     * 受け取るキーベント一覧。
     */
    Map<Integer, Key> keys = new HashMap<Integer, KeyInput.Key>();

    public KeyInput() {

    }

    /**
     * 認識を行うキーを追加する。
     * @param keyCode
     */
    public void addKey(int keyCode) {
        keys.put(keyCode, new Key(keyCode));
    }

    /**
     * 毎フレームの更新を行う。
     */
    public void update() {
        Iterator<Entry<Integer, Key>> iterator = keys.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Integer, Key> entry = iterator.next();
            entry.getValue().update();
        }
    }

    /**
     * 対応したキーを取得する。
     * @param keyCode
     * @return
     */
    public Key getKey(int keyCode) {
        return keys.get(keyCode);
    }

    /**
     * keydownイベントを受け取る。
     * @param keyCode
     * @param event
     */
    public boolean onKeyEvent(int keyCode, KeyEvent event) {
        final int action = event.getAction();
        Key key = keys.get(keyCode);
        if (key != null) {
            if (action == KeyEvent.ACTION_DOWN) {
                key.attribute |= Key.ATTR_KEY_PRESS;
            } else if (action == KeyEvent.ACTION_UP) {
                key.attribute &= (~Key.ATTR_KEY_PRESS);
            }
            return true;
        }
        return false;
    }
}
