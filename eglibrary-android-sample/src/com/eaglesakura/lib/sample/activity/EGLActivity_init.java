package com.eaglesakura.lib.sample.activity;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.GLRenderer;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;
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
                Fragment fragment = new EGLFragment() {

                    void requestRendering() {
                        Thread thread = (new Thread() {
                            @Override
                            public void run() {
                                getEgl().rendering(new GLRenderer() {

                                    @Override
                                    public void onWorking(EGLManager egl) {

                                    }

                                    @Override
                                    public void onSurfaceReady(EGLManager egl) {

                                    }

                                    @Override
                                    public void onSurfaceNotReady(EGLManager egl) {
                                        LogUtil.log("onSurfaceNotReady");
                                    }

                                    @Override
                                    public void onRendering(EGLManager egl) {
                                        GL11 gl = egl.getGL();
                                        gl.glClearColor((float) Math.random(), (float) Math.random(),
                                                (float) Math.random(), 1);
                                        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

                                        requestRendering();
                                    }
                                });
                            }
                        });
                        thread.setName("rendering-thread");
                        thread.start();
                    }

                    @Override
                    protected void onEglResume(EGLManager egl) {
                        requestRendering();
                    }
                };
                transaction.add(R.id.gl_area, fragment, fragment.getClass().getName());
            }
            transaction.commit();
        }
    }
}
