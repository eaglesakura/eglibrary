#define GL_GLEXT_PROTOTYPES 1

#include "GLES/gl.h"
#include "GLES/glext.h"

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glMatrixMode
 * Signature: (I)V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glMatrixMode(
        JNIEnv
*env,
jobject _this, jint
mode) {
glMatrixMode(mode);
}

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glCurrentPaletteMatrixOES
 * Signature: (I)V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glCurrentPaletteMatrixOES(
        JNIEnv
*env,
jobject _this, jint
index) {
glCurrentPaletteMatrixOES(index);
}

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glLoadMatrixx
 * Signature: (Ljava/nio/Buffer;)V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glLoadMatrixx(
        JNIEnv
*env,
jobject _this, jobject
buffer) {
glLoadMatrixx((*env)
->
GetDirectBufferAddress(env, buffer
));
}

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glLoadMatrixf
 * Signature: (Ljava/nio/Buffer;)V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glLoadMatrixf(
        JNIEnv
*env,
jobject _this, jobject
buffer) {
glLoadMatrixf((*env)
->
GetDirectBufferAddress(env, buffer
));
}

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glEnable
 * Signature: (I)V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glEnable(
        JNIEnv
*env,
jobject _this, jint
param) {
glEnable(param);
}

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glEnableClientState
 * Signature: (I)V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glEnableClientState(
        JNIEnv
*env,
jobject _this, jint
param) {
glEnableClientState(param);
}

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glWeightPointerOES
 * Signature: (IIILjava/nio/Buffer;)V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glWeightPointerOES(
        JNIEnv
*env,
jobject _this, jint
size,
jint type, jint
stride,
jobject buffer
) {
glWeightPointerOES(size, type, stride,
(*env)
->
GetDirectBufferAddress(env, buffer
));
}

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glMatrixIndexPointerOES
 * Signature: (IIILjava/nio/Buffer;)V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glMatrixIndexPointerOES(
        JNIEnv
*env,
jobject _this, jint
size,
jint type, jint
stride,
jobject buffer
) {
glMatrixIndexPointerOES(size, type, stride,
(*env)
->
GetDirectBufferAddress(env, buffer
));
}

/*
 * Class:     com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension
 * Method:    glLoadPaletteFromModelViewMatrixOES
 * Signature: ()V
 */JNIEXPORT void JNICALL
Java_com_eaglesakura_lib_android_game_graphics_gl11_GL11Extension_glLoadPaletteFromModelViewMatrixOES(
        JNIEnv
*env,
jobject _this
) {
glLoadPaletteFromModelViewMatrixOES();

}
