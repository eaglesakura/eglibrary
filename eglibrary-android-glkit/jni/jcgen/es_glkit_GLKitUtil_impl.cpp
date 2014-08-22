#include <array>
#include "es_glkit_GLKitUtil.h"

namespace es {
namespace glkit {

namespace {

static ::jc::lang::class_wrapper class_GLKitUtil;
static ::std::array<jmethodID, 1> class_GLKitUtil_methods;
static void es_glkit_GLKitUtil_initialize(JNIEnv *env) {

    if(class_GLKitUtil.hasObject()){ return; } // initialized
    
    class_GLKitUtil = ::jc::lang::class_wrapper::find(env, "com/eaglesakura/android/glkit/GLKitUtil");
    assert(class_GLKitUtil.hasObject());
    
    class_GLKitUtil.setMultiThreadAccess(true);
    /* load method */
    class_GLKitUtil_methods[0] = class_GLKitUtil.getMethod("nativeCaptureDevice", "(Lcom/eaglesakura/android/glkit/egl/IEGLDevice;Landroid/graphics/Bitmap;)Z", true);
    assert(methodObjectName[0]);
    /* load field */
    
}

}
::jc::lang::boolean_wrapper GLKitUtil::nativeCaptureDevice(::jc::lang::object_wrapper arg0, ::jc::lang::object_wrapper arg1) {
    JNIEnv *env = class_GLKitUtil.getEnv();
    es_glkit_GLKitUtil_initialize(env);
    return ::jc::lang::boolean_wrapper(env->CallStaticBooleanMethod(class_GLKitUtil.getJclass(), class_GLKitUtil_methods[0], arg0.getJobject(), arg1.getJobject()));
}

}
}

#if 0 /* stub! */
#include "es_glkit_GLKitUtil.h"
extern "C" {

JNIEXPORT jboolean JNICALL Java_com_eaglesakura_android_glkit_GLKitUtil_nativeCaptureDevice(JNIEnv *env, jobject arg0, jobject arg1) {
}

}
#endif /* stub! */
