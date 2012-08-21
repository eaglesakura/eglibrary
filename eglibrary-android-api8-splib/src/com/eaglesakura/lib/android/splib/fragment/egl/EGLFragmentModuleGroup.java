package com.eaglesakura.lib.android.splib.fragment.egl;

import android.view.KeyEvent;

import com.eaglesakura.lib.android.splib.fragment.EGLFragment;
import com.eaglesakura.lib.list.OrderAccessList;
import com.eaglesakura.lib.list.OrderAccessList.Iterator;

public class EGLFragmentModuleGroup extends EGLFragmentModule {

    OrderAccessList<EGLFragmentModule> childs = new OrderAccessList<EGLFragmentModule>();

    /**
     * モジュールを追加する
     * @param module
     */
    public void addModule(final EGLFragmentModule module) {

        work(new Runnable() {
            @Override
            public void run() {
                module.onAttach(getFragment());
                childs.add(module);
            }
        });
    }

    /**
     * モジュールを追加する
     * @param module
     * @param tag
     */
    public void addModule(final EGLFragmentModule module, final Object tag) {
        work(new Runnable() {

            @Override
            public void run() {
                module.setTag(tag);
                childs.add(module);
                module.onAttach(getFragment());
            }
        });
    }

    public EGLFragmentModule findModuleByTag(Object tag) {
        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            if (tag.equals(module.getTag())) {
                return module;
            }
            if (module instanceof EGLFragmentModuleGroup) {
                EGLFragmentModuleGroup group = (EGLFragmentModuleGroup) module;

                EGLFragmentModule result = group.findModuleByTag(tag);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * モジュールを削除する
     * @param module
     */
    public void remove(final EGLFragmentModule module) {
        if (childs.indexOf(module) < 0) {
            // 孫を探索する
            Iterator<EGLFragmentModule> iterator = childs.iterator();
            while (iterator.hasNext()) {
                EGLFragmentModule fragmentModule = iterator.next();
                if (fragmentModule instanceof EGLFragmentModuleGroup) {
                    EGLFragmentModuleGroup group = (EGLFragmentModuleGroup) fragmentModule;

                    // グループから削除する
                    group.remove(module);
                }
            }
        } else {
            // 直接子を持っている
            work(new Runnable() {
                @Override
                public void run() {
                    childs.remove(module);
                    module.onDetatch();
                }
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.dispose();
            }
        }
    }

    @Override
    public void onAttach(EGLFragment fragment) {
        super.onAttach(fragment);

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onAttach(fragment);
            }
        }
    }

    @Override
    public void onDetatch() {
        super.onDetatch();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onDetatch();
            }
        }

    }

    @Override
    public void onFragmentResume() {
        super.onFragmentResume();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onFragmentResume();
            }
        }

    }

    @Override
    public void onFragmentSuspend() {
        super.onFragmentSuspend();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onFragmentSuspend();
            }
        }
    }

    @Override
    public void onEGLDispose() {
        super.onEGLDispose();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onEGLDispose();
            }
        }
    }

    @Override
    public void onEGLPause() {
        super.onEGLPause();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onEGLPause();
            }
        }

    }

    @Override
    public void onEGLResume() {
        super.onEGLResume();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onEGLResume();
            }
        }
    }

    @Override
    public void onGLSurfaceChanged(int width, int height) {
        super.onGLSurfaceChanged(width, height);

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onGLSurfaceChanged(width, height);
            }
        }
    }

    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onKeyDown(keyCode, event);
            }
        }
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        super.onKeyEvent(event);

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onKeyEvent(event);
            }
        }
    }

    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onKeyUp(keyCode, event);
            }
        }

    }

    @Override
    public void onRenderingBegin() {
        super.onRenderingBegin();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onRenderingBegin();
            }
        }
    }

    @Override
    public void onRendering() {
        super.onRendering();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onRendering();
            }
        }
    }

    @Override
    public void onRenderingEnd() {
        super.onRenderingEnd();

        Iterator<EGLFragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            EGLFragmentModule module = iterator.next();
            {
                module.onRenderingEnd();
            }
        }
    }
}
