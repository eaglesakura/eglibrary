package com.eaglesakura.android.glkit.egl;

import android.os.Build;

import javax.microedition.khronos.egl.EGL10;

/**
 * OpenGL ESのバージョンを指定する
 */
public enum GLESVersion {
    /**
     * OpenGL ES 1.1
     */
    GLES11 {
        @Override
        public int[] getContextAttribute() {
            return null;
        }
    },

    /**
     * OpenGL ES 2.0
     */
    GLES20 {
        @Override
        public int[] getContextAttribute() {
            return new int[]{
                    0x3098 /*EGL_CONTEXT_CLIENT_VERSION */, 2,
                    EGL10.EGL_NONE,
            };
        }
    },

    /**
     * OpenGL ES 3.0
     */
    GLES30 {
        @Override
        public int[] getContextAttribute() {
            return new int[]{
                    0x3098 /*EGL_CONTEXT_CLIENT_VERSION */, 3,
                    EGL10.EGL_NONE,
            };
        }
    },

    /**
     * OpenGL ES 3.1
     */
    GLES31 {
        @Override
        public int[] getContextAttribute() {
            return new int[]{
                    0x3098 /*EGL_CONTEXT_CLIENT_VERSION */, 3,
                    EGL10.EGL_NONE,
            };
        }
    };

    /**
     * eglCreateContext用attrを取得する
     *
     * @return
     */
    public abstract int[] getContextAttribute();

    /**
     * サポートする最大バージョンを取得する
     */
    public static GLESVersion supported() {
        if (Build.VERSION.SDK_INT < 8) {
            return GLES11;
        } else if (Build.VERSION.SDK_INT < 18) {
            return GLES20;
        } else if (Build.VERSION.SDK_INT < 20) {
            return GLES30;
        } else {
            return GLES31;
        }
    }
}
