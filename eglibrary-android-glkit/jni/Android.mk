LOCAL_PATH := $(call my-dir)
############  Module : cedec2014  ############
include $(CLEAR_VARS)
###### Includes
LOCAL_MODULE := cedec2014

###### Includes
LOCAL_C_INCLUDES += src
LOCAL_C_INCLUDES += jcgen

###### Sources
LOCAL_SRC_FILES += jcgen/es_glkit_DeviceType_impl.cpp
LOCAL_SRC_FILES += jcgen/es_glkit_EGLSpecRequest_impl.cpp
LOCAL_SRC_FILES += jcgen/es_glkit_GLKitUtil_impl.cpp

###### Flags
LOCAL_CFLAGS += -Wno-psabi
LOCAL_CPPFLAGS += -fexceptions
LOCAL_CPPFLAGS += -pthread
LOCAL_CPPFLAGS += -frtti
LOCAL_CPPFLAGS += -std=c++0x

###### Libs
LOCAL_LDLIBS += -lEGL
LOCAL_LDLIBS += -lGLESv1
LOCAL_LDLIBS += -lGLESv3
LOCAL_LDLIBS += -landroid
LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += -ljnigraphics

###### Build
include $(BUILD_SHARED_LIBRARY)

