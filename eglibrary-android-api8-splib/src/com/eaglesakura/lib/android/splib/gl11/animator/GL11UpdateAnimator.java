package com.eaglesakura.lib.android.splib.gl11.animator;

import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;
import com.eaglesakura.lib.list.OrderAccessList;

/**
 * GLオブジェクトの更新と描画を行わせる
 * @author TAKESHI YAMASHITA
 *
 */
public class GL11UpdateAnimator extends GL11Animator {
    OrderAccessList<Updatable> updateObjects = new OrderAccessList<GL11UpdateAnimator.Updatable>();

    public GL11UpdateAnimator(GL11Fragment fragment) {
        super(fragment);
    }

    @Override
    protected boolean doAnimation(GL11Fragment fragment) {
        boolean finished = false;

        OrderAccessList.Iterator<Updatable> iterator = updateObjects.iterator();
        while (iterator.hasNext()) {
            Updatable updatable = iterator.next();
            if (updatable.update()) {
                iterator.remove();
            }
        }

        finished = updateObjects.isEmpty();
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
    public GL11UpdateAnimator add(final Updatable updatable) {
        fragment.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (updateObjects.contains(updatable)) {
                    return;
                }

                updateObjects.add(updatable);
            }
        });
        return this;
    }

    /**
     * アップデート用オブジェクトを取得する。
     * @return
     */
    public OrderAccessList<Updatable> getUpdateObjects() {
        return updateObjects;
    }
}
