#ifndef __ES_GLKIT_IEGLCONTEXTGROUP_H__
#define __ES_GLKIT_IEGLCONTEXTGROUP_H__

#include  "JointConnector.hpp"

namespace es {
namespace glkit {

class IEGLContextGroup {
public:
    
    virtual ~IEGLContextGroup(){}
    
    /* Constant Fields */
    /* Fields */
    /* Methods */
    virtual ::jc::lang::s32_wrapper getDeviceNum () = 0;
    
    
};

}
}

#endif // __ES_GLKIT_IEGLCONTEXTGROUP_H__
