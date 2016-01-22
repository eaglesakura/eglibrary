package com.eaglesakura.android.glkit.media;

import com.eaglesakura.android.glkit.egl.EGLSpecRequest;
import com.eaglesakura.android.glkit.egl.GLESVersion;
import com.eaglesakura.android.glkit.egl.IEGLDevice;
import com.eaglesakura.android.glkit.egl11.EGL11Manager;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.thread.Holder;
import com.eaglesakura.util.LogUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class OffscreenPreviewSurface {

    final Context context;
    EGL11Manager eglManager;
    IEGLDevice eglDevice;

    Object surface;

    int previewTexture;

    public OffscreenPreviewSurface(Context context) {
        this.context = context;
    }

    private Object createSurfaceTexture() {
        eglManager = new EGL11Manager(context);

        // EGL初期化する
        EGLSpecRequest eglSpecRequest = new EGLSpecRequest();
        eglSpecRequest.version = GLESVersion.GLES20;
        eglManager.initialize(eglSpecRequest);
        eglDevice = eglManager.newDevice(null);
        eglDevice.createPBufferSurface(1, 1);
        if (!eglDevice.bind()) {
            throw new IllegalStateException("EGL createSurface failed");
        }


        this.previewTexture = genPreviewTexture();
        surface = new SurfaceTexture(previewTexture);
        return surface;
    }

    private Object createSurfaceView() {
        final Holder<Object> viewHolder = new Holder<>();
        UIHandler.postUI(new Runnable() {
            SurfaceView view;

            @Override
            public void run() {
                view = new SurfaceView(context.getApplicationContext());
                view.getHolder().addCallback(callback);
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                        PixelFormat.TRANSLUCENT);
                wm.addView(view, params);
            }

            SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    LogUtil.log("surfaceCreated(%s)", holder.toString());
                    viewHolder.set(view);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                }
            };
        });

        surface = viewHolder.getWithWait();
        return surface;
    }

    /**
     * 初期化処理を行う
     */
    public Object createSurface() {
        if (surface != null) {
            return surface;
        }

        if (AndroidUtil.isSupportedSurfaceTexture()) {
            return createSurfaceTexture();
        } else {
            return createSurfaceView();
        }
    }

    private static int genPreviewTexture() {
        int[] temp = new int[1];
        glGenTextures(1, temp, 0);
        int texture = temp[0];
        return texture;
    }

    /**
     * 開放処理を行う
     */
    public void dispose() {
        if (surface != null) {
            if (surface instanceof SurfaceView) {
                // detach window
                final Object lock = new Object();

                UIHandler.postUI(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        wm.removeView((View) surface);
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });

                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (Exception e) {
                    }
                }
            } else {
                ((SurfaceTexture) surface).release();
            }
            surface = null;
        }

        if (previewTexture != 0) {
            glDeleteTextures(1, new int[]{previewTexture}, 0);
            previewTexture = 0;
        }

        if (eglDevice != null) {
            eglDevice.unbind();
            eglDevice.dispose();
            eglDevice = null;
        }
    }
}
