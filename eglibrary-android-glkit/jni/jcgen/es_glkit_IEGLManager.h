#ifndef __ES_GLKIT_IEGLMANAGER_H__
#define __ES_GLKIT_IEGLMANAGER_H__

#include  "JointConnector.hpp"

namespace es {
namespace glkit {

class IEGLManager {
public:
    
    virtual ~IEGLManager(){}
    
    /* Constant Fields */
    /* Fields */
    /* Methods */
    virtual ::jc::lang::object_wrapper newDevice (::jc::lang::object_wrapper arg0) = 0;
    
    
};

}
}

#endif // __ES_GLKIT_IEGLMANAGER_H__
