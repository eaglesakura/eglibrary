package com.eaglesakura.lib.sample.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.eaglesakura.lib.android.game.graphics.canvas.BitmapImage;
import com.eaglesakura.lib.android.game.graphics.gl11.BitmapTextureImage;
import com.eaglesakura.lib.android.game.graphics.gl11.SpriteManager;
import com.eaglesakura.lib.android.game.graphics.gl11.TextureImageBase;
import com.eaglesakura.lib.android.splib.fragment.GL11Fragment;
import com.eaglesakura.lib.android.splib.gl11.module.BufferClearModule;
import com.eaglesakura.lib.android.splib.gl11.module.GL11FragmentSpriteModule;
import com.eaglesakura.lib.sample.R;

public class SpriteRotateSample extends FragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.gllayout);
        if (bundle == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                GL11Fragment glFragment = new GL11Fragment() {

                    @Override
                    protected void onGLInitialize(int width, int height) {
                        addModule(new BufferClearModule(0x00ffffff));

                        addModule(new GL11FragmentSpriteModule() {

                            TextureImageBase texture = null;

                            float rotate = 0;

                            @Override
                            public void onAttach(GL11Fragment fragment) {
                                super.onAttach(fragment);

                                BitmapImage image = new BitmapImage().loadFromDrawable(getResources(), R.drawable.bomb,
                                        null);
                                texture = new BitmapTextureImage(image.getBitmap(), getGLManager());
                            }

                            @Override
                            public void onRendering() {
                                SpriteManager spriteManager = getSpriteManager();

                                spriteManager.begin();
                                {
                                    float scale = 5;
                                    spriteManager.drawImage(
                                            texture,//
                                            0, 0, texture.getWidth(),
                                            texture.getHeight(), //
                                            50, 50, (int) (texture.getWidth() * scale),
                                            (int) (texture.getHeight() * scale), //
                                            rotate, 0xFFFFFFFF);

                                }
                                spriteManager.end();

                                rotate += 1;
                                rendering();
                            }
                        });

                        rendering();
                    }
                };

                transaction.add(R.id.gl_area, glFragment, GL11Fragment.class.getName());
            }
            transaction.commit();
        }
    }
}
