package com.eaglesakura.lib.android.game.resource;

import com.eaglesakura.lib.android.game.thread.UIHandler;

import java.util.List;

/**
 * {@link System#gc()}による管理ができない大規模リソースを管理する。
 * OpenGL ESのテクスチャ、Bitmapクラス等の管理を行う。
 *
 * @author TAKESHI YAMASHITA
 */
public abstract class GCResourceBase extends DisposableResource {
    /**
     * GC管理クラス
     */
    private GarbageCollector garbageCollector;

    /**
     *
     * @param garbageCollector
     */
    public GCResourceBase(GarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;

        if (garbageCollector == null) {
            this.garbageCollector = new GarbageCollector(UIHandler.getInstance());
        }
    }

    /**
     * finalizeは確実性が低いため、オーバーライドを許さない。
     */
    @Override
    protected final void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * 管理しているリソースを取得する。
     */
    public abstract List<IRawResource> getRawResources();

    /**
     * 関連付けられたGCクラスを取得する。
     */
    public GarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    /**
     * GCクラスへ登録する。
     * 通常、リソースを作成した後にこのメソッドを呼び出す。
     */
    protected final void register() {
        garbageCollector.add(this);
    }

    /**
     * 解放メソッドが呼ばれたことを示す。
     */
    protected abstract void onDispose();

    /**
     * 管理している資源を明示的に解放する。
     * 解放自体は{@link IRawResource}を通して行われる。
     */
    @Override
    public final void dispose() {
        onDispose();
        garbageCollector.dispose(this);
    }
}
