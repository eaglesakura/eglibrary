package com.eaglesakura.lib.android.splib.fragment.egl.module;

import java.util.HashMap;
import java.util.Map;

import android.view.KeyEvent;

import com.eaglesakura.lib.android.game.util.Timer;
import com.eaglesakura.lib.android.splib.fragment.egl.EGLFragmentModule;

/**
 * キー操作を受け取るモジュール
 * @author TAKESHI YAMASHITA
 *
 */
public class KeyModule extends EGLFragmentModule {

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

    public class KeyData {
        /**
         * 押しているキーコード
         */
        int keyCode = -1;

        /**
         * 
         */
        Timer timer = new Timer();

        /**
         * キーが有効な場合true
         */
        boolean exist = true;

        /**
         * 有効ならtrue
         * @return
         */
        public boolean isExist() {
            return exist;
        }

        private void begin(int keyCode) {
            this.keyCode = keyCode;
            timer.start();
            exist = true;
        }

        /**
         * キー操作を終了した
         */
        private void end() {
            timer.end();
            exist = false;
        }

        /**
         * キーを押している時間を取得する
         * @return
         */
        public int getPressTime() {
            if (timer.isEnd()) {
                return -1;
            }

            return (int) (System.currentTimeMillis() - timer.getStartTime());
        }
    }

    /**
     * 管理中のキー一欄
     */
    Map<Integer, KeyModule.KeyData> keysMap = new HashMap<Integer, KeyModule.KeyData>();

    /**
     * キーが押された
     */
    @Override
    public void onKeyDown(int keyCode, KeyEvent __) {
        synchronized (keysMap) {
            KeyData data = keysMap.get(keyCode);
            if (data == null) {
                data = new KeyData();
                keysMap.put(keyCode, data);
            }
            data.begin(keyCode);
        }
    }

    /**
     * キーが離された
     */
    @Override
    public void onKeyUp(int keyCode, KeyEvent __) {
        synchronized (keysMap) {
            KeyData data = keysMap.get(keyCode);
            if (data == null) {
                return;
            }
            data.end();
        }
    }

    /**
     * キーを押している時間をミリ秒単位で取得する
     * 押されていない場合は負の値が帰る
     * @param keyCode
     * @return
     */
    public int getKeyPressTimeMS(int keyCode) {
        synchronized (keysMap) {
            KeyData data = keysMap.get(keyCode);
            if (data == null) {
                return -1;
            }

            return data.getPressTime();
        }
    }

    /**
     * 指定したキーが押されていたらtrueを返す。
     * @param keyCode
     * @return
     */
    public boolean isPressed(int keyCode) {
        return getKeyPressTimeMS(keyCode) >= 0;
    }

    @Override
    public void dispose() {

    }
}
