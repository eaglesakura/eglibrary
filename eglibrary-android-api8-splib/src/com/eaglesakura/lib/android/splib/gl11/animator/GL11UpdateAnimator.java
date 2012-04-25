package com.eaglesakura.lib.android.splib.gl11.animator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;

/**
 * GLオブジェクトの更新と描画を行わせる
 * @author TAKESHI YAMASHITA
 *
 */
public class GL11UpdateAnimator extends GL11Animator {
    List<Updatable> updateObjects = new LinkedList<GL11UpdateAnimator.Updatable>();

    public GL11UpdateAnimator(GL11Fragment fragment) {
        super(fragment);
    }

    @Override
    protected boolean doAnimation(GL11Fragment fragment) {
        boolean finished = false;
        synchronized (updateObjects) {
            Iterator<Updatable> iterator = updateObjects.iterator();
            while (iterator.hasNext()) {
                Updatable updatable = iterator.next();
                if (updatable.update()) {
                    iterator.remove();
                }
            }
            finished = updateObjects.isEmpty();
        }
        fragment.rendering();
        return finished;
    }

    public interface Updatable {
        public boolean update();
    }

    /**
     * 更新可能オブジェクトを追加する
     * @param updatable
     */
    public GL11UpdateAnimator add(Updatable updatable) {
        synchronized (updateObjects) {
            if (updateObjects.contains(updatable)) {
                return this;
            }

            updateObjects.add(updatable);
            return this;
        }
    }
}
