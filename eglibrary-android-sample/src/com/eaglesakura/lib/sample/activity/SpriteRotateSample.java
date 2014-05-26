package com.eaglesakura.lib.sample.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.eaglesakura.lib.sample.R;

public class SpriteRotateSample extends FragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.gllayout);
        if (bundle == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                //                Fragment fragment = new SampleFragment();
                //                transaction.add(R.id.gl_area, fragment, fragment.getClass().getName());
            }
            transaction.commit();
        }
    }

    /*
    public static class SampleFragment extends GL11Fragment {

        GL11FragmentSpriteModule renderModule = new GL11FragmentSpriteModule() {

            TextureImageBase texture = null;

            float rotate = 0;

            @Override
            public void onAttach(GL11Fragment fragment) {
                super.onAttach(fragment);

                BitmapImage image = new BitmapImage().loadFromDrawable(getResources(), R.drawable.ic_launcher, null);
                texture = new BitmapTextureImage(image.getBitmap(), getGLManager());
            }

            @Override
            public void onRendering() {
                SpriteManager spriteManager = getSpriteManager();

                spriteManager.begin();
                {
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
        protected void onGLInitialize(int width, int height) {
            addModule(new BufferClearModule(0x00ffffff));
            addModule(renderModule);
            rendering();
        }
    }
    */
}
