package com.eaglesakura.lib.android.splib.fragment.egl.module;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.gl11.SpriteManager;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;
import com.eaglesakura.lib.android.splib.fragment.egl.EGLFragmentModule;

public class EGLFragmentSpriteModule extends EGLFragmentModule {
    /**
     * スプライト管理
     */
    SpriteManager spriteManager;

    /**
     * 仮想ディスプレイ管理
     */
    VirtualDisplay display = null;

    @Override
    public void onAttach(EGLFragment fragment) {
        super.onAttach(fragment);

        {
            display = new VirtualDisplay();
            display.setRealDisplaySize(fragment.getRenderAreaWidth(), fragment.getRenderAreaHeight());
            display.setVirtualDisplaySize(fragment.getRenderAreaWidth(), fragment.getRenderAreaHeight());
        }
        spriteManager = new SpriteManager(display, getGPU());
    }

    /**
     * バインドされているディスプレイを設定する
     */
    public VirtualDisplay getDisplay() {
        return display;
    }

    /**
     * レンダリングエリアを更新する
     */
    protected void updateRenderArea() {
        getGPU().updateDrawArea(display);
    }

    public SpriteManager getSpriteManager() {
        return spriteManager;
    }

    @Override
    public void dispose() {
        work(new Runnable() {
            @Override
            public void run() {
                spriteManager.dispose();
            }
        });
    }
}
