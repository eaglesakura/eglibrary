/*
 * type.h
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */

#ifndef TYPE_H_
#define TYPE_H_

//! プラットフォーム間依存を無くすために、極力プリミティブは代替型を利用する
typedef signed char s8;
typedef signed short s16;
typedef signed int s32;
typedef signed long long s64;

typedef unsigned char u8;
typedef unsigned short u16;
typedef unsigned int u32;
typedef unsigned long long u64;

/**
 * 文字型
 */
typedef char charactor;

#ifndef	null
	/**
	 * java風null記述
	 */
	#define	null	((void*)0)
#endif

#endif /* TYPE_H_ */
