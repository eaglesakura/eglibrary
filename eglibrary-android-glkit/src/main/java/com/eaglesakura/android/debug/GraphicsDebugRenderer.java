package com.eaglesakura.android.debug;

import android.content.Context;

import com.eaglesakura.android.debug.window.RealtimeDebugWindow;
import com.eaglesakura.android.debug.window.log.DebugText;
import com.eaglesakura.graphics.Color;
import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCField;
import com.eaglesakura.jc.annotation.JCMethod;

/**
 * グラフィックス用のデバッグ描画を行う
 * <p/>
 * Native側にサポート用のclassを配置する
 */
@JCClass(cppNamespace = "es.debug")
public class GraphicsDebugRenderer extends RealtimeDebugWindow {

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

    public GraphicsDebugRenderer(Context context) {
        super(context);
    }

    /**
     * レンダリングテキストを追加する
     *
     * @param text
     * @param flags
     * @param showTimeMs
     * @param rgba
     */
    @JCMethod
    public void addTextMessage(String text, int flags, int showTimeMs, int rgba) {
        DebugText debugText = new DebugText();
        debugText.setMessage(text);
        debugText.setFlags(flags);
        debugText.setRenderingTime(showTimeMs);
        debugText.setArgb(Color.rgba2argb(rgba));

        addMessage(debugText);
    }
}
