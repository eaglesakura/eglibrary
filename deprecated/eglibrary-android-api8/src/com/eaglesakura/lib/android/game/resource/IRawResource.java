package com.eaglesakura.lib.android.game.resource;

/**
 * {@link System#gc()}による管理ができない大規模リソースを保持する。
 * OpenGL ESのテクスチャ、Bitmapクラス等の管理を行う。
 * 指定した{@link GarbageCollector}で一元管理を行い、適当なタイミングでリソースの一括解放を行う。
 * 管理す忘れたリソースの解放ミスを防ぐ。
 *
 * @author TAKESHI YAMASHITA
 */
public interface IRawResource {
    /**
     * 管理しているraw資源を解放する。
     */
    public void dispose();
}
