/*
 * new.h
 *
 *  Created on: 2012/05/12
 *      Author: Takeshi
 */

#ifndef NEW_H_
#define NEW_H_

#include "string.h"
#include "stdlib.h"
#include "type.h"

#define		SAFE_DELETE( p )		if( p ){	delete p; p = NULL;		}
#define		SAFE_DELETE_ARRAY( p )	if( p ){	delete[] p; p = NULL;	}
#define		SAFE_RELEASE( p )		if( p ){	Memory::release( p ); p = NULL;	}

extern void* operator new(const size_t size);
extern void* operator new[](const size_t size);

extern void operator delete(void* ptr);
extern void operator delete[](void* ptr);

//!	メモリ管理を行う。
namespace egl {
class Memory {
public:
	//!	指定サイズ分のメモリを確保する。
	static void* alloc(const size_t size);
	//!	指定したアドレスのメモリを開放する。
	static void free(void* ptr);
	//!	参照カウントを１つ上げる。
	static void addRef(void* ptr);
	//!	参照カウントを１つ下げる。
	static s32 release(void* ptr);
	//!	メモリの使用可能サイズ（byte）を取得する。
	static size_t getSize(void* ptr);
	//!	指定したバイト数分オフセットしたポインタを返す
	inline static void* offsetPtr(const void* head, const s32 offset) {
		return (void*) (&(((u8*) head)[offset]));
	}

	//! デバッグ用
	//! 確保した合計サイズを取得する
	static size_t getSumAllocSize();
	//! 開放した合計サイズを取得する
	static size_t getSumFreeSize();
	//! 使用中のメモリサイズを取得する。
	static size_t getExistsSize();
private:
	//! allocした合計メモリサイズ
	static size_t sumAllocSize;

	//! 開放した合計メモリサイズ
	static size_t sumFreeSize;
};
}

#endif /* NEW_H_ */
