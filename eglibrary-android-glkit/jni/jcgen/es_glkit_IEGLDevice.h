#ifndef __ES_GLKIT_IEGLDEVICE_H__
#define __ES_GLKIT_IEGLDEVICE_H__

#include  "JointConnector.hpp"

namespace es {
namespace glkit {

class IEGLDevice {
public:
    
    virtual ~IEGLDevice(){}
    
    /* Constant Fields */
    /* Fields */
    /* Methods */
    virtual ::jc::lang::boolean_wrapper bind () = 0;
    
    virtual ::jc::lang::s32_wrapper getSurfaceWidth () = 0;
    
    virtual ::jc::lang::s32_wrapper getSurfaceHeight () = 0;
    
    virtual void dispose () = 0;
    
    virtual ::jc::lang::boolean_wrapper isBinded () = 0;
    
    virtual void unbind () = 0;
    
    virtual ::jc::lang::boolean_wrapper hasSurfaceDestroyRequest () = 0;
    
    virtual void createPBufferSurface (::jc::lang::s32_wrapper arg0, ::jc::lang::s32_wrapper arg1) = 0;
    
    virtual ::jc::lang::boolean_wrapper hasSurface () = 0;
    
    virtual ::jc::lang::boolean_wrapper isBindedThread () = 0;
    
    virtual void swapBuffers () = 0;
    
    virtual ::jc::lang::boolean_wrapper hasContext () = 0;
    
    virtual ::jc::lang::boolean_wrapper isWindowDevice () = 0;
    
    
};

}
}

#endif // __ES_GLKIT_IEGLDEVICE_H__
