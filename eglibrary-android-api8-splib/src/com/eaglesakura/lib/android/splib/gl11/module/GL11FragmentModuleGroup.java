package com.eaglesakura.lib.android.splib.gl11.module;

import android.view.KeyEvent;

import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;
import com.eaglesakura.lib.list.OrderAccessList;
import com.eaglesakura.lib.list.OrderAccessList.Iterator;

public class GL11FragmentModuleGroup extends GL11FragmentModule {

    OrderAccessList<GL11FragmentModule> childs = new OrderAccessList<GL11FragmentModule>();

    public void addModule(GL11FragmentModule module) {
        childs.add(module);
    }

    public void addModule(GL11FragmentModule module, Object tag) {
        module.setTag(tag);
        childs.add(module);
    }

    public GL11FragmentModule findModuleByTag(Object tag) {
        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            if (tag.equals(module.getTag())) {
                return module;
            }
        }
        return null;
    }

    /**
     * モジュールを削除する
     * @param module
     */
    public void remove(GL11FragmentModule module) {
        childs.remove(module);
    }

    @Override
    public void dispose() {
        super.dispose();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.dispose();
            }
        }
    }

    @Override
    public void onAttach(GL11Fragment fragment) {
        super.onAttach(fragment);

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onAttach(fragment);
            }
        }
    }

    @Override
    public void onDetatch() {
        super.onDetatch();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onDetatch();
            }
        }

    }

    @Override
    public void onFragmentResume() {
        super.onFragmentResume();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onFragmentResume();
            }
        }

    }

    @Override
    public void onFragmentSuspend() {
        super.onFragmentSuspend();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onFragmentSuspend();
            }
        }
    }

    @Override
    public void onGLDispose() {
        super.onGLDispose();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onGLDispose();
            }
        }
    }

    @Override
    public void onGLPause() {
        super.onGLPause();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onGLPause();
            }
        }

    }

    @Override
    public void onGLResume() {
        super.onGLResume();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onGLResume();
            }
        }
    }

    @Override
    public void onGLSurfaceChanged(int width, int height) {
        super.onGLSurfaceChanged(width, height);

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onGLSurfaceChanged(width, height);
            }
        }
    }

    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onKeyDown(keyCode, event);
            }
        }
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        super.onKeyEvent(event);

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onKeyEvent(event);
            }
        }
    }

    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onKeyUp(keyCode, event);
            }
        }

    }

    @Override
    public void onRenderingBegin() {
        super.onRenderingBegin();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onRenderingBegin();
            }
        }
    }

    @Override
    public void onRendering() {
        super.onRendering();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onRendering();
            }
        }
    }

    @Override
    public void onRenderingEnd() {
        super.onRenderingEnd();

        Iterator<GL11FragmentModule> iterator = childs.iterator();
        while (iterator.hasNext()) {
            GL11FragmentModule module = iterator.next();
            {
                module.onRenderingEnd();
            }
        }
    }
}
