package com.eaglesakura.lib.android.game.graphics.gl11;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.eaglesakura.lib.android.game.graphics.gl11.DisposableGLResource.GLResource;

/**
 * OpenGL ES用のガベージコレクタクラス。
 * @author TAKESHI YAMASHITA
 *
 */
public class GLGarbageCollector {
    private OpenGLManager glManager = null;

    /**
     * GC対象を管理するクラス。
     * @author TAKESHI YAMASHITA
     *
     */
    static class GCTarget {
        GLResource resource = null;
        WeakReference<DisposableGLResource> parent = null;
    }

    /**
     * マークする処理対象。
     */
    private List<GCTarget> marks = new LinkedList<GLGarbageCollector.GCTarget>();

    /**
     * 
     * @param glHandler
     */
    public GLGarbageCollector(OpenGLManager glManager) {
        this.glManager = glManager;
    }

    /**
     * gc対象に追加を行う。
     * @param resource
     */
    public void add(DisposableGLResource resource) {
        // 関連するリソースを削除する
        remove(resource);

        // 再度登録する
        List<GLResource> resources = resource.getRawResources();
        for (GLResource r : resources) {
            GCTarget gctarget = new GCTarget();
            gctarget.parent = new WeakReference<DisposableGLResource>(resource);
            gctarget.resource = r;
            marks.add(gctarget);
        }
    }

    /**
     * gc対象から排除を行う。
     * @param resource
     */
    public void remove(DisposableGLResource resource) {
        Iterator<GCTarget> iterator = marks.iterator();

        while (iterator.hasNext()) {
            final GCTarget next = iterator.next();
            final DisposableGLResource disposableGLResource = next.parent.get();

            if (disposableGLResource == resource) {
                // 削除対象を見つけたから、単純にリストから排除する
                iterator.remove();
            }
        }
    }

    /**
     * ガベージコレクション対象のクラス数を取得する。
     * @return
     */
    public int getGcTargetCount() {
        return marks.size();
    }

    /**
     * 死んでいる参照を削除する。
     */
    private void _remove(GCTarget target) {
        if (target.parent.get() != null) {
            return;
        }

        GLResource resource = target.resource;
        resource.delete(glManager.getGL());
    }

    /**
     * OpenGLリソースのGCを行う。
     * @return 廃棄したGLリソース数
     */
    public int gc() {
        if (!glManager.isGLThread()) {
            glManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    gc();
                }
            });
            return 0;
        }

        int result = 0;
        Iterator<GCTarget> iterator = marks.iterator();
        while (iterator.hasNext()) {
            GCTarget gctarget = iterator.next();

            // 参照が死んでるため、解放を行う。
            if (gctarget.parent.get() == null) {
                _remove(gctarget);
                ++result;
                iterator.remove();
            }
        }
        System.gc();
        return result;
    }

    /**
     * 管理リソースを強制的に解放する
     */
    public int delete() {
        int result = 0;
        Iterator<GCTarget> iterator = marks.iterator();
        while (iterator.hasNext()) {
            GCTarget gctarget = iterator.next();

            // 強制的に解放を行う。
            _remove(gctarget);
            ++result;
            iterator.remove();
        }
        System.gc();
        return result;
    }

}
