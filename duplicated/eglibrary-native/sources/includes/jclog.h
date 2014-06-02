/*
 * egllog.h
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */

#ifndef EGLLOG_H_
#define EGLLOG_H_

#ifdef ANDROID

#include "android/log.h"

#ifndef LOG_TAG
#define LOG_TAG "jni-log"
#endif

#ifndef LOG_TYPE
#define LOG_TYPE ANDROID_LOG_INFO
#endif

#define logf(fmt, ... )		__android_log_print(LOG_TYPE, LOG_TAG, fmt, __VA_ARGS__)
#define log( msg )		__android_log_write( LOG_TYPE, LOG_TAG, msg)

// endif ANDROID
#endif

#endif /* EGLLOG_H_ */
