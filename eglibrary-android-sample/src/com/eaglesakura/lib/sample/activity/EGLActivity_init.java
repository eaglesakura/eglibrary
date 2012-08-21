package com.eaglesakura.lib.sample.activity;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;
import com.eaglesakura.lib.android.splib.fragment.egl.EGLFragmentModule;
import com.eaglesakura.lib.sample.R;

public class EGLActivity_init extends FragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        LogUtil.setTag("egl");
        LogUtil.setOutput(true);
        setContentView(R.layout.gllayout);
        super.onCreate(bundle);

        if (bundle == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                EGLFragment fragment = new EGLFragment() {
                    @Override
                    protected void onEGLResume() {
                        this.rendering();
                    }
                };

                fragment.getRootModule().addModule(new EGLFragmentModule() {
                    @Override
                    public void onRendering() {
                        GL11 gl = getGL();
                        gl.glClearColor(0, (float) Math.random(), (float) Math.random(), 1);
                        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
                    }
                });
                transaction.add(R.id.gl_area, fragment, fragment.getClass().getName());
            }
            transaction.commit();
        }
    }
}
