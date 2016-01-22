package com.eaglesakura.lib.android.game.resource;

import com.eaglesakura.lib.android.game.thread.UIHandler;
import com.eaglesakura.lib.android.game.util.ContextUtil;

import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * GCターゲットとして扱えないリソース（GL系、Bitmap系）を一元管理する。
 *
 * @author TAKESHI YAMASHITA
 */
public class GarbageCollector {
    protected static class GCTarget {
        /**
         * 解放対象のリソース
         */
        IRawResource rawResource = null;

        /**
         * 監視対象のクラス
         */
        WeakReference<GCResourceBase> parent;
    }

    /**
     * 監視対象のりソース一覧
     */
    protected List<GCTarget> marks = new LinkedList<GarbageCollector.GCTarget>();

    /**
     * 解放対象のハンドラ。
     * nullの場合、常に同期的に解放を行う。
     */
    protected Handler handler = null;

    /**
     * @param gcHandler 解放対象のスレッドを示すハンドラ
     */
    public GarbageCollector(Handler gcHandler) {
        this.handler = gcHandler;
    }

    /**
     * UIスレッドでのGCハンドラを生成する。
     */
    public GarbageCollector() {
        this(UIHandler.getInstance());
    }

    /**
     * gc対象に追加を行う。
     */
    public void add(GCResourceBase resource) {
        // 関連するリソースを削除する
        remove(resource);

        synchronized (marks) {
            // 再度登録する
            List<IRawResource> resources = resource.getRawResources();
            for (IRawResource r : resources) {
                GCTarget gctarget = new GCTarget();
                gctarget.parent = new WeakReference<GCResourceBase>(resource);
                gctarget.rawResource = r;
                marks.add(gctarget);
            }
        }
    }

    /**
     * gc対象から排除を行う。
     */
    public void remove(GCResourceBase resource) {
        synchronized (marks) {
            Iterator<GCTarget> iterator = marks.iterator();

            while (iterator.hasNext()) {
                final GCTarget next = iterator.next();
                final GCResourceBase disposableGLResource = next.parent.get();

                if (disposableGLResource == resource) {
                    // 削除対象を見つけたから、単純にリストから排除する
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 指定したリソースに関連するRawResourceを解放する。
     */
    public void dispose(final GCResourceBase resource) {
        if (handler != null && !ContextUtil.isHandlerThread(handler)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dispose(resource);
                }
            });
            return;
        }

        synchronized (marks) {
            Iterator<GCTarget> iterator = marks.iterator();
            while (iterator.hasNext()) {
                GCTarget gcTarget = iterator.next();
                GCResourceBase check = gcTarget.parent.get();
                if (check == null || check == resource) {
                    _remove(gcTarget, true);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * ガベージコレクション対象のクラス数を取得する。
     */
    public int getGcTargetCount() {
        synchronized (marks) {
            return marks.size();
        }
    }

    /**
     * gc対象が空の場合、trueを返す。
     */
    public boolean isEmpty() {
        synchronized (marks) {
            return marks.isEmpty();
        }
    }

    /**
     * 死んでいる参照を削除する。
     *
     * @param target  解放対象
     * @param forcing 参照有無にかかわらず強制削除する場合はtrue
     */
    private void _remove(GCTarget target, boolean forcing) {
        if (!forcing && target.parent.get() != null) {
            return;
        }

        IRawResource resource = target.rawResource;
        resource.dispose();
    }

    /**
     * 不要なメモリを取り除く
     *
     * @return 開放した資源の数
     */
    public int gc() {
        if (handler != null && !ContextUtil.isHandlerThread(handler)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    gc();
                }
            });
            return 0;
        }

        synchronized (marks) {
            int result = 0;
            Iterator<GCTarget> iterator = marks.iterator();
            while (iterator.hasNext()) {
                GCTarget gctarget = iterator.next();
                // 参照が死んでるため、解放を行う。
                if (gctarget.parent.get() == null) {
                    _remove(gctarget, false);
                    ++result;
                    iterator.remove();
                }
            }

            if (result > 0) {
                System.gc();
            }

            return result;
        }
    }

    /**
     * 管理リソースを強制的に解放する
     */
    public int delete() {

        synchronized (marks) {
            int result = 0;
            Iterator<GCTarget> iterator = marks.iterator();
            while (iterator.hasNext()) {
                GCTarget gctarget = iterator.next();

                // 強制的に解放を行う。
                _remove(gctarget, true);
                ++result;
                iterator.remove();
            }

            if (result > 0) {
                System.gc();
            }
            return result;
        }
    }
}
