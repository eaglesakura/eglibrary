package com.eaglesakura.lib.android.game.thread;

import android.os.Handler;
import android.os.Looper;

/**
 * UIスレッド専用のハンドラ
 * @author Takeshi
 *
 */
public class UIHandler extends Handler {

    public UIHandler() {
        super(Looper.getMainLooper());
    }
}
