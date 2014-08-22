#ifndef __ES_GLKIT_GLKITUTIL_H__
#define __ES_GLKIT_GLKITUTIL_H__

#include  "JointConnector.hpp"

namespace es {
namespace glkit {

class GLKitUtil {
protected:
    ::jc::lang::object_wrapper mObject;
public:
    GLKitUtil(){}
    GLKitUtil(::jc::lang::object_wrapper obj){ this->mObject = obj; }
    GLKitUtil(jobject obj, JNIEnv *env = nullptr, bool newLocalRefFlag = true){ this->mObject = ::jc::lang::object_wrapper(obj, env, newLocalRefFlag); }
    
    virtual ~GLKitUtil(){}
    
    ::jc::lang::object_wrapper getWrapperObject() const { return mObject; }
    /* Constant Fields */
    /* Fields */
    /* Methods */
    static ::jc::lang::boolean_wrapper nativeCaptureDevice (::jc::lang::object_wrapper arg0, ::jc::lang::object_wrapper arg1);
    
    
};

}
}

#endif // __ES_GLKIT_GLKITUTIL_H__
