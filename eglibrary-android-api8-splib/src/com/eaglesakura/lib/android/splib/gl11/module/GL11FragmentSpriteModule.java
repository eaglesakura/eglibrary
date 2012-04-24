package com.eaglesakura.lib.android.splib.gl11.module;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.gl11.SpriteManager;
import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;

public class GL11FragmentSpriteModule extends GL11FragmentModule {
    /**
     * スプライト管理
     */
    SpriteManager spriteManager;

    /**
     * 仮想ディスプレイ管理
     */
    VirtualDisplay display = null;

    @Override
    public void onAttach(GL11Fragment fragment) {
        super.onAttach(fragment);

        {
            display = new VirtualDisplay();
            display.setRealDisplaySize(getRenderAreaWidth(), getRenderAreaHeight());
            display.setVirtualDisplaySize(getRenderAreaWidth(), getRenderAreaHeight());
        }

        spriteManager = new SpriteManager(display, getGLManager());
    }

    /**
     * バインドされているディスプレイを設定する
     * @return
     */
    public VirtualDisplay getDisplay() {
        return display;
    }

    /**
     * レンダリングエリアを更新する
     */
    protected void updateRenderArea() {
        getGLManager().updateDrawArea(display);
    }

    public SpriteManager getSpriteManager() {
        return spriteManager;
    }
}
