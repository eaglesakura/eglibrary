/*
 * JavaJointInputStream.h
 *
 *  Created on: 2012/05/16
 *      Author: Takeshi
 */

#ifndef JAVAJOINTINPUTSTREAM_H_
#define JAVAJOINTINPUTSTREAM_H_

#include "eglibrary-android.h"

/**
 * Java部分との接続を行うInputStream
 */
class JCaJavaJointInputStream {
	/**
	 * JNI接続
	 */
	JNIEnv *env;

	/**
	 * Java側のInputStream
	 */
	jobject inputstream;

	/**
	 * read(byte[], int, int)メソッドの呼び出しID
	 */
	jmethodID reads;

	/**
	 * 一時的な読み取りのためのバッファ。
	 */
	jbyteArray tempBuffer;
public:
	JCaJavaJointInputStream(JNIEnv *env, jobject inputstream);
	virtual ~JCaJavaJointInputStream();
};

#endif /* JAVAJOINTINPUTSTREAM_H_ */
