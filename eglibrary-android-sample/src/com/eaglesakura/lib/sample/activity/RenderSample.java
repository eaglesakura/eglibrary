package com.eaglesakura.lib.sample.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.eaglesakura.lib.android.game.graphics.canvas.BitmapImage;
import com.eaglesakura.lib.android.game.graphics.gl11.BitmapTextureImage;
import com.eaglesakura.lib.android.game.graphics.gl11.SpriteManager;
import com.eaglesakura.lib.android.game.graphics.gl11.TextureImageBase;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;
import com.eaglesakura.lib.android.splib.fragment.egl.module.EGLFragmentSpriteModule;
import com.eaglesakura.lib.sample.R;

public class RenderSample extends FragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LogUtil.setTag("EGL-TEST");
        LogUtil.setOutput(true);
        setContentView(R.layout.gllayout);
        if (bundle == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                Fragment fragment = new SampleFragment();
                transaction.add(R.id.gl_area, fragment, fragment.getClass().getName());
            }
            transaction.commit();
        }
    }

    public static class SampleFragment extends EGLFragment {
        EGLFragmentSpriteModule renderModule = new EGLFragmentSpriteModule() {
            TextureImageBase texture = null;

            float rotate = 0;

            @Override
            public void onAttach(EGLFragment fragment) {
                super.onAttach(fragment);

                work(new Runnable() {
                    @Override
                    public void run() {
                        BitmapImage image = new BitmapImage().loadFromDrawable(getResources(), R.drawable.ic_launcher,
                                null);
                        texture = new BitmapTextureImage(image.getBitmap(), getVRAM());
                        image.dispose();
                    }
                });
            }

            @Override
            public void onRendering() {
                getGPU().clearColorRGBA(0.0f, 0.5f, 1.0f, 1.0f);
                getGPU().clear();
                SpriteManager spriteManager = getSpriteManager();

                spriteManager.begin();
                {
                    spriteManager.fillRect(0, 0, 100, 100, 0xFFFFFFFF);

                    float scale = 5;
                    //    rotate = 45;//
                    spriteManager.drawImage(texture,//
                            0, 0, texture.getWidth(), texture.getHeight(), //
                            50, 50, (int) (texture.getWidth() * scale), (int) (texture.getHeight() * scale), //
                            rotate, 0xFFFFFFFF);

                }
                spriteManager.end();

                rotate += 1;
                rendering();
            }
        };

        @Override
        protected void onEGLInitialized() {
            super.onEGLInitialized();
            getRootModule().addModule(renderModule);
            rendering();
        }
        /*
        @Override
        protected void onGLInitialize(int width, int height) {
            //            addModule(new BufferClearModule(0x00ffffff));
            addModule(renderModule);
            rendering();
        }
        */
    }
}
