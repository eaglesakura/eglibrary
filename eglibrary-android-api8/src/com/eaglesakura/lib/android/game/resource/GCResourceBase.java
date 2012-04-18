package com.eaglesakura.lib.android.game.resource;

import java.util.List;

/**
 * {@link System#gc()}による管理ができない大規模リソースを管理する。
 * OpenGL ESのテクスチャ、Bitmapクラス等の管理を行う。
 * @author TAKESHI YAMASHITA
 *
 */
public abstract class GCResourceBase {
    protected GarbageCollector garbageCollector;

    public GCResourceBase(GarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    /**
     * 管理しているリソースを取得する。
     * @return
     */
    public abstract List<IRawResource> getRawResources();

    /**
     * 関連付けられたGCクラスを取得する。
     * @return
     */
    public GarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    /**
     * 解放メソッドが呼ばれたことを示す。
     */
    protected abstract void onDispose();

    /**
     * 管理している資源を明示的に解放する。
     * 解放自体は{@link IRawResource}を通して行われる。
     */
    public final void dispose() {
        onDispose();
        garbageCollector.remove(this);
    }
}
