package com.eaglesakura.android.debug;

import android.content.Context;

import com.eaglesakura.android.debug.window.RealtimeDebugWindow;
import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCField;
import com.eaglesakura.jc.annotation.JCMethod;

/**
 * デバッグ描画のNDKからのブリッジ
 * <p/>
 * Native側にサポート用のclassを配置する
 */
@JCClass(cppNamespace = "es.debug")
public class RealtimeDebugWindowBridge extends RealtimeDebugWindow {

    /**
     * 左側に描画する
     */
    @JCField
    public static final int FLAG_RENDERING_POSITION_LEFT = 0x1 << 3;

    /**
     * 右側に描画する
     */
    @JCField
    public static final int FLAG_RENDERING_POSITION_RIGHT = 0x1 << 4;

    public RealtimeDebugWindowBridge(Context context) {
        super(context);
    }

    @JCMethod
    @Override
    public void addTextMessage(String text, int flags, int showTimeMs, int rgba) {
        super.addTextMessage(text, flags, showTimeMs, rgba);
    }
}
